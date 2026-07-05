package com.canoestudio.retrofuturethewildupdate.block;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockSculkCatalyst extends Block implements ITileEntityProvider {

    public static final PropertyBool BLOOM = PropertyBool.create("bloom");
    private static final int BLOOM_TICKS = 40;

    public BlockSculkCatalyst() {
        super(Material.ROCK);
        this.setRegistryName(RTWU.ID, "sculk_catalyst");
        this.setTranslationKey(RTWU.ID + ".sculk_catalyst");
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setHardness(1.5f);
        this.setResistance(1.5f);
        this.setSoundType(SoundType.SLIME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BLOOM, false));
    }

    public void bloomFromDeath(World world, BlockPos catalystPos, EntityLivingBase deadEntity) {
        if (world.isRemote) {
            return;
        }

        TileEntity tile = world.getTileEntity(catalystPos);
        if (tile instanceof TileEntitySculkCatalyst && !((TileEntitySculkCatalyst) tile).canBloom()) {
            return;
        }

        world.setBlockState(catalystPos, this.getDefaultState().withProperty(BLOOM, true), 3);
        world.scheduleUpdate(catalystPos, this, BLOOM_TICKS);
        world.playSound(null, catalystPos, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.6f, 1.6f);
        this.spreadSculk(world, catalystPos, new BlockPos(deadEntity), Math.max(3, Math.min(12, (int) Math.ceil(deadEntity.getMaxHealth() / 5.0f))));

        if (tile instanceof TileEntitySculkCatalyst) {
            ((TileEntitySculkCatalyst) tile).markBlooming(BLOOM_TICKS + 20);
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote && state.getValue(BLOOM)) {
            worldIn.setBlockState(pos, state.withProperty(BLOOM, false), 3);
        }
    }

    private void spreadSculk(World world, BlockPos catalystPos, BlockPos deathPos, int attempts) {
        Random rand = world.rand;
        for (int i = 0; i < attempts; ++i) {
            BlockPos target = deathPos.add(rand.nextInt(9) - 4, rand.nextInt(5) - 2, rand.nextInt(9) - 4);
            BlockPos floor = findFloor(world, target);
            if (floor == null || floor.distanceSq(catalystPos) > 144.0) {
                continue;
            }

            if (world.isAirBlock(floor.up()) && rand.nextInt(3) == 0) {
                world.setBlockState(floor.up(), ModBlocks.SCULK_VEIN.getDefaultState().withProperty(BlockSculkVein.DOWN, true), 3);
            } else if (canReplaceWithSculk(world, floor)) {
                world.setBlockState(floor, ModBlocks.SCULK.getDefaultState(), 3);
            }
        }
    }

    private BlockPos findFloor(World world, BlockPos origin) {
        for (int offset = 2; offset >= -3; --offset) {
            BlockPos pos = origin.add(0, offset, 0);
            if (canReplaceWithSculk(world, pos) || world.getBlockState(pos).isSideSolid(world, pos, net.minecraft.util.EnumFacing.UP)) {
                return pos;
            }
        }
        return null;
    }

    private boolean canReplaceWithSculk(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        return state.isFullBlock()
            && material != Material.AIR
            && material != Material.WATER
            && material != Material.LAVA
            && material != Material.PORTAL
            && state.getBlock() != Blocks.BEDROCK
            && state.getBlock() != this
            && state.getBlock() != ModBlocks.SCULK_SENSOR
            && state.getBlock() != ModBlocks.SCULK_SHRIEKER;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BLOOM) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(BLOOM, (meta & 1) != 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BLOOM);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySculkCatalyst();
    }
}

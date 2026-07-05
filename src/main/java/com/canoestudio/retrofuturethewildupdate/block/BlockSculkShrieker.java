package com.canoestudio.retrofuturethewildupdate.block;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.entity.Warden;
import com.canoestudio.retrofuturethewildupdate.potion.ModPotions;
import com.canoestudio.retrofuturethewildupdate.sounds.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockSculkShrieker extends Block implements ITileEntityProvider {

    public static final PropertyBool SHRIEKING = PropertyBool.create("shrieking");
    public static final PropertyBool CAN_SUMMON = PropertyBool.create("can_summon");
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.9375, 1.0);
    private static final int SHRIEK_TICKS = 90;
    private static final int WARNING_LIMIT = 4;
    private static final int PLAYER_WARNING_DECAY = 12000;

    public BlockSculkShrieker() {
        super(Material.ROCK);
        this.setRegistryName(RTWU.ID, "sculk_shrieker");
        this.setTranslationKey(RTWU.ID + ".sculk_shrieker");
        this.setCreativeTab(CreativeTabs.REDSTONE);
        this.setHardness(1.5f);
        this.setResistance(1.5f);
        this.setSoundType(SoundType.SLIME);
        this.setDefaultState(this.blockState.getBaseState().withProperty(SHRIEKING, false).withProperty(CAN_SUMMON, true));
    }

    public void receiveVibration(World world, BlockPos pos, @Nullable Entity source, int strength) {
        if (world.isRemote) {
            return;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntitySculkShrieker && !((TileEntitySculkShrieker) tile).canShriek()) {
            return;
        }

        EntityPlayer player = source instanceof EntityPlayer ? (EntityPlayer) source : findNearestPlayer(world, pos);
        if (player == null || player.isCreative() || player.isSpectator()) {
            return;
        }

        this.startShrieking(world, pos, player);
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        this.receiveVibration(worldIn, pos, entityIn, 15);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        this.receiveVibration(worldIn, pos, playerIn, 15);
        return true;
    }

    private void startShrieking(World world, BlockPos pos, EntityPlayer player) {
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockSculkShrieker)) {
            return;
        }

        world.setBlockState(pos, state.withProperty(SHRIEKING, true), 3);
        world.scheduleUpdate(pos, this, SHRIEK_TICKS);
        world.playSound(null, pos, SoundEvents.ENTITY_ENDERMEN_SCREAM, SoundCategory.BLOCKS, 1.7f, 0.45f);
        this.applyDarkness(world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntitySculkShrieker) {
            ((TileEntitySculkShrieker) tile).markShrieking(SHRIEK_TICKS + 30);
        }

        if (state.getValue(CAN_SUMMON)) {
            int warnings = this.addWarning(player, world);
            if (warnings >= WARNING_LIMIT) {
                this.trySpawnWarden(world, pos, player);
                this.clearWarning(player);
            }
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote && state.getValue(SHRIEKING)) {
            worldIn.setBlockState(pos, state.withProperty(SHRIEKING, false), 3);
        }
    }

    private void applyDarkness(World world, BlockPos pos) {
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos).grow(40.0));
        for (EntityPlayer player : players) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.addPotionEffect(new PotionEffect(ModPotions.DARKNESS, 260, 0, true, true));
            }
        }
    }

    private int addWarning(EntityPlayer player, World world) {
        NBTTagCompound data = player.getEntityData();
        long now = world.getTotalWorldTime();
        long lastWarning = data.getLong("rtwu_sculk_warning_time");
        int warnings = now - lastWarning > PLAYER_WARNING_DECAY ? 0 : data.getInteger("rtwu_sculk_warning_count");
        warnings++;
        data.setInteger("rtwu_sculk_warning_count", warnings);
        data.setLong("rtwu_sculk_warning_time", now);
        return warnings;
    }

    private void clearWarning(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        data.setInteger("rtwu_sculk_warning_count", 0);
        data.setLong("rtwu_sculk_warning_time", 0L);
    }

    private void trySpawnWarden(World world, BlockPos shriekerPos, EntityPlayer player) {
        if (world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            return;
        }

        AxisAlignedBB existingArea = new AxisAlignedBB(shriekerPos).grow(48.0);
        if (!world.getEntitiesWithinAABB(Warden.class, existingArea).isEmpty()) {
            return;
        }

        Random rand = world.rand;
        for (int i = 0; i < 24; ++i) {
            double angle = rand.nextDouble() * Math.PI * 2.0;
            double distance = 5.0 + rand.nextDouble() * 7.0;
            double x = player.posX + Math.cos(angle) * distance;
            double z = player.posZ + Math.sin(angle) * distance;
            int startY = Math.max(1, Math.min(255, (int) player.posY + rand.nextInt(5) - 2));

            for (int yOffset = -5; yOffset <= 5; ++yOffset) {
                BlockPos spawnPos = new BlockPos(x, startY + yOffset, z);
                if (this.canSpawnAt(world, spawnPos)) {
                    Warden warden = new Warden(world);
                    warden.setLocationAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                        rand.nextFloat() * 360.0f, 0.0f);
                    warden.startEmerging();
                    warden.enablePersistence();
                    if (world.spawnEntity(warden)) {
                        world.playSound(null, spawnPos, ModSounds.WARDEN_EMERGE, SoundCategory.HOSTILE, 1.5f, 1.0f);
                    }
                    return;
                }
            }
        }
    }

    private boolean canSpawnAt(World world, BlockPos pos) {
        AxisAlignedBB box = new AxisAlignedBB(
            pos.getX() - 0.4, pos.getY(), pos.getZ() - 0.4,
            pos.getX() + 1.4, pos.getY() + 2.8, pos.getZ() + 1.4
        );
        return world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP)
            && world.isAirBlock(pos)
            && world.isAirBlock(pos.up())
            && world.getCollisionBoxes(null, box).isEmpty()
            && !world.containsAnyLiquid(box);
    }

    @Nullable
    private EntityPlayer findNearestPlayer(World world, BlockPos pos) {
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos).grow(16.0));
        EntityPlayer nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (EntityPlayer player : players) {
            double distance = player.getDistanceSq(pos);
            if (distance < nearestDistance && !player.isCreative() && !player.isSpectator()) {
                nearest = player;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(CAN_SUMMON) ? 1 : 0;
        return state.getValue(SHRIEKING) ? meta | 2 : meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(CAN_SUMMON, (meta & 1) != 0)
            .withProperty(SHRIEKING, (meta & 2) != 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, SHRIEKING, CAN_SUMMON);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySculkShrieker();
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }
}

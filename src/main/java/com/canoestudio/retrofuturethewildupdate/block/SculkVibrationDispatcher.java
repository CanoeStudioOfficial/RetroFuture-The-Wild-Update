package com.canoestudio.retrofuturethewildupdate.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = com.canoestudio.retrofuturethewildupdate.RTWU.ID)
public final class SculkVibrationDispatcher {

    private static final int SENSOR_RANGE = 8;
    private static final int SHRIEKER_RANGE = 8;
    private static final int CATALYST_RANGE = 8;
    private static final Map<UUID, BlockPos> LAST_PLAYER_STEP = new HashMap<>();

    public static void emit(World world, BlockPos sourcePos, @Nullable Entity source, int strength) {
        if (world.isRemote) {
            return;
        }
        notifySculkBlocks(world, sourcePos, source, Math.max(1, strength), true);
    }

    public static void emitFromSensor(World world, BlockPos sensorPos, @Nullable Entity source, int strength) {
        if (world.isRemote) {
            return;
        }
        notifySculkBlocks(world, sensorPos, source, Math.max(1, strength), false);
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        World world = entity.world;
        if (world.isRemote || entity.isSneaking() || entity.ticksExisted % 5 != 0) {
            return;
        }

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            UUID id = player.getUniqueID();
            BlockPos current = player.getPosition();
            BlockPos previous = LAST_PLAYER_STEP.get(id);
            if (previous != null && previous.equals(current)) {
                return;
            }
            LAST_PLAYER_STEP.put(id, current);
        }

        double speedSq = entity.motionX * entity.motionX + entity.motionZ * entity.motionZ;
        if (entity.onGround && speedSq > 0.0025) {
            emit(world, entity.getPosition(), entity, 4);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        emit(event.getWorld(), event.getPos(), event.getPlayer(), 12);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.PlaceEvent event) {
        emit(event.getWorld(), event.getPos(), event.getPlayer(), 10);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        emit(event.getWorld(), event.getPos(), event.getEntityPlayer(), 6);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        emit(entity.world, entity.getPosition(), entity, 8);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        World world = entity.world;
        if (world.isRemote) {
            return;
        }

        BlockPos deathPos = entity.getPosition();
        notifyCatalysts(world, deathPos, entity);
        emit(world, deathPos, entity, 15);
    }

    private static void notifySculkBlocks(World world, BlockPos sourcePos, @Nullable Entity source, int strength, boolean includeSensors) {
        int range = Math.max(SENSOR_RANGE, SHRIEKER_RANGE);
        for (BlockPos pos : BlockPos.getAllInBoxMutable(sourcePos.add(-range, -range, -range), sourcePos.add(range, range, range))) {
            IBlockState state = world.getBlockState(pos);
            double distanceSq = pos.distanceSq(sourcePos);
            if (includeSensors && state.getBlock() instanceof BlockSculkSensor && distanceSq <= SENSOR_RANGE * SENSOR_RANGE) {
                ((BlockSculkSensor) state.getBlock()).receiveVibration(world, pos.toImmutable(), source, strengthFromDistance(strength, distanceSq));
            } else if (state.getBlock() instanceof BlockSculkShrieker && distanceSq <= SHRIEKER_RANGE * SHRIEKER_RANGE) {
                ((BlockSculkShrieker) state.getBlock()).receiveVibration(world, pos.toImmutable(), source, strength);
            }
        }
    }

    private static void notifyCatalysts(World world, BlockPos deathPos, EntityLivingBase deadEntity) {
        for (BlockPos pos : BlockPos.getAllInBoxMutable(deathPos.add(-CATALYST_RANGE, -CATALYST_RANGE, -CATALYST_RANGE), deathPos.add(CATALYST_RANGE, CATALYST_RANGE, CATALYST_RANGE))) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockSculkCatalyst && pos.distanceSq(deathPos) <= CATALYST_RANGE * CATALYST_RANGE) {
                ((BlockSculkCatalyst) state.getBlock()).bloomFromDeath(world, pos.toImmutable(), deadEntity);
            }
        }
    }

    private static int strengthFromDistance(int strength, double distanceSq) {
        int loss = (int) Math.floor(Math.sqrt(distanceSq));
        return Math.max(1, Math.min(15, strength - loss + 4));
    }

    private SculkVibrationDispatcher() {
    }
}

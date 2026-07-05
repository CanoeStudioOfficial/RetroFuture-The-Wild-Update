package com.canoestudio.retrofuturethewildupdate.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

public class TileEntitySculkSensor extends TileEntity implements ITickable {

    private int cooldown;

    public boolean canActivate(World world) {
        return this.cooldown <= 0 && world.getTotalWorldTime() >= this.getLastActivationTime() + 5L;
    }

    public void markActivated(World world, int cooldownTicks) {
        this.cooldown = cooldownTicks;
        this.getTileData().setLong("last_activation", world.getTotalWorldTime());
        this.markDirty();
    }

    @Override
    public void update() {
        if (this.cooldown > 0) {
            --this.cooldown;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("cooldown", this.cooldown);
        compound.setLong("last_activation", this.getLastActivationTime());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.cooldown = compound.getInteger("cooldown");
        this.getTileData().setLong("last_activation", compound.getLong("last_activation"));
    }

    private long getLastActivationTime() {
        return this.getTileData().getLong("last_activation");
    }
}

package com.canoestudio.retrofuturethewildupdate.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileEntitySculkShrieker extends TileEntity implements ITickable {

    private int cooldown;

    public boolean canShriek() {
        return this.cooldown <= 0;
    }

    public void markShrieking(int cooldownTicks) {
        this.cooldown = cooldownTicks;
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
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.cooldown = compound.getInteger("cooldown");
    }
}

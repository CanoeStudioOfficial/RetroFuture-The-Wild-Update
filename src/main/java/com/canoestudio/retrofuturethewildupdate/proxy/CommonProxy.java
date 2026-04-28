package com.canoestudio.retrofuturethewildupdate.proxy;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CommonProxy {

    public void preInit() {
    }

    public void init() {
    }

    public void spawnSonicBoom(World world, double x, double y, double z) {
    }

    protected static ResourceLocation prefix(String name) {
        return new ResourceLocation(RTWU.ID, name);
    }
}

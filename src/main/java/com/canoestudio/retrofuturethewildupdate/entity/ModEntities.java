package com.canoestudio.retrofuturethewildupdate.entity;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = RTWU.ID)
public class ModEntities {

    private static int entityId = 0;

    public static final ResourceLocation WARDEN_NAME = new ResourceLocation(RTWU.ID, "warden");

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        IForgeRegistry<EntityEntry> registry = event.getRegistry();
        registerEntity(registry, WARDEN_NAME, Warden.class, Warden::new, 80, 3, true);
    }

    private static <T extends Entity> void registerEntity(
        IForgeRegistry<EntityEntry> registry,
        ResourceLocation registryName,
        Class<T> entityClass,
        Function<World, T> factory,
        int trackingRange,
        int updateInterval,
        boolean sendVelocityUpdates
    ) {
        EntityEntryBuilder<T> builder = EntityEntryBuilder.create()
            .id(registryName, entityId++)
            .name(registryName.getNamespace() + "." + registryName.getPath())
            .entity(entityClass)
            .factory(factory)
            .tracker(trackingRange, updateInterval, sendVelocityUpdates);
        registry.register(builder.build());
    }
}

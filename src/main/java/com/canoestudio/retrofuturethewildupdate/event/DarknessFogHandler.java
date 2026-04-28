package com.canoestudio.retrofuturethewildupdate.event;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.potion.ModPotions;
import com.canoestudio.retrofuturethewildupdate.sounds.ModSounds;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = {Side.CLIENT}, modid = RTWU.ID)
public class DarknessFogHandler {

    private static final ResourceLocation VIGNETTE_TEX = new ResourceLocation(RTWU.ID, "textures/misc/vignette.png");
    private static float darknessPulse = 0.0f;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (darknessPulse > 0.0f) {
                darknessPulse -= 0.025f;
            } else {
                darknessPulse = 0.0f;
            }
        }
    }

    @SubscribeEvent
    public static void onSoundPlay(PlaySoundEvent event) {
        if (event.getSound() != null && ModSounds.WARDEN_HEARTBEAT != null
            && event.getSound().getSoundLocation().equals(ModSounds.WARDEN_HEARTBEAT.getRegistryName())) {
            darknessPulse = 1.0f;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFogColor(EntityViewRenderEvent.FogColors event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null && player.isPotionActive(ModPotions.DARKNESS)) {
            event.setRed(0.0f);
            event.setGreen(0.0f);
            event.setBlue(0.0f);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null && player.isPotionActive(ModPotions.DARKNESS)) {
            if (event.getState().getMaterial() == Material.WATER
                || event.getState().getMaterial() == Material.LAVA) {
                return;
            }
            float density = 0.033f + darknessPulse * 0.1f;
            event.setDensity(density);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HELMET) {
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null && player.isPotionActive(ModPotions.DARKNESS)) {
            ScaledResolution sr = event.getResolution();
            int sw = sr.getScaledWidth();
            int sh = sr.getScaledHeight();
            float alpha = 0.2f + darknessPulse * 0.5f;

            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            GlStateManager.color(0.0f, 0.0f, 0.0f, alpha);
            Minecraft.getMinecraft().getTextureManager().bindTexture(VIGNETTE_TEX);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(0.0, sh, -90.0).tex(0.0, 1.0).endVertex();
            buffer.pos(sw, sh, -90.0).tex(1.0, 1.0).endVertex();
            buffer.pos(sw, 0.0, -90.0).tex(1.0, 0.0).endVertex();
            buffer.pos(0.0, 0.0, -90.0).tex(0.0, 0.0).endVertex();
            tessellator.draw();

            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}

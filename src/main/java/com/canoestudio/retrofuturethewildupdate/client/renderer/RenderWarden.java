package com.canoestudio.retrofuturethewildupdate.client.renderer;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.client.models.ModelWarden;
import com.canoestudio.retrofuturethewildupdate.entity.Warden;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.ResourceLocation;

public class RenderWarden extends RenderLiving<Warden> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(RTWU.ID, "textures/model/warden/warden.png");
    private static final ResourceLocation BIOLUMINESCENT_LAYER =
        new ResourceLocation(RTWU.ID, "textures/model/warden/warden_bioluminescent_layer.png");
    private static final ResourceLocation HEART_LAYER =
        new ResourceLocation(RTWU.ID, "textures/model/warden/warden_heart.png");
    private static final ResourceLocation PULSATING_SPOTS_1 =
        new ResourceLocation(RTWU.ID, "textures/model/warden/warden_pulsating_spots_1.png");
    private static final ResourceLocation PULSATING_SPOTS_2 =
        new ResourceLocation(RTWU.ID, "textures/model/warden/warden_pulsating_spots_2.png");

    public RenderWarden(RenderManager manager) {
        super(manager, new ModelWarden(), 0.9f);
        this.addLayer(new WardenGlowLayer(this));
    }

    @Override
    protected void preRenderCallback(Warden entity, float partialTickTime) {
        GlStateManager.scale(1.0f, 1.0f, 1.0f);
    }

    @Override
    protected ResourceLocation getEntityTexture(Warden entity) {
        return TEXTURE;
    }

    private class WardenGlowLayer implements LayerRenderer<Warden> {

        private final RenderWarden renderer;

        public WardenGlowLayer(RenderWarden renderer) {
            this.renderer = renderer;
        }

        @Override
        public void doRenderLayer(Warden entity, float limbSwing, float limbSwingAmount,
                                   float partialTicks, float ageInTicks, float netHeadYaw,
                                   float headPitch, float scale) {
            ModelWarden model = (ModelWarden) this.renderer.getMainModel();

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            GlStateManager.depthMask(false);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

            this.renderer.bindTexture(BIOLUMINESCENT_LAYER);
            model.root.render(scale);

            float time = entity.ticksExisted + partialTicks;
            float pulse1 = 0.5f + 0.5f * (float) Math.cos(time * 0.1f);
            float pulse2 = 0.5f + 0.5f * (float) Math.cos(time * 0.1f + 1.5f);

            if (pulse1 > 0.05f) {
                this.renderer.bindTexture(PULSATING_SPOTS_1);
                GlStateManager.color(1.0f, 1.0f, 1.0f, pulse1);
                model.root.render(scale);
            }
            if (pulse2 > 0.05f) {
                this.renderer.bindTexture(PULSATING_SPOTS_2);
                GlStateManager.color(1.0f, 1.0f, 1.0f, pulse2);
                model.root.render(scale);
            }

            GlStateManager.disableLighting();
            int i = 61680;
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);

            int anger = entity.getDataManager().get(Warden.ANGER_LEVEL);
            float speed = 0.15f + anger / 80.0f * 0.25f;
            float dynamicHeartPulse = 0.3f + 0.7f * (float) Math.sin(ageInTicks * speed);

            this.renderer.bindTexture(HEART_LAYER);
            GlStateManager.color(1.0f, 1.0f, 1.0f, dynamicHeartPulse);
            model.root.render(scale);

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.depthMask(true);
            i = entity.getBrightnessForRender();
            j = i % 65536;
            k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}

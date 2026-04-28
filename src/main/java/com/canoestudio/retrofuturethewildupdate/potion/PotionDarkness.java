package com.canoestudio.retrofuturethewildupdate.potion;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionDarkness extends Potion {

    private static final ResourceLocation SPRITE = RTWU.prefix("textures/gui/darkness.png");

    public PotionDarkness() {
        super(true, 0);
        this.setPotionName("effect.darkness");
        this.setIconIndex(0, 0);
    }

    @Override
    public boolean shouldRender(PotionEffect effect) {
        return true;
    }

    @Override
    public boolean shouldRenderInvText(PotionEffect effect) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHUDEffect(PotionEffect effect, Gui gui, int x, int y, float z, float alpha) {
        Minecraft.getMinecraft().renderEngine.bindTexture(SPRITE);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        net.minecraftforge.fml.client.config.GuiUtils.drawModalRectWithCustomSizedTexture(
            x + 3, y + 3, z, 0.0f, 0.0f, 18, 18, 18.0f, 18.0f);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(PotionEffect effect, Gui gui, int x, int y, float z) {
        Minecraft.getMinecraft().renderEngine.bindTexture(SPRITE);
        net.minecraftforge.fml.client.config.GuiUtils.drawModalRectWithCustomSizedTexture(
            x + 6, y + 7, z, 0.0f, 0.0f, 18, 18, 18.0f, 18.0f);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getStatusIconIndex() {
        Minecraft.getMinecraft().renderEngine.bindTexture(SPRITE);
        return super.getStatusIconIndex();
    }
}

package com.radsto.radstorms.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.items.ModArmorMaterials;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class GasMaskOverlay {
    private static final ResourceLocation GAS_MASK_TEXTURE =
            new ResourceLocation(RadStormsMod.MOD_ID, "textures/misc/gas_mask_overlay.png");

    public static final IGuiOverlay HUD_GAS_MASK = (gui, guiGraphics, partialTick, width, height) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        ItemStack helmet = minecraft.player.getItemBySlot(EquipmentSlot.HEAD);

        if (!helmet.isEmpty() && helmet.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial() == ModArmorMaterials.GAS_MASK) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            guiGraphics.blit(GAS_MASK_TEXTURE, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    };
}

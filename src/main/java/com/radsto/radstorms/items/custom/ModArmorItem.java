package com.radsto.radstorms.items.custom;

import com.google.common.collect.ImmutableMap;
import com.radsto.radstorms.items.ModArmorMaterials;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ModArmorItem extends ArmorItem {
    private static final Map<ArmorMaterial, MobEffectInstance> MATERIAL_TO_EFFECT_MAP =
            (new ImmutableMap.Builder<ArmorMaterial, MobEffectInstance>())
                    .put(ModArmorMaterials.GAS_MASK, new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1,
                            false, false, true)).build();

    public ModArmorItem(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {
        if(!world.isClientSide()) {
            ArmorMaterial material = this.getMaterial();
            if (MATERIAL_TO_EFFECT_MAP.containsKey(material)) {
                if (material == ModArmorMaterials.GAS_MASK) {
                    if (isWearingGasMask(player)) {
                        addStatusEffect(player, MATERIAL_TO_EFFECT_MAP.get(material));
                    }
                }
                else if (hasFullSuitOfCorrectArmor(player, material)) {
                    addStatusEffect(player, MATERIAL_TO_EFFECT_MAP.get(material));
                }
            }
        }
    }

    private boolean isWearingGasMask(Player player) {
        ItemStack helmetStack = player.getInventory().getArmor(3);
        if (!helmetStack.isEmpty() && helmetStack.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getMaterial() == ModArmorMaterials.GAS_MASK;
        }
        return false;
    }

    private boolean hasFullSuitOfCorrectArmor(Player player, ArmorMaterial material) {
        for (int i = 0; i < 4; i++) {
            ItemStack armorStack = player.getInventory().getArmor(i);
            if (armorStack.isEmpty() || !(armorStack.getItem() instanceof ArmorItem armorItem)) {
                return false;
            }
            if (armorItem.getMaterial() != material) {
                return false;
            }
        }
        return true;
    }

    private void addStatusEffect(Player player, MobEffectInstance effectInstance) {
        player.addEffect(new MobEffectInstance(effectInstance));
    }

//
//    private void evaluateArmorEffects(Player player) {
//        for (Map.Entry<ArmorMaterial, MobEffectInstance> entry : MATERIAL_TO_EFFECT_MAP.entrySet()) {
//            ArmorMaterial mapArmorMaterial = entry.getKey();
//            MobEffectInstance mapStatusEffect = entry.getValue();
//
//            if(hasCorrectArmorOn(mapArmorMaterial, player)) {
//                addStatusEffectForMaterial(player, mapArmorMaterial, mapStatusEffect);
//            }
//        }
//    }
//
//    private void addStatusEffectForMaterial(Player player, ArmorMaterial mapArmorMaterial,
//                                            MobEffectInstance mapStatusEffect) {
//        boolean hasPlayerEffect = player.hasEffect(mapStatusEffect.getEffect());
//
//        if(hasCorrectArmorOn(mapArmorMaterial, player) && !hasPlayerEffect) {
//            player.addEffect(new MobEffectInstance(mapStatusEffect));
//        }
//    }

//    private boolean hasFullSuitOfArmorOn(Player player) {
//        ItemStack boots = player.getInventory().getArmor(0);
//        ItemStack leggings = player.getInventory().getArmor(1);
//        ItemStack chestplate = player.getInventory().getArmor(2);
//        ItemStack helmet  = player.getInventory().getArmor(3);
//
//        return !helmet.isEmpty() && !chestplate.isEmpty()
//                && !leggings.isEmpty() && !boots.isEmpty();
//    }

//    private boolean hasCorrectArmorOn(ArmorMaterial material, Player player) {
//        for (ItemStack armorStack : player.getInventory().armor) {
//            if(!(armorStack.getItem() instanceof ArmorItem)) {
//                return false;
//            }
//        }
//
//        ArmorItem boots = ((ArmorItem)player.getInventory().getArmor(0).getItem());
//        ArmorItem leggings = ((ArmorItem)player.getInventory().getArmor(1).getItem());
//        ArmorItem chestplate = ((ArmorItem)player.getInventory().getArmor(2).getItem());
//        ArmorItem helmet = ((ArmorItem)player.getInventory().getArmor(3).getItem());
//
//        return helmet.getMaterial() == material && chestplate.getMaterial() == material &&
//                leggings.getMaterial() == material && boots.getMaterial() == material;
//    }
}

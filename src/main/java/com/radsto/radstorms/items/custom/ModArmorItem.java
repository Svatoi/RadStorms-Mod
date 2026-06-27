package com.radsto.radstorms.items.custom;

import com.google.common.collect.ImmutableMap;
import com.radsto.radstorms.items.ModArmorMaterials;
import com.radsto.radstorms.items.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag() && stack.getTag().contains("FilterLeft")) {
            int filter = stack.getTag().getInt("FilterLeft");

            int percent = filter / 10;

            if (percent > 50) {
                tooltip.add(Component.literal("§2Фильтр: " + percent + "%"));
            } else if (percent > 15) {
                tooltip.add(Component.literal("§6Фильтр: " + percent + "%"));
            } else {
                tooltip.add(Component.literal("§4Фильтр: " + percent + "% (Требуется замена!)"));
            }
        } else {
            tooltip.add(Component.literal("§7Фильтр: Отсутствует"));
        }
        super.appendHoverText(stack, level, tooltip, flag);
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

    public boolean overrideOtherStackedOnMe(ItemStack pStack, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (pAction == ClickAction.SECONDARY) {
            if(!pOther.isEmpty() && pOther.getItem() == ModItems.GAS_MASK_FILTER.get()) {
                int currentFilter = pStack.getOrCreateTag().getInt("FilterLeft");

                if (currentFilter >= 1000) {
                    return false;
                }

                pStack.getOrCreateTag().putInt("FilterLeft", 1000);

                pOther.shrink(1);

                pPlayer.level().playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                        SoundEvents.ARMOR_EQUIP_LEATHER,
                        SoundSource.PLAYERS, 0.8f, 1.2f);

                return true;
            }
        }

        return super.overrideOtherStackedOnMe(pStack, pOther, pSlot, pAction, pPlayer, pAccess);
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

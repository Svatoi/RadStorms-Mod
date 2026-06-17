package com.radsto.radstorms.items.custom;

import com.radsto.radstorms.capability.PlayerRadiationProvider;
import com.radsto.radstorms.event.ModEvents;
import com.radsto.radstorms.items.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AntiDotItem extends Item {
    public AntiDotItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if(!pLevel.isClientSide()) {
            if (pLivingEntity instanceof Player player) {
                player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                    player.hurt(pLevel.damageSources().magic(), 3f);
                    ModEvents.subRadiationStage(player, pLevel, 20f);
                });
                if (!player.getAbilities().instabuild) {
                    ItemStack emptySyringe = new ItemStack(ModItems.EMPTY_ANTIDOT.get());

                    if (pStack.getCount() <= 1) {
                        return emptySyringe;
                    }

                    player.getInventory().add(emptySyringe);
                    pStack.shrink(1);
                    return pStack;
                }
            }
        }
        return pStack;
    }
}

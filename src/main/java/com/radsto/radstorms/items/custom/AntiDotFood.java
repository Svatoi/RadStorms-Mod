package com.radsto.radstorms.items.custom;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class AntiDotFood {
    public static final FoodProperties ANTIDOT = new FoodProperties.Builder().nutrition(5)
            .saturationMod(0.5F)
            .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 150), 0.4f)
            .fast()
            .alwaysEat()
            .build();
}

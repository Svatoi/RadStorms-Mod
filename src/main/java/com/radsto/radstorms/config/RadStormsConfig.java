package com.radsto.radstorms.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RadStormsConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> RAD_RAIN_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> RAD_RAIN_DAMAGE;

    static {
        BUILDER.push("RadStorms Weather Settings");

        BUILDER.comment("The chance that the rain that has started will be radioactive (0.0 - 0%, 1.0 - 100%)");
        RAD_RAIN_CHANCE = BUILDER.defineInRange("radRainChance", 0.30, 0.0, 1.0);

        BUILDER.comment("The amount of radiation accumulated by the player under radioactive rain every second (once every 20 ticks)");
        RAD_RAIN_DAMAGE = BUILDER.defineInRange("radRainDamage", 0.25, 0.0, 100.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

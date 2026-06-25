package com.radsto.radstorms.recipe;

import com.radsto.radstorms.RadStormsMod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, RadStormsMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<GasMaskFilterRecipe>> GAS_FILTER_SERIALIZER =
            SERIALIZERS.register("gas_filter", () -> GasMaskFilterRecipe.SERIALIZER);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}

package com.radsto.radstorms.items;

import com.radsto.radstorms.RadStormsMod;;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RadStormsMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> RADSTORM_TAB = CREATIVE_MODE_TABS.register("radstorm_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.ANTIDOT.get()))
                    .title(Component.translatable("creativetab.radstorm_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.ANTIDOT.get());
                        pOutput.accept(ModItems.EMPTY_ANTIDOT.get());

                        pOutput.accept(ModItems.GAS_MASK.get());
                        pOutput.accept(ModItems.GAS_MASK_FILTER.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

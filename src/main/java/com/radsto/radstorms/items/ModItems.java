package com.radsto.radstorms.items;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.items.custom.AntiDotFood;
import com.radsto.radstorms.items.custom.AntiDotItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RadStormsMod.MOD_ID);

    public static final RegistryObject<Item> ANTIDOT = ITEMS.register("antidot",
            () -> new AntiDotItem(new Item.Properties().food(AntiDotFood.ANTIDOT)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

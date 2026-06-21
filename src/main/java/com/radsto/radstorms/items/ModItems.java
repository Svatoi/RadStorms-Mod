package com.radsto.radstorms.items;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.items.custom.AntiDotFood;
import com.radsto.radstorms.items.custom.AntiDotItem;
import com.radsto.radstorms.items.custom.ModArmorItem;
import net.minecraft.world.item.ArmorItem;
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
    public static final RegistryObject<Item> EMPTY_ANTIDOT = ITEMS.register("empty_antidot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GAS_MASK = ITEMS.register("gas_mask",
            () -> new ModArmorItem(ModArmorMaterials.GAS_MASK, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

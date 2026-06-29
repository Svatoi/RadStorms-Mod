package com.radsto.radstorms.sound;

import com.radsto.radstorms.RadStormsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RadStormsMod.MOD_ID);

    public static final RegistryObject<SoundEvent> GEIGER_CLICK = SOUND_EVENTS.register("geiger_click",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RadStormsMod.MOD_ID, "geiger_click")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}

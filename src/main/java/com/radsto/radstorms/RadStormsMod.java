package com.radsto.radstorms;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(RadStormsMod.MOD_ID)
public class RadStormsMod {
    // ПЕРЕМЕННЫЕ ДОЛЖНЫ БЫТЬ ТУТ (ВНЕ КОНСТРУКТОРА):
    public static final String MOD_ID = "radstorms_mod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RadStormsMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        GeckoLib.initialize();

//        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RadStormsConfig.SPEC);
        modEventBus.addListener(this::commonSetup);

        // Регистрируем наш главный класс в шине событий Forge
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Basic Radiation Storms setup completed successfully!");
        });
    }
}
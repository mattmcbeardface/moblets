package com.moblets;

import com.moblets.config.MobletsConfig;
import com.moblets.fabric.MobletsFabricEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class BabyMobs implements ModInitializer {

    @Override
    public void onInitialize() {
        MobletsConfig.initialize(
                FabricLoader.getInstance().getConfigDir()
        );

        MobletsFabricEvents.register();

        Moblets.LOGGER.info("Moblets initialized.");
    }
}

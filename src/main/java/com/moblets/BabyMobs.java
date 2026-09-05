package com.moblets;

import net.fabricmc.api.ModInitializer;

public final class BabyMobs implements ModInitializer {

    @Override
    public void onInitialize() {
        BabyMobSpawns.register();
        BabyMobCommands.register();
        BabyPillagers.register();
        BabySnowGolems.register();
        BabyIronGolems.register();
        BabyWanderingTraders.register();
        BabyCamelHusks.register();

        Moblets.LOGGER.info("Moblets initialized.");
    }
}

package com.moblets;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BabyMobs implements ModInitializer {
    public static final String MOD_ID = "baby_mobs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        BabyMobSpawns.register();
        BabyMobCommands.register();
        BabyPillagers.register();
        BabySnowGolems.register();
        BabyIronGolems.register();
        BabyWanderingTraders.register();
        BabyCamelHusks.register();

        LOGGER.info("Moblets initialized.");
    }
}

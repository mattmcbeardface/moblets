package com.pagesofatlas.babymobs;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BabyMobs implements ModInitializer {
    public static final String MOD_ID = "baby_mobs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        BabySkeletons.register();
        BabyCreepers.register();
        BabyMobCommands.register();

        LOGGER.info("Baby Mobs initialized.");
    }
}

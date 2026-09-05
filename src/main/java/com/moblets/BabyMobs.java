package com.moblets;

import com.moblets.fabric.MobletsFabricEvents;
import net.fabricmc.api.ModInitializer;

public final class BabyMobs implements ModInitializer {

    @Override
    public void onInitialize() {
        MobletsFabricEvents.register();

        BabyPillagers.register();
        BabySnowGolems.register();
        BabyIronGolems.register();
        BabyWanderingTraders.register();
        BabyCamelHusks.register();

        Moblets.LOGGER.info("Moblets initialized.");
    }
}

package com.moblets.fabric;

import com.moblets.BabyCamelHusks;
import com.moblets.BabyIronGolems;
import com.moblets.BabyMobCommands;
import com.moblets.BabyMobSpawns;
import com.moblets.BabyPillagers;
import com.moblets.BabySnowGolems;
import com.moblets.BabyWanderingTraders;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.EntityLoadData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.EntitySpawnReason;

public final class MobletsFabricEvents {
    private MobletsFabricEvents() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, buildContext, selection) ->
                        BabyMobCommands.register(dispatcher)
        );

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            EntityLoadData loadData = (EntityLoadData) entity;

            /*
             * Preserve the original registration order:
             * natural Moblet conversion happened before the
             * population/caravan tracking listeners.
             */
            if (!loadData.isLoadedFromDisk()
                    && loadData.spawnReason() == EntitySpawnReason.NATURAL) {
                BabyMobSpawns.handleNaturalSpawn(entity);
            }

            BabyPillagers.onEntityLoaded(entity);
            BabySnowGolems.onEntityLoaded(entity);
            BabyIronGolems.onEntityLoaded(entity);
            BabyWanderingTraders.onEntityLoaded(entity);
            BabyCamelHusks.onEntityLoaded(entity);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            BabyPillagers.onServerTick(server);
            BabySnowGolems.onServerTick(server);
            BabyIronGolems.onServerTick(server);
            BabyWanderingTraders.onServerTick(server);
            BabyCamelHusks.onServerTick(server);
        });
    }
}

package com.moblets.fabric;

import com.moblets.BabyMobCommands;
import com.moblets.BabyMobSpawns;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.EntityLoadData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
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

            if (loadData.isLoadedFromDisk()) {
                return;
            }

            if (loadData.spawnReason() != EntitySpawnReason.NATURAL) {
                return;
            }

            BabyMobSpawns.handleNaturalSpawn(entity);
        });
    }
}

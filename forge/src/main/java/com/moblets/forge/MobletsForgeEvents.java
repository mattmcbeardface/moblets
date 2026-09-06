package com.moblets.forge;

import com.moblets.BabyCamelHusks;
import com.moblets.BabyIronGolems;
import com.moblets.BabyMobCommands;
import com.moblets.BabyMobSpawns;
import com.moblets.BabyPillagers;
import com.moblets.BabySnowGolems;
import com.moblets.BabyWanderingTraders;

import net.minecraft.world.entity.EntitySpawnReason;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;

public final class MobletsForgeEvents {
    private MobletsForgeEvents() {
    }

    public static void register() {
        RegisterCommandsEvent.BUS.addListener(event ->
                BabyMobCommands.register(event.getDispatcher())
        );

        MobSpawnEvent.FinalizeSpawn.BUS.addListener(event -> {
            if (event.getSpawnReason() == EntitySpawnReason.NATURAL) {
                BabyMobSpawns.handleNaturalSpawn(event.getEntity());
            }
        });

        EntityJoinLevelEvent.BUS.addListener(event -> {
            if (event.getLevel().isClientSide()) {
                return;
            }

            var entity = event.getEntity();

            BabyPillagers.onEntityLoaded(entity);
            BabySnowGolems.onEntityLoaded(entity);
            BabyIronGolems.onEntityLoaded(entity);
            BabyWanderingTraders.onEntityLoaded(entity);
            BabyCamelHusks.onEntityLoaded(entity);
        });

        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> {
            var server = event.server();

            BabyPillagers.onServerTick(server);
            BabySnowGolems.onServerTick(server);
            BabyIronGolems.onServerTick(server);
            BabyWanderingTraders.onServerTick(server);
            BabyCamelHusks.onServerTick(server);
        });
    }
}

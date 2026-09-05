package com.moblets.neoforge;

import com.moblets.BabyCamelHusks;
import com.moblets.BabyIronGolems;
import com.moblets.BabyMobCommands;
import com.moblets.BabyMobSpawns;
import com.moblets.BabyPillagers;
import com.moblets.BabySnowGolems;
import com.moblets.BabyWanderingTraders;

import net.minecraft.world.entity.EntitySpawnReason;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class MobletsNeoForgeEvents {

    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent event) {
        BabyMobCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getSpawnType() == EntitySpawnReason.NATURAL) {
            BabyMobSpawns.handleNaturalSpawn(event.getEntity());
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        var entity = event.getEntity();

        BabyPillagers.onEntityLoaded(entity);
        BabySnowGolems.onEntityLoaded(entity);
        BabyIronGolems.onEntityLoaded(entity);
        BabyWanderingTraders.onEntityLoaded(entity);
        BabyCamelHusks.onEntityLoaded(entity);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();

        BabyPillagers.onServerTick(server);
        BabySnowGolems.onServerTick(server);
        BabyIronGolems.onServerTick(server);
        BabyWanderingTraders.onServerTick(server);
        BabyCamelHusks.onServerTick(server);
    }
}

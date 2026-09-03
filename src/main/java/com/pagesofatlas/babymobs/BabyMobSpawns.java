package com.pagesofatlas.babymobs;

import net.fabricmc.fabric.api.event.lifecycle.v1.EntityLoadData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.skeleton.Skeleton;

public final class BabyMobSpawns {

    private BabyMobSpawns() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            // Ignore everything Baby Mobs does not currently support.
            if (!(entity instanceof Skeleton)
                    && !(entity instanceof Creeper)
                    && !(entity instanceof EnderMan)) {
                return;
            }

            EntityLoadData loadData = (EntityLoadData) entity;

            // Existing entities retain whatever state they were saved with.
            if (loadData.isLoadedFromDisk()) {
                return;
            }

            // Commands, spawn eggs, spawners, etc. remain adults.
            if (loadData.spawnReason() != EntitySpawnReason.NATURAL) {
                return;
            }

            if (entity instanceof Skeleton skeleton) {
                if (roll(skeleton, BabySkeletons.NATURAL_BABY_CHANCE)) {
                    BabySkeletons.applyBaby(skeleton);
                }
                return;
            }

            if (entity instanceof Creeper creeper) {
                if (roll(creeper, BabyCreepers.NATURAL_BABY_CHANCE)) {
                    BabyCreepers.applyBaby(creeper);
                }
                return;
            }

            if (entity instanceof EnderMan enderman
                    && roll(enderman, BabyEndermen.NATURAL_BABY_CHANCE)) {
                BabyEndermen.applyBaby(enderman);
            }
        });
    }

    private static boolean roll(Mob mob, float chance) {
        return mob.getRandom().nextFloat() < chance;
    }
}

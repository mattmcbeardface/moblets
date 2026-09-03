package com.pagesofatlas.babymobs;

import net.fabricmc.fabric.api.event.lifecycle.v1.EntityLoadData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

public final class BabyMobSpawns {
    private BabyMobSpawns() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof AbstractSkeleton)
                    && !(entity instanceof Creeper)
                    && !(entity instanceof EnderMan)
                    && !(entity instanceof Witch)
                    && !(entity instanceof CamelHusk)) {
                return;
            }

            EntityLoadData loadData = (EntityLoadData) entity;

            if (loadData.isLoadedFromDisk()) {
                return;
            }

            if (loadData.spawnReason() != EntitySpawnReason.NATURAL) {
                return;
            }

            if (entity instanceof AbstractSkeleton skeleton) {
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

            if (entity instanceof EnderMan enderman) {
                if (roll(enderman, BabyEndermen.NATURAL_BABY_CHANCE)) {
                    BabyEndermen.applyBaby(enderman);
                }
                return;
            }

            if (entity instanceof Witch witch) {
                if (roll(witch, BabyWitches.NATURAL_BABY_CHANCE)) {
                    BabyWitches.applyBaby(witch);
                }
                return;
            }

            if (entity instanceof CamelHusk camelHusk
                    && roll(
                            camelHusk,
                            BabyCamelHusks.NATURAL_BABY_CHANCE
                    )) {
                BabyCamelHusks.applyBaby(camelHusk);
            }
        });
    }

    private static boolean roll(Mob mob, float chance) {
        return mob.getRandom().nextFloat() < chance;
    }
}

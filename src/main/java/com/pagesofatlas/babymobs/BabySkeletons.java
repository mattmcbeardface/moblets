package com.pagesofatlas.babymobs;

import net.fabricmc.fabric.api.event.lifecycle.v1.EntityLoadData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.Skeleton;

public final class BabySkeletons {
    private static final float NATURAL_BABY_CHANCE = 0.05F;

    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(BabyMobs.MOD_ID, "baby_skeleton_scale");

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(BabyMobs.MOD_ID, "baby_skeleton_speed");

    private static final AttributeModifier BABY_SCALE =
            new AttributeModifier(
                    BABY_SCALE_ID,
                    -0.45D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private static final AttributeModifier BABY_SPEED =
            new AttributeModifier(
                    BABY_SPEED_ID,
                    0.25D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private BabySkeletons() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof Skeleton skeleton)) {
                return;
            }

            EntityLoadData loadData = (EntityLoadData) entity;

            // Never reroll skeletons loaded from disk.
            if (loadData.isLoadedFromDisk()) {
                return;
            }

            // Only naturally spawned skeletons participate in the 5% roll.
            // Commands, spawn eggs, spawners, etc. remain adult unless
            // explicitly converted/spawned as babies by Baby Mobs.
            if (loadData.spawnReason() != EntitySpawnReason.NATURAL) {
                return;
            }

            if (skeleton.getRandom().nextFloat() < NATURAL_BABY_CHANCE) {
                applyBaby(skeleton);
            }
        });
    }

    public static void applyBaby(Skeleton skeleton) {
        AttributeInstance scale = skeleton.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.addOrReplacePermanentModifier(BABY_SCALE);
        }

        AttributeInstance speed = skeleton.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addOrReplacePermanentModifier(BABY_SPEED);
        }
    }

    public static boolean isBaby(Skeleton skeleton) {
        AttributeInstance scale = skeleton.getAttribute(Attributes.SCALE);

        return scale != null && scale.hasModifier(BABY_SCALE_ID);
    }
}

package com.pagesofatlas.babymobs;

import com.pagesofatlas.babymobs.mixin.CreeperAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.EntityLoadData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;

public final class BabyCreepers {
    private static final float NATURAL_BABY_CHANCE = 0.08F;

    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(BabyMobs.MOD_ID, "baby_creeper_scale");

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(BabyMobs.MOD_ID, "baby_creeper_speed");

    private static final AttributeModifier BABY_SCALE =
            new AttributeModifier(
                    BABY_SCALE_ID,
                    -0.50D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private static final AttributeModifier BABY_SPEED =
            new AttributeModifier(
                    BABY_SPEED_ID,
                    0.35D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private BabyCreepers() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof Creeper creeper)) {
                return;
            }

            EntityLoadData loadData = (EntityLoadData) entity;

            if (loadData.isLoadedFromDisk()) {
                return;
            }

            if (loadData.spawnReason() != EntitySpawnReason.NATURAL) {
                return;
            }

            if (creeper.getRandom().nextFloat() < NATURAL_BABY_CHANCE) {
                applyBaby(creeper);
            }
        });
    }

    public static void applyBaby(Creeper creeper) {
        AttributeInstance scale = creeper.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.addOrReplacePermanentModifier(BABY_SCALE);
        }

        AttributeInstance speed = creeper.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addOrReplacePermanentModifier(BABY_SPEED);
        }

        ((CreeperAccessor) creeper).babyMobs$setExplosionRadius(2);
    }

    public static boolean isBaby(Creeper creeper) {
        AttributeInstance scale = creeper.getAttribute(Attributes.SCALE);

        return scale != null && scale.hasModifier(BABY_SCALE_ID);
    }
}

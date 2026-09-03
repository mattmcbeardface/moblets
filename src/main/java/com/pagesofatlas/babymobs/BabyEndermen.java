package com.pagesofatlas.babymobs;

import net.fabricmc.fabric.api.event.lifecycle.v1.EntityLoadData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;

public final class BabyEndermen {
    private static final float NATURAL_BABY_CHANCE = 0.08F;

    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(BabyMobs.MOD_ID, "baby_enderman_scale");

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(BabyMobs.MOD_ID, "baby_enderman_speed");

    private static final Identifier BABY_DAMAGE_ID =
            Identifier.fromNamespaceAndPath(BabyMobs.MOD_ID, "baby_enderman_damage");

    private static final AttributeModifier BABY_SCALE =
            new AttributeModifier(
                    BABY_SCALE_ID,
                    -0.55D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private static final AttributeModifier BABY_SPEED =
            new AttributeModifier(
                    BABY_SPEED_ID,
                    0.25D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private static final AttributeModifier BABY_DAMAGE =
            new AttributeModifier(
                    BABY_DAMAGE_ID,
                    -0.65D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private BabyEndermen() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof EnderMan enderman)) {
                return;
            }

            EntityLoadData loadData = (EntityLoadData) entity;

            // Never reroll Endermen loaded from disk.
            if (loadData.isLoadedFromDisk()) {
                return;
            }

            // Only naturally spawned Endermen participate in the 8% roll.
            if (loadData.spawnReason() != EntitySpawnReason.NATURAL) {
                return;
            }

            if (enderman.getRandom().nextFloat() < NATURAL_BABY_CHANCE) {
                applyBaby(enderman);
            }
        });
    }

    public static void applyBaby(EnderMan enderman) {
        AttributeInstance scale = enderman.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.addOrReplacePermanentModifier(BABY_SCALE);
        }

        AttributeInstance speed = enderman.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addOrReplacePermanentModifier(BABY_SPEED);
        }

        AttributeInstance damage = enderman.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.addOrReplacePermanentModifier(BABY_DAMAGE);
        }
    }

    public static boolean isBaby(EnderMan enderman) {
        AttributeInstance scale = enderman.getAttribute(Attributes.SCALE);

        return scale != null && scale.hasModifier(BABY_SCALE_ID);
    }
}

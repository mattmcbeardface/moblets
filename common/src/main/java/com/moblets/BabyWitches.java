package com.moblets;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Witch;

public final class BabyWitches {
    static final float NATURAL_BABY_CHANCE = 0.08F;

    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_witch_scale");

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_witch_speed");

    private static final Identifier TAMED_HEALTH_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "tamed_witch_health");

    private static final AttributeModifier BABY_SCALE =
            new AttributeModifier(
                    BABY_SCALE_ID,
                    -0.50D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private static final AttributeModifier BABY_SPEED =
            new AttributeModifier(
                    BABY_SPEED_ID,
                    0.20D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private BabyWitches() {
    }

    public static void applyBaby(Witch witch) {
        AttributeInstance scale = witch.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.addOrReplacePermanentModifier(BABY_SCALE);
        }

        AttributeInstance speed = witch.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addOrReplacePermanentModifier(BABY_SPEED);
        }
    }

    public static void applyTamedStats(Witch witch) {
        if (!isBaby(witch)) {
            return;
        }

        AttributeInstance health =
                witch.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (health == null) {
            return;
        }

        double targetHealth = 35.0D;

        double bonus =
                targetHealth - health.getBaseValue();

        health.addOrReplacePermanentModifier(
                new AttributeModifier(
                        TAMED_HEALTH_ID,
                        bonus,
                        AttributeModifier.Operation.ADD_VALUE
                )
        );

        witch.setHealth(
                witch.getMaxHealth()
        );
    }

    public static boolean isBaby(Witch witch) {
        AttributeInstance scale = witch.getAttribute(Attributes.SCALE);

        return scale != null && scale.hasModifier(BABY_SCALE_ID);
    }
}

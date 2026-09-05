package com.moblets;

import com.moblets.mixin.CreeperAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;

public final class BabyCreepers {
    static final float NATURAL_BABY_CHANCE = 0.08F;

    private static final int BABY_EXPLOSION_RADIUS = 2;

    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_creeper_scale");

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_creeper_speed");

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

    public static void applyBaby(Creeper creeper) {
        AttributeInstance scale = creeper.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.addOrReplacePermanentModifier(BABY_SCALE);
        }

        AttributeInstance speed = creeper.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addOrReplacePermanentModifier(BABY_SPEED);
        }

        ((CreeperAccessor) creeper)
                .babyMobs$setExplosionRadius(BABY_EXPLOSION_RADIUS);
    }

    public static boolean isBaby(Creeper creeper) {
        AttributeInstance scale = creeper.getAttribute(Attributes.SCALE);

        return scale != null && scale.hasModifier(BABY_SCALE_ID);
    }
}

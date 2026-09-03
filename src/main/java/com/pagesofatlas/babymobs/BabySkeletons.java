package com.pagesofatlas.babymobs;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;

public final class BabySkeletons {
    static final float NATURAL_BABY_CHANCE = 0.08F;

    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(BabyMobs.MOD_ID, "baby_skeleton_scale");

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(BabyMobs.MOD_ID, "baby_skeleton_speed");

    private static final Identifier BABY_WITHER_DAMAGE_ID =
            Identifier.fromNamespaceAndPath(BabyMobs.MOD_ID, "baby_wither_skeleton_damage");

    private static final AttributeModifier BABY_SCALE =
            new AttributeModifier(
                    BABY_SCALE_ID,
                    -0.50D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private static final AttributeModifier BABY_SPEED =
            new AttributeModifier(
                    BABY_SPEED_ID,
                    0.25D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private static final AttributeModifier BABY_WITHER_DAMAGE =
            new AttributeModifier(
                    BABY_WITHER_DAMAGE_ID,
                    -0.50D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private BabySkeletons() {
    }

    public static void applyBaby(AbstractSkeleton skeleton) {
        AttributeInstance scale = skeleton.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.addOrReplacePermanentModifier(BABY_SCALE);
        }

        AttributeInstance speed = skeleton.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addOrReplacePermanentModifier(BABY_SPEED);
        }

        // Wither Skeletons are primarily melee mobs, so give them
        // the same 50% offensive reduction as the ranged skeleton family.
        if (skeleton instanceof WitherSkeleton) {
            AttributeInstance damage = skeleton.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.addOrReplacePermanentModifier(BABY_WITHER_DAMAGE);
            }
        }
    }

    public static boolean isBaby(AbstractSkeleton skeleton) {
        AttributeInstance scale = skeleton.getAttribute(Attributes.SCALE);
        return scale != null && scale.hasModifier(BABY_SCALE_ID);
    }
}

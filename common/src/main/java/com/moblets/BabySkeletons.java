package com.moblets;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;

public final class BabySkeletons {
    static final float NATURAL_BABY_CHANCE = 0.08F;

    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_skeleton_scale");

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_skeleton_speed");

    private static final Identifier BABY_WITHER_DAMAGE_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_wither_skeleton_damage");

    private static final Identifier TAMED_HEALTH_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "tamed_skeleton_health");

    private static final Identifier WOLF_LESSON_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_skeleton_wolf_lesson");

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

    /*
     * Vanilla Skeleton max health is 20 HP.
     * +25% gives a tamed Skeleton Moblet 25 HP.
     */
    private static final AttributeModifier TAMED_HEALTH =
            new AttributeModifier(
                    TAMED_HEALTH_ID,
                    0.25D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    /*
     * Zero-value permanent modifier used only as saved state.
     *
     * Once a Moblet Skeleton gets bitten by a wolf, this marker
     * remembers that lesson even across save/reload.
     */
    private static final AttributeModifier WOLF_LESSON_MARKER =
            new AttributeModifier(
                    WOLF_LESSON_ID,
                    0.0D,
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

    public static void applyTamedStats(
            AbstractSkeleton skeleton
    ) {
        if (!isBaby(skeleton)) {
            return;
        }

        AttributeInstance health =
                skeleton.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (health != null) {
            health.addOrReplacePermanentModifier(
                    TAMED_HEALTH
            );

            /*
             * Successful taming starts the companion at
             * its new full health.
             */
            skeleton.setHealth(
                    skeleton.getMaxHealth()
            );
        }
    }

    public static boolean isBaby(AbstractSkeleton skeleton) {
        AttributeInstance scale = skeleton.getAttribute(Attributes.SCALE);
        return scale != null && scale.hasModifier(BABY_SCALE_ID);
    }

    public static void learnWolfLesson(AbstractSkeleton skeleton) {
        AttributeInstance speed =
                skeleton.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed != null) {
            speed.addOrReplacePermanentModifier(
                    WOLF_LESSON_MARKER
            );
        }
    }

    public static boolean hasLearnedWolfLesson(
            AbstractSkeleton skeleton
    ) {
        AttributeInstance speed =
                skeleton.getAttribute(Attributes.MOVEMENT_SPEED);

        return speed != null
                && speed.hasModifier(WOLF_LESSON_ID);
    }
}

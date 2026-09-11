package com.moblets;

import com.moblets.balance.MobletBalance;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;

public final class BabySkeletons {
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

        // Wither Skeleton damage is attribute-based rather than projectile-based.
        if (skeleton instanceof WitherSkeleton) {
            AttributeInstance damage = skeleton.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.addOrReplacePermanentModifier(BABY_WITHER_DAMAGE);
            }
        }

        MobletBalance.initialize(skeleton);
    }

    public static void applyTamedStats(
            AbstractSkeleton skeleton
    ) {
        if (!isBaby(skeleton)) {
            return;
        }

        /*
         * Companion health is intentionally defined as an
         * explicit target rather than a percentage of each
         * vanilla mob's base health.
         *
         * Skeleton / Stray / Wither Skeleton: 35 HP
         * Bogged / Parched:                    28 HP
         *
         * Each target is 175% of the corresponding vanilla
         * adult's max health.
         */
        double targetHealth;

        if (skeleton.getType() == EntityTypes.SKELETON
                || skeleton.getType() == EntityTypes.STRAY
                || skeleton.getType()
                        == EntityTypes.WITHER_SKELETON) {
            targetHealth = 35.0D;
        } else if (skeleton.getType() == EntityTypes.BOGGED
                || skeleton.getType() == EntityTypes.PARCHED) {
            targetHealth = 28.0D;
        } else {
            return;
        }

        AttributeInstance health =
                skeleton.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (health == null) {
            return;
        }

        /*
         * ADD_VALUE lets us compensate for the different vanilla
         * base-health values while keeping one persistent modifier
         * ID. addOrReplace also migrates the old +25% modifier when
         * this method is applied.
         */
        double bonus =
                targetHealth - health.getBaseValue();

        health.addOrReplacePermanentModifier(
                new AttributeModifier(
                        TAMED_HEALTH_ID,
                        bonus,
                        AttributeModifier.Operation.ADD_VALUE
                )
        );

        /*
         * A newly tamed companion starts at full health.
         */
        skeleton.setHealth(
                skeleton.getMaxHealth()
        );

        /*
         * Wild baby Wither Skeletons deal 50% of the adult
         * attack damage. Taming turns the Wither into our melee
         * specialist, so restore the vanilla adult attack
         * attribute while retaining its baby scale/speed.
         */
        if (skeleton instanceof WitherSkeleton) {
            AttributeInstance damage =
                    skeleton.getAttribute(
                            Attributes.ATTACK_DAMAGE
                    );

            if (damage != null) {
                damage.removeModifier(
                        BABY_WITHER_DAMAGE_ID
                );
            }
        }
    }

    public static boolean isRangedFamily(
            AbstractSkeleton skeleton
    ) {
        return skeleton.getType() == EntityTypes.SKELETON
                || skeleton.getType() == EntityTypes.STRAY
                || skeleton.getType() == EntityTypes.BOGGED
                || skeleton.getType() == EntityTypes.PARCHED;
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

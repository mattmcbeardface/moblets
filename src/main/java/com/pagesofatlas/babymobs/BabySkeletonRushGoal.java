package com.pagesofatlas.babymobs;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public final class BabySkeletonRushGoal extends Goal {
    /*
     * If the player gets farther away than this, the Moblet
     * interrupts its bow behavior and rushes back in.
     */
    private static final double RUSH_START_DISTANCE = 10.0D;
    private static final double RUSH_START_DISTANCE_SQR =
            RUSH_START_DISTANCE * RUSH_START_DISTANCE;

    /*
     * Once this close, release control back to Minecraft's
     * normal bow attack goal.
     */
    private static final double RUSH_STOP_DISTANCE = 8.5D;
    private static final double RUSH_STOP_DISTANCE_SQR =
            RUSH_STOP_DISTANCE * RUSH_STOP_DISTANCE;

    /*
     * The Moblet already has +25% movement speed from its
     * baby attribute. This navigation modifier gives the
     * actual charge a little more urgency.
     */
    private static final double RUSH_SPEED = 1.20D;

    private final AbstractSkeleton skeleton;

    public BabySkeletonRushGoal(AbstractSkeleton skeleton) {
        this.skeleton = skeleton;

        /*
         * Owning MOVE prevents the normal bow goal from running
         * simultaneously while the Moblet is closing distance.
         */
        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE,
                Goal.Flag.LOOK
        ));
    }

    @Override
    public boolean canUse() {
        if (!BabySkeletons.isBaby(this.skeleton)) {
            return false;
        }

        if (!this.skeleton.isHolding(Items.BOW)) {
            return false;
        }

        LivingEntity target = this.skeleton.getTarget();

        /*
         * Keep the special juvenile rush behavior focused on
         * combat with players. Other vanilla Skeleton targets
         * retain their normal behavior.
         */
        if (!(target instanceof Player)
                || target.isDeadOrDying()) {
            return false;
        }

        return this.skeleton.distanceToSqr(target)
                > RUSH_START_DISTANCE_SQR;
    }

    @Override
    public boolean canContinueToUse() {
        if (!BabySkeletons.isBaby(this.skeleton)
                || !this.skeleton.isHolding(Items.BOW)) {
            return false;
        }

        LivingEntity target = this.skeleton.getTarget();

        if (!(target instanceof Player)
                || target.isDeadOrDying()) {
            return false;
        }

        /*
         * Hysteresis:
         *
         * Start rushing beyond 10 blocks.
         * Keep rushing until within 8.5 blocks.
         *
         * This avoids rapid goal switching around one exact
         * distance boundary.
         */
        return this.skeleton.distanceToSqr(target)
                > RUSH_STOP_DISTANCE_SQR;
    }

    @Override
    public void start() {
        /*
         * If it happened to be drawing its bow before the rush
         * took over, cancel the draw immediately.
         */
        this.skeleton.stopUsingItem();
        this.skeleton.setAggressive(true);
    }

    @Override
    public void stop() {
        this.skeleton.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.skeleton.getTarget();

        if (!(target instanceof Player)
                || target.isDeadOrDying()) {
            return;
        }

        /*
         * Absolutely no bow charging while closing distance.
         */
        this.skeleton.stopUsingItem();

        this.skeleton.getNavigation().moveTo(
                target,
                RUSH_SPEED
        );

        this.skeleton.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );
    }
}

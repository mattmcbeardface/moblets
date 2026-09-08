package com.moblets;

import com.moblets.taming.MobletTameState;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

public final class BabySkeletonWolfCuriosityGoal extends Goal {
    /*
     * Moblet notices the wolf well before the wolf notices it.
     */
    private static final double DETECTION_DISTANCE = 24.0D;
    private static final double DETECTION_DISTANCE_SQR =
            DETECTION_DISTANCE * DETECTION_DISTANCE;

    private static final double INSPECTION_DISTANCE = 2.5D;
    private static final double INSPECTION_DISTANCE_SQR =
            INSPECTION_DISTANCE * INSPECTION_DISTANCE;

    private static final double APPROACH_SPEED = 1.20D;

    private final AbstractSkeleton skeleton;
    private Wolf wolf;

    public BabySkeletonWolfCuriosityGoal(
            AbstractSkeleton skeleton
    ) {
        this.skeleton = skeleton;

        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE,
                Goal.Flag.LOOK
        ));
    }

    @Override
    public boolean canUse() {
        if (((MobletTameState) this.skeleton)
                .moblets$isTamed()) {
            return false;
        }

        if (!BabySkeletons.isBaby(this.skeleton)
                || BabySkeletons.hasLearnedWolfLesson(
                        this.skeleton
                )) {
            return false;
        }

        /*
         * A bite is the permanent end of dog curiosity.
         */
        if (this.skeleton.getLastHurtByMob()
                instanceof Wolf) {

            BabySkeletons.learnWolfLesson(
                    this.skeleton
            );

            return false;
        }

        this.wolf = this.findNearestWolf();

        return this.wolf != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (((MobletTameState) this.skeleton)
                .moblets$isTamed()) {
            return false;
        }

        if (!BabySkeletons.isBaby(this.skeleton)
                || BabySkeletons.hasLearnedWolfLesson(
                        this.skeleton
                )
                || this.wolf == null
                || !this.wolf.isAlive()) {
            return false;
        }

        /*
         * This will normally detect the first bite while
         * the Moblet is standing near the wolf.
         */
        if (this.skeleton.getLastHurtByMob()
                instanceof Wolf) {

            BabySkeletons.learnWolfLesson(
                    this.skeleton
            );

            return false;
        }

        return this.skeleton.distanceToSqr(this.wolf)
                <= DETECTION_DISTANCE_SQR;
    }

    @Override
    public void start() {
        this.skeleton.stopUsingItem();
    }

    @Override
    public void stop() {
        this.skeleton.getNavigation().stop();
        this.wolf = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.wolf == null
                || !this.wolf.isAlive()) {
            return;
        }

        this.skeleton.stopUsingItem();

        this.skeleton.getLookControl().setLookAt(
                this.wolf,
                30.0F,
                30.0F
        );

        double distanceSqr =
                this.skeleton.distanceToSqr(this.wolf);

        if (distanceSqr > INSPECTION_DISTANCE_SQR) {
            this.skeleton.getNavigation().moveTo(
                    this.wolf,
                    APPROACH_SPEED
            );
        } else {
            this.skeleton.getNavigation().stop();
        }
    }

    private Wolf findNearestWolf() {
        List<Wolf> wolves =
                this.skeleton.level().getEntitiesOfClass(
                        Wolf.class,
                        this.skeleton
                                .getBoundingBox()
                                .inflate(
                                        DETECTION_DISTANCE,
                                        8.0D,
                                        DETECTION_DISTANCE
                                ),
                        Wolf::isAlive
                );

        Wolf nearest = null;
        double nearestDistanceSqr =
                DETECTION_DISTANCE_SQR;

        for (Wolf candidate : wolves) {
            double distanceSqr =
                    this.skeleton.distanceToSqr(
                            candidate
                    );

            if (distanceSqr < nearestDistanceSqr) {
                nearest = candidate;
                nearestDistanceSqr =
                        distanceSqr;
            }
        }

        return nearest;
    }
}

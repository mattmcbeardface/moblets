package com.moblets;

import java.util.EnumSet;
import java.util.List;

import com.moblets.taming.MobletTargeting;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public final class BabySkeletonWolfFleeGoal extends Goal {
    /*
     * After learning that wolves bite, notice an approaching
     * wolf from this far away and panic BEFORE being bitten.
     */
    private static final double ALERT_DISTANCE = 18.0D;
    private static final double ALERT_DISTANCE_SQR =
            ALERT_DISTANCE * ALERT_DISTANCE;

    /*
     * Flee slightly farther than the alert distance.
     */
    private static final double RESET_DISTANCE = 20.0D;
    private static final double RESET_DISTANCE_SQR =
            RESET_DISTANCE * RESET_DISTANCE;

    private static final double FLEE_SPEED = 1.50D;

    private final AbstractSkeleton skeleton;
    private final PathNavigation navigation;

    private Wolf wolf;
    private Path path;

    public BabySkeletonWolfFleeGoal(
            AbstractSkeleton skeleton
    ) {
        this.skeleton = skeleton;
        this.navigation = skeleton.getNavigation();

        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE,
                Goal.Flag.LOOK
        ));
    }

    @Override
    public boolean canUse() {
        if (!shouldAvoidWolves(
                BabySkeletons.isBaby(this.skeleton),
                MobletTargeting.isTamedMoblet(this.skeleton)
        )) {
            return false;
        }

        /*
         * First bite:
         * learn the lesson immediately and flee.
         */
        LivingEntity attacker =
                this.skeleton.getLastHurtByMob();

        if (attacker instanceof Wolf attackingWolf
                && attackingWolf.isAlive()) {

            BabySkeletons.learnWolfLesson(
                    this.skeleton
            );

            if (this.skeleton.distanceToSqr(
                    attackingWolf
            ) < RESET_DISTANCE_SQR) {

                this.wolf = attackingWolf;
                return this.makeEscapePath();
            }
        }

        /*
         * After the first bite, we no longer wait to get
         * attacked. Detect any nearby wolf and bolt.
         */
        if (!BabySkeletons.hasLearnedWolfLesson(
                this.skeleton
        )) {
            return false;
        }

        this.wolf = this.findNearestWolf();

        if (this.wolf == null) {
            return false;
        }

        return this.makeEscapePath();
    }

    @Override
    public boolean canContinueToUse() {
        return shouldAvoidWolves(
                        BabySkeletons.isBaby(this.skeleton),
                        MobletTargeting.isTamedMoblet(
                                this.skeleton
                        )
                )
                && BabySkeletons.hasLearnedWolfLesson(
                        this.skeleton
                )
                && this.wolf != null
                && this.wolf.isAlive()
                && this.skeleton.distanceToSqr(this.wolf)
                        < RESET_DISTANCE_SQR;
    }

    static boolean shouldAvoidWolves(
            boolean babySkeleton,
            boolean tamedMoblet
    ) {
        return babySkeleton && !tamedMoblet;
    }

    @Override
    public void start() {
        this.skeleton.stopUsingItem();
        this.skeleton.setAggressive(false);

        /*
         * Never retaliate against the dog after learning
         * the lesson.
         */
        if (this.skeleton.getTarget() == this.wolf) {
            this.skeleton.setTarget(null);
        }

        if (this.path != null) {
            this.navigation.moveTo(
                    this.path,
                    FLEE_SPEED
            );
        }
    }

    @Override
    public void stop() {
        if (this.skeleton.getTarget() == this.wolf) {
            this.skeleton.setTarget(null);
        }

        this.navigation.stop();
        this.path = null;
        this.wolf = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.wolf == null) {
            return;
        }

        this.skeleton.stopUsingItem();
        this.skeleton.setAggressive(false);

        if (this.skeleton.getTarget() == this.wolf) {
            this.skeleton.setTarget(null);
        }

        /*
         * If the first escape path ends early, keep finding
         * another route until we're 20 blocks clear.
         */
        if (this.navigation.isDone()
                && this.skeleton.distanceToSqr(this.wolf)
                        < RESET_DISTANCE_SQR) {

            if (this.makeEscapePath()) {
                this.navigation.moveTo(
                        this.path,
                        FLEE_SPEED
                );
            }
        }
    }

    private Wolf findNearestWolf() {
        List<Wolf> wolves =
                this.skeleton.level().getEntitiesOfClass(
                        Wolf.class,
                        this.skeleton
                                .getBoundingBox()
                                .inflate(
                                        ALERT_DISTANCE,
                                        8.0D,
                                        ALERT_DISTANCE
                                ),
                        Wolf::isAlive
                );

        Wolf nearest = null;
        double nearestDistanceSqr =
                ALERT_DISTANCE_SQR;

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

    private boolean makeEscapePath() {
        if (this.wolf == null) {
            return false;
        }

        Vec3 escape = DefaultRandomPos.getPosAway(
                this.skeleton,
                16,
                7,
                this.wolf.position()
        );

        if (escape == null) {
            return false;
        }

        if (this.wolf.distanceToSqr(
                escape.x,
                escape.y,
                escape.z
        ) <= this.wolf.distanceToSqr(
                this.skeleton
        )) {
            return false;
        }

        this.path = this.navigation.createPath(
                escape.x,
                escape.y,
                escape.z,
                0
        );

        return this.path != null;
    }
}

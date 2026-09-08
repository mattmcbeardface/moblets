package com.moblets.taming;

import com.moblets.BabyPillagers;
import com.moblets.BabySkeletons;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class MobletSkeletonSentryGoal extends Goal {
    private static final double DETECTION_RADIUS = 12.0D;
    private static final double DETECTION_RADIUS_SQR =
            DETECTION_RADIUS * DETECTION_RADIUS;

    /*
     * Give tower guards plenty of vertical search space.
     * Engagement distance itself is horizontal so a mob
     * below a wall/tower can still be considered.
     */
    private static final double VERTICAL_SCAN_RADIUS = 32.0D;

    private static final int SCAN_INTERVAL_TICKS = 10;

    /*
     * Vanilla Skeleton bow combat uses a 15-block attack radius.
     *
     * Its backwards-strafe hysteresis works out to:
     *
     *   < 7.5 blocks  -> force backwards movement
     *   > ~13 blocks  -> stop backwards movement
     *
     * Keeping those two thresholds separate is what prevents
     * the "two steps backward, then stop" behavior we were
     * seeing in Stay mode.
     */
    private static final double EVADE_START_DISTANCE_SQR =
            56.25D;

    private static final double EVADE_STOP_DISTANCE_SQR =
            168.75D;

    /*
     * Pillager crossbow AI only begins charging within its
     * eight-block attack radius, so Stay-mode evasion must
     * remain comfortably inside that envelope.
     */
    private static final double PILLAGER_EVADE_START_DISTANCE_SQR =
            25.0D;

    private static final double PILLAGER_EVADE_STOP_DISTANCE_SQR =
            42.25D;

    private static final double PILLAGER_EVADE_ANCHOR_RADIUS =
            5.0D;

    private static final double EVADE_VERTICAL_RANGE =
            2.0D;

    private static final int STRAFE_DIRECTION_INTERVAL =
            20;

    private final Mob mob;
    private int scanCooldown;

    private boolean evading;
    private boolean strafeClockwise;
    private int strafeDirectionTicks;

    public MobletSkeletonSentryGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        MobletTameState state =
                (MobletTameState) this.mob;

        return isSupportedRangedMob()
                && state.moblets$isTamed()
                && state.moblets$isOrderedToStay()
                && state.moblets$getStayAnchor() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void stop() {
        resetEvasion();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        MobletTameState state =
                (MobletTameState) this.mob;

        BlockPos anchor =
                state.moblets$getStayAnchor();

        if (anchor == null) {
            return;
        }

        LivingEntity current =
                this.mob.getTarget();

        /*
         * Once a valid target exists, this goal becomes the
         * sole Stay-mode combat movement authority.
         *
         * RangedBowAttackGoal still aims, draws and fires, but
         * its movement calls are suppressed for sentries.
         */
        if (current != null
                && current.isAlive()
                && !(current instanceof Player)
                && (!(current instanceof MobletTameState targetState)
                        || !targetState.moblets$isTamed())) {

            handleCombatMovement(
                    anchor,
                    current
            );

            return;
        }

        /*
         * A hostile Moblet may have been tamed after this sentry
         * originally selected it. Drop that stale target immediately.
         */
        if (current instanceof MobletTameState targetState
                && targetState.moblets$isTamed()) {

            this.mob.setTarget(null);
        }

        resetEvasion();

        if (this.scanCooldown > 0) {
            --this.scanCooldown;
            return;
        }

        this.scanCooldown = SCAN_INTERVAL_TICKS;

        double anchorX = anchor.getX() + 0.5D;
        double anchorY = anchor.getY() + 0.5D;
        double anchorZ = anchor.getZ() + 0.5D;

        AABB searchBox =
                new AABB(
                        anchorX - DETECTION_RADIUS,
                        anchorY - VERTICAL_SCAN_RADIUS,
                        anchorZ - DETECTION_RADIUS,
                        anchorX + DETECTION_RADIUS,
                        anchorY + VERTICAL_SCAN_RADIUS,
                        anchorZ + DETECTION_RADIUS
                );

        List<Mob> candidates =
                this.mob.level().getEntitiesOfClass(
                        Mob.class,
                        searchBox,
                        this::isCandidate
                );

        Mob best = null;
        double bestDistanceSqr =
                Double.MAX_VALUE;

        for (Mob candidate : candidates) {
            double dx =
                    candidate.getX() - anchorX;

            double dz =
                    candidate.getZ() - anchorZ;

            double horizontalDistanceSqr =
                    dx * dx + dz * dz;

            if (horizontalDistanceSqr
                    > DETECTION_RADIUS_SQR) {
                continue;
            }

            if (!this.mob.getSensing()
                    .hasLineOfSight(candidate)) {
                continue;
            }

            if (horizontalDistanceSqr
                    < bestDistanceSqr) {

                best = candidate;
                bestDistanceSqr =
                        horizontalDistanceSqr;
            }
        }

        if (best != null) {
            this.mob.setTarget(best);
        }
    }

    private void handleCombatMovement(
            BlockPos anchor,
            LivingEntity target
    ) {
        double distanceSqr =
                this.mob.distanceToSqr(target);

        boolean sameVerticalBand =
                Math.abs(
                        target.getY()
                                - this.mob.getY()
                ) <= EVADE_VERTICAL_RANGE;

        /*
         * Enter evasion only once the attacker crosses the
         * same close-range threshold used by vanilla bow AI.
         */
        double evadeStartDistanceSqr =
                this.mob instanceof Pillager
                        ? PILLAGER_EVADE_START_DISTANCE_SQR
                        : EVADE_START_DISTANCE_SQR;

        if (!this.evading
                && sameVerticalBand
                && distanceSqr
                        < evadeStartDistanceSqr) {

            this.evading = true;
            this.strafeClockwise =
                    this.mob.getRandom()
                            .nextBoolean();

            this.strafeDirectionTicks = 0;
        }

        /*
         * Once retreating, keep retreating until real
         * separation has been created.
         *
         * Do NOT shut evasion off merely because the target
         * crossed back over the entry threshold.
         */
        double evadeStopDistanceSqr =
                this.mob instanceof Pillager
                        ? PILLAGER_EVADE_STOP_DISTANCE_SQR
                        : EVADE_STOP_DISTANCE_SQR;

        if (this.evading
                && (!sameVerticalBand
                        || distanceSqr
                                > evadeStopDistanceSqr)) {

            resetEvasion();
        }

        if (this.evading) {
            ++this.strafeDirectionTicks;

            /*
             * Keep a little of vanilla's lateral variation,
             * without ever randomly cancelling the backward
             * component.
             */
            if (this.strafeDirectionTicks
                    >= STRAFE_DIRECTION_INTERVAL) {

                this.strafeDirectionTicks = 0;

                if (this.mob.getRandom()
                        .nextFloat() < 0.30F) {

                    this.strafeClockwise =
                            !this.strafeClockwise;
                }
            }

            /*
             * Prefer the chosen lateral direction. If terrain
             * makes it unsafe, try the opposite side before
             * giving up on movement.
             */
            if (tryEvasionStep(
                    anchor,
                    target,
                    this.strafeClockwise)) {
                return;
            }

            if (tryEvasionStep(
                    anchor,
                    target,
                    !this.strafeClockwise)) {

                this.strafeClockwise =
                        !this.strafeClockwise;

                return;
            }

            /*
             * No safe evasive movement exists. Hold the post
             * and keep fighting rather than stepping into an
             * unsafe location.
             */
            this.mob.getNavigation().stop();
            this.mob.getMoveControl().setWait();
            return;
        }

        /*
         * Outside melee pressure, use the existing deliberate
         * sentry movement:
         *
         *   LOS     -> hold firing position
         *   no LOS  -> safely reposition inside normal radius
         */
        MobletSentryMovement.tick(
                this.mob,
                anchor,
                target
        );
    }

    private boolean isSupportedRangedMob() {
        if (this.mob instanceof AbstractSkeleton skeleton) {
            return BabySkeletons.isBaby(skeleton)
                    && BabySkeletons.isRangedFamily(skeleton);
        }

        if (this.mob instanceof Pillager pillager) {
            return BabyPillagers.isBaby(pillager);
        }

        return false;
    }

    private boolean tryEvasionStep(
            BlockPos anchor,
            LivingEntity target,
            boolean clockwise
    ) {
        return MobletSentryMovement.tryEvasionStep(
                this.mob,
                anchor,
                target,
                clockwise
        );
    }

    private void resetEvasion() {
        this.evading = false;
        this.strafeDirectionTicks = 0;
    }

    private boolean isCandidate(Mob candidate) {
        if (candidate == this.mob
                || !candidate.isAlive()
                || !(candidate instanceof Enemy)) {
            return false;
        }

        /*
         * Never have sentries decide that another tamed
         * hostile-derived Moblet is an enemy.
         */
        if (candidate instanceof MobletTameState state
                && state.moblets$isTamed()) {
            return false;
        }

        return true;
    }
}

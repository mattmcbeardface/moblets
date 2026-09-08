package com.moblets.taming;

import java.util.List;

import com.moblets.BabySkeletons;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.phys.AABB;

/*
 * Stay-mode melee guard behavior for tamed baby Wither Skeletons.
 *
 * Follow mode remains reactive: defend the owner / attack what
 * the owner attacks.
 *
 * Stay mode becomes proactive: detect hostile mobs around the
 * assigned post and let vanilla Wither Skeleton melee AI engage.
 */
public final class MobletWitherGuardGoal extends Goal {

    private static final double DETECTION_RADIUS =
            12.0D;

    private static final double DETECTION_RADIUS_SQR =
            DETECTION_RADIUS * DETECTION_RADIUS;

    private static final double VERTICAL_SCAN_RADIUS =
            8.0D;

    private static final int SCAN_INTERVAL_TICKS =
            10;

    private final WitherSkeleton skeleton;

    private int scanCooldown;

    public MobletWitherGuardGoal(
            WitherSkeleton skeleton
    ) {
        this.skeleton = skeleton;
    }

    @Override
    public boolean canUse() {
        MobletTameState state =
                (MobletTameState) this.skeleton;

        return BabySkeletons.isBaby(this.skeleton)
                && state.moblets$isTamed()
                && state.moblets$isOrderedToStay()
                && state.moblets$getStayAnchor() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        MobletTameState state =
                (MobletTameState) this.skeleton;

        BlockPos anchor =
                state.moblets$getStayAnchor();

        if (anchor == null) {
            return;
        }

        /*
         * Vanilla melee AI already owns the actual pursuit and
         * attack once a valid target has been assigned.
         */
        if (this.skeleton.getTarget() != null
                && this.skeleton.getTarget().isAlive()) {
            return;
        }

        if (this.scanCooldown > 0) {
            --this.scanCooldown;
            return;
        }

        this.scanCooldown =
                SCAN_INTERVAL_TICKS;

        double anchorX =
                anchor.getX() + 0.5D;

        double anchorY =
                anchor.getY() + 0.5D;

        double anchorZ =
                anchor.getZ() + 0.5D;

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
                this.skeleton.level()
                        .getEntitiesOfClass(
                                Mob.class,
                                searchBox,
                                this::isCandidate
                        );

        Mob nearest = null;

        double nearestDistanceSqr =
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

            if (!this.skeleton.getSensing()
                    .hasLineOfSight(candidate)) {
                continue;
            }

            if (horizontalDistanceSqr
                    < nearestDistanceSqr) {

                nearest =
                        candidate;

                nearestDistanceSqr =
                        horizontalDistanceSqr;
            }
        }

        if (nearest != null) {
            this.skeleton.setTarget(
                    nearest
            );
        }
    }

    private boolean isCandidate(
            Mob candidate
    ) {
        if (candidate == this.skeleton
                || !candidate.isAlive()
                || !(candidate instanceof Enemy)) {
            return false;
        }

        /*
         * Never proactively attack another tamed hostile-derived
         * Moblet.
         */
        MobletTameState candidateState =
                (MobletTameState) candidate;

        return !candidateState.moblets$isTamed();
    }
}

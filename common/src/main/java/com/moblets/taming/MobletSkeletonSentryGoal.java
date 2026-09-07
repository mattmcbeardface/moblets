package com.moblets.taming;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
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

    private final Mob mob;
    private int scanCooldown;

    public MobletSkeletonSentryGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        MobletTameState state =
                (MobletTameState) this.mob;

        return state.moblets$isTamed()
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
                (MobletTameState) this.mob;

        BlockPos anchor =
                state.moblets$getStayAnchor();

        if (anchor == null) {
            return;
        }

        LivingEntity current =
                this.mob.getTarget();

        /*
         * Don't interfere with a valid target already being
         * fought. The stay-combat controller handles the
         * outer 16-block engagement cutoff.
         */
        if (current != null
                && current.isAlive()
                && !(current instanceof Player)) {
            return;
        }

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

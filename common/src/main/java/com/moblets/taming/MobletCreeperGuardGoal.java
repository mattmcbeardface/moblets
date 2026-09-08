package com.moblets.taming;

import java.util.List;

import com.moblets.BabyCreepers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

public final class MobletCreeperGuardGoal extends Goal {

    private static final double DETECTION_RADIUS =
            12.0D;

    private static final double DETECTION_RADIUS_SQR =
            DETECTION_RADIUS * DETECTION_RADIUS;

    private static final double VERTICAL_SCAN_RADIUS =
            8.0D;

    private static final int SCAN_INTERVAL_TICKS =
            10;

    private final Creeper creeper;

    private int scanCooldown;

    public MobletCreeperGuardGoal(
            Creeper creeper
    ) {
        this.creeper = creeper;
    }

    @Override
    public boolean canUse() {
        MobletTameState state =
                (MobletTameState) this.creeper;

        return BabyCreepers.isBaby(this.creeper)
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
                (MobletTameState) this.creeper;

        BlockPos anchor =
                state.moblets$getStayAnchor();

        if (anchor == null) {
            return;
        }

        if (this.creeper.getTarget() != null
                && this.creeper.getTarget().isAlive()) {
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
                this.creeper.level()
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

            if (!this.creeper.getSensing()
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
            this.creeper.setTarget(nearest);
        }
    }

    private boolean isCandidate(
            Mob candidate
    ) {
        if (candidate == this.creeper
                || !candidate.isAlive()
                || !(candidate instanceof Enemy)) {
            return false;
        }

        /*
         * Never proactively bomb another tamed Moblet.
         */
        MobletTameState candidateState =
                (MobletTameState) candidate;

        return !candidateState.moblets$isTamed();
    }
}

package com.moblets.taming;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

public final class MobletFollowOwnerGoal extends Goal {
    private static final double FOLLOW_START_DISTANCE = 6.0D;
    private static final double FOLLOW_START_DISTANCE_SQR =
            FOLLOW_START_DISTANCE * FOLLOW_START_DISTANCE;

    private static final double FOLLOW_STOP_DISTANCE = 4.5D;
    private static final double FOLLOW_STOP_DISTANCE_SQR =
            FOLLOW_STOP_DISTANCE * FOLLOW_STOP_DISTANCE;

    private static final double FOLLOW_SPEED = 1.0D;

    /*
     * Following may require routes that initially move away
     * from the owner.
     */
    private static final float FOLLOW_PATH_LENGTH = 32.0F;

    private static final float FOLLOW_PATHFINDING_MULTIPLIER =
            4.0F;

    /*
     * Match vanilla FollowOwnerGoal's repath cadence.
     */
    private static final int REPATH_INTERVAL_TICKS = 10;

    private final Mob mob;

    private Player owner;
    private int timeToRecalcPath;

    public MobletFollowOwnerGoal(Mob mob) {
        this.mob = mob;

        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE,
                Goal.Flag.LOOK
        ));
    }

    @Override
    public boolean canUse() {
        MobletTameState state =
                (MobletTameState) this.mob;

        if (!state.moblets$isTamed()
                || state.moblets$isOrderedToStay()
                || (this.mob.getTarget() != null
                        && this.mob.getTarget().isAlive())) {
            return false;
        }

        this.owner = findOwner(state);

        if (this.owner == null
                || !this.owner.isAlive()
                || this.owner.isSpectator()) {
            return false;
        }

        return this.mob.distanceToSqr(this.owner)
                > FOLLOW_START_DISTANCE_SQR;
    }

    @Override
    public boolean canContinueToUse() {
        MobletTameState state =
                (MobletTameState) this.mob;

        if (!state.moblets$isTamed()
                || state.moblets$isOrderedToStay()
                || (this.mob.getTarget() != null
                        && this.mob.getTarget().isAlive())
                || this.owner == null
                || !this.owner.isAlive()
                || this.owner.isSpectator()) {
            return false;
        }

        return this.mob.distanceToSqr(this.owner)
                > FOLLOW_STOP_DISTANCE_SQR;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;

        this.mob.getNavigation()
                .setRequiredPathLength(
                        FOLLOW_PATH_LENGTH
                );

        this.mob.getNavigation()
                .setMaxVisitedNodesMultiplier(
                        FOLLOW_PATHFINDING_MULTIPLIER
                );
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();

        this.mob.getNavigation()
                .setRequiredPathLength(0.0F);

        this.mob.getNavigation()
                .resetMaxVisitedNodesMultiplier();

        this.owner = null;
        this.timeToRecalcPath = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }

        this.mob.getLookControl().setLookAt(
                this.owner,
                30.0F,
                30.0F
        );

        if (--this.timeToRecalcPath > 0) {
            return;
        }

        this.timeToRecalcPath =
                REPATH_INTERVAL_TICKS;

        Path directPath =
                this.mob.getNavigation()
                        .createPath(
                                this.owner,
                                1
                        );

        /*
         * Best case: vanilla knows exactly how to get there.
         */
        if (directPath != null
                && directPath.canReach()) {

            this.mob.getNavigation().moveTo(
                    directPath,
                    FOLLOW_SPEED
            );

            return;
        }

        /*
         * Owner is below us but vanilla can't safely reach
         * them directly. Search for a reachable lower waypoint.
         *
         * This is what lets a Moblet turn away from a cliff,
         * find stairs behind itself, descend, then resume
         * ordinary following.
         */
        Path recoveryPath =
                MobletFollowRecovery.findDescentPath(
                        this.mob,
                        this.owner
                );

        if (recoveryPath != null) {
            this.mob.getNavigation().moveTo(
                    recoveryPath,
                    FOLLOW_SPEED
            );

            return;
        }

        /*
         * No special recovery available. A partial vanilla
         * path may still be useful for ordinary obstacles.
         */
        if (directPath != null) {
            this.mob.getNavigation().moveTo(
                    directPath,
                    FOLLOW_SPEED
            );
        }
    }

    private Player findOwner(
            MobletTameState state
    ) {
        UUID ownerUuid =
                state.moblets$getOwnerUuid();

        if (ownerUuid == null) {
            return null;
        }

        return this.mob.level()
                .getPlayerByUUID(ownerUuid);
    }
}

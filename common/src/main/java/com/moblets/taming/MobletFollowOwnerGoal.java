package com.moblets.taming;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

public final class MobletFollowOwnerGoal extends Goal {
    /*
     * Start following when the owner gets more than six
     * blocks away. Catch up to roughly 4.5 blocks, then stop.
     *
     * Using different start/stop distances prevents jitter.
     */
    private static final double FOLLOW_START_DISTANCE = 6.0D;
    private static final double FOLLOW_START_DISTANCE_SQR =
            FOLLOW_START_DISTANCE * FOLLOW_START_DISTANCE;

    private static final double FOLLOW_STOP_DISTANCE = 4.5D;
    private static final double FOLLOW_STOP_DISTANCE_SQR =
            FOLLOW_STOP_DISTANCE * FOLLOW_STOP_DISTANCE;

    private static final double FOLLOW_SPEED = 1.0D;

    private final Mob mob;
    private Player owner;

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
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        this.owner = null;
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

        this.mob.getNavigation().moveTo(
                this.owner,
                FOLLOW_SPEED
        );

        this.mob.getLookControl().setLookAt(
                this.owner,
                30.0F,
                30.0F
        );
    }

    private Player findOwner(
            MobletTameState state
    ) {
        UUID ownerUuid =
                state.moblets$getOwnerUuid();

        if (ownerUuid == null) {
            return null;
        }

        return this.mob.level().getPlayerByUUID(
                ownerUuid
        );
    }
}

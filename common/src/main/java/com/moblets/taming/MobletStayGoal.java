package com.moblets.taming;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public final class MobletStayGoal extends Goal {
    /*
     * For now Stay holds the Moblet essentially at its anchor.
     *
     * Later combat AI will be allowed to move within a
     * three-block sentry radius, then this goal will bring it
     * back here after combat.
     */
    private static final double RETURN_DISTANCE = 1.0D;
    private static final double RETURN_DISTANCE_SQR =
            RETURN_DISTANCE * RETURN_DISTANCE;

    private static final double RETURN_SPEED = 1.0D;

    private final Mob mob;

    public MobletStayGoal(Mob mob) {
        this.mob = mob;

        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE
        ));
    }

    @Override
    public boolean canUse() {
        MobletTameState state =
                (MobletTameState) this.mob;

        return state.moblets$isTamed()
                && state.moblets$isOrderedToStay()
                && state.moblets$getStayAnchor() != null
                && (this.mob.getTarget() == null
                        || !this.mob.getTarget().isAlive());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        enforceAnchor();
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        enforceAnchor();
    }

    private void enforceAnchor() {
        MobletTameState state =
                (MobletTameState) this.mob;

        BlockPos anchor =
                state.moblets$getStayAnchor();

        if (anchor == null) {
            stopMovement();
            return;
        }

        double x = anchor.getX() + 0.5D;
        double y = anchor.getY();
        double z = anchor.getZ() + 0.5D;

        double distanceSqr =
                this.mob.distanceToSqr(
                        x,
                        y,
                        z
                );

        /*
         * If something eventually displaces the Moblet from
         * its post, it walks back rather than permanently
         * adopting the new position.
         */
        if (distanceSqr > RETURN_DISTANCE_SQR) {
            this.mob.getNavigation().moveTo(
                    x,
                    y,
                    z,
                    RETURN_SPEED
            );
            return;
        }

        stopMovement();
    }

    private void stopMovement() {
        this.mob.getNavigation().stop();
        this.mob.getMoveControl().setWait();

        this.mob.xxa = 0.0F;
        this.mob.zza = 0.0F;

        var movement =
                this.mob.getDeltaMovement();

        this.mob.setDeltaMovement(
                0.0D,
                movement.y,
                0.0D
        );
    }
}

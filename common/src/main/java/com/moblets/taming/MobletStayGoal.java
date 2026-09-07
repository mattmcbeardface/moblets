package com.moblets.taming;

import java.util.EnumSet;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public final class MobletStayGoal extends Goal {
    private final Mob mob;

    public MobletStayGoal(Mob mob) {
        this.mob = mob;

        /*
         * Claim movement only.
         *
         * The Moblet can still use normal look/head movement
         * while staying so it doesn't feel frozen in place.
         */
        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE
        ));
    }

    @Override
    public boolean canUse() {
        MobletTameState state =
                (MobletTameState) this.mob;

        return state.moblets$isTamed()
                && state.moblets$isOrderedToStay();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        stopMovement();
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

package com.moblets.taming;

import java.util.EnumSet;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/*
 * While a Moblet is tamed, this blocks the mob's ordinary
 * hostile target-selector goals whenever neither of our
 * owner-directed combat goals is active.
 *
 * In other words, a tamed Skeleton stops behaving like a
 * normal Monster looking for random victims.
 */
public final class MobletTamedTargetGuardGoal extends Goal {
    private final Mob mob;

    public MobletTamedTargetGuardGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return ((MobletTameState) this.mob)
                .moblets$isTamed();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.mob.setTarget(null);
    }
}

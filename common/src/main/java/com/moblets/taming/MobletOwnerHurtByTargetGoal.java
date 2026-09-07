package com.moblets.taming;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

public final class MobletOwnerHurtByTargetGoal extends Goal {
    private final Mob mob;

    private LivingEntity target;
    private int lastTimestamp;

    public MobletOwnerHurtByTargetGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        MobletTameState state =
                (MobletTameState) this.mob;

        if (!state.moblets$isTamed()) {
            return false;
        }

        Player owner = findOwner(state);

        if (owner == null || !owner.isAlive()) {
            return false;
        }

        LivingEntity candidate =
                owner.getLastHurtByMob();

        int timestamp =
                owner.getLastHurtByMobTimestamp();

        if (timestamp == this.lastTimestamp
                || !validTarget(candidate)) {
            return false;
        }

        this.target = candidate;
        this.lastTimestamp = timestamp;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null
                && this.target.isAlive()
                && !(this.target instanceof Player)
                && this.mob.getTarget() == this.target;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
    }

    @Override
    public void stop() {
        if (this.mob.getTarget() == this.target) {
            this.mob.setTarget(null);
        }

        this.target = null;
    }

    private boolean validTarget(
            LivingEntity candidate
    ) {
        return candidate != null
                && candidate.isAlive()
                && candidate != this.mob
                && !(candidate instanceof Player);
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

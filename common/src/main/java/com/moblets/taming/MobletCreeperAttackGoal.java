package com.moblets.taming;

import java.util.EnumSet;

import com.moblets.BabyCreepers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class MobletCreeperAttackGoal extends Goal {

    private static final double SWELL_DISTANCE_SQR =
            9.0D;

    /*
     * After detonating, the Creeper tries to open roughly
     * eight blocks of distance before waiting out the rest
     * of its recharge.
     */
    private static final double RETREAT_DISTANCE =
            8.0D;

    private static final double RETREAT_DISTANCE_SQR =
            RETREAT_DISTANCE * RETREAT_DISTANCE;

    private static final double ATTACK_SPEED =
            1.15D;

    private static final double RETREAT_SPEED =
            1.45D;

    private static final int RETREAT_REPATH_INTERVAL =
            8;

    private final Creeper creeper;

    private int retreatRepathCooldown;

    public MobletCreeperAttackGoal(
            Creeper creeper
    ) {
        this.creeper = creeper;

        this.setFlags(
                EnumSet.of(
                        Goal.Flag.MOVE,
                        Goal.Flag.LOOK
                )
        );
    }

    @Override
    public boolean canUse() {
        MobletTameState tameState =
                (MobletTameState) this.creeper;

        LivingEntity target =
                this.creeper.getTarget();

        return BabyCreepers.isBaby(this.creeper)
                && tameState.moblets$isTamed()
                && target != null
                && target.isAlive()
                && !(target instanceof Player)
                && (!(target instanceof MobletTameState targetState)
                        || !targetState.moblets$isTamed());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.creeper.setSwellDir(-1);
        this.retreatRepathCooldown = 0;
    }

    @Override
    public void stop() {
        this.creeper.setSwellDir(-1);
        this.creeper.getNavigation().stop();
        this.retreatRepathCooldown = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target =
                this.creeper.getTarget();

        if (target == null
                || !target.isAlive()) {
            return;
        }

        this.creeper.getLookControl()
                .setLookAt(
                        target,
                        30.0F,
                        30.0F
                );

        MobletCreeperState creeperState =
                (MobletCreeperState) this.creeper;

        /*
         * Recharge behavior:
         *
         * Immediately defuse and retreat from the target.
         * Once roughly eight blocks away, stop and wait out
         * whatever remains of the explosion cooldown.
         */
        if (creeperState.moblets$getExplosionCooldown()
                > 0) {

            this.creeper.setSwellDir(-1);

            double distanceSqr =
                    this.creeper.distanceToSqr(target);

            if (distanceSqr
                    >= RETREAT_DISTANCE_SQR) {

                this.creeper.getNavigation().stop();
                this.retreatRepathCooldown = 0;
                return;
            }

            if (this.retreatRepathCooldown > 0) {
                --this.retreatRepathCooldown;
            }

            if (this.retreatRepathCooldown <= 0
                    || this.creeper.getNavigation().isDone()) {

                this.retreatRepathCooldown =
                        RETREAT_REPATH_INTERVAL;

                Vec3 escape =
                        DefaultRandomPos.getPosAway(
                                this.creeper,
                                12,
                                5,
                                target.position()
                        );

                if (escape != null
                        && target.distanceToSqr(
                                escape.x,
                                escape.y,
                                escape.z
                        ) > target.distanceToSqr(
                                this.creeper
                        )) {

                    this.creeper.getNavigation()
                            .moveTo(
                                    escape.x,
                                    escape.y,
                                    escape.z,
                                    RETREAT_SPEED
                            );
                }
            }

            return;
        }

        this.retreatRepathCooldown = 0;

        /*
         * Recharge complete. Close on the target until normal
         * Creeper detonation range is reached.
         */
        if (this.creeper.distanceToSqr(target)
                        > SWELL_DISTANCE_SQR
                || !this.creeper.getSensing()
                        .hasLineOfSight(target)) {

            this.creeper.setSwellDir(-1);

            this.creeper.getNavigation()
                    .moveTo(
                            target,
                            ATTACK_SPEED
                    );

            return;
        }

        /*
         * Let vanilla Creeper.tick() perform the real swelling,
         * hiss and fuse animation.
         */
        this.creeper.getNavigation().stop();
        this.creeper.setSwellDir(1);
    }
}

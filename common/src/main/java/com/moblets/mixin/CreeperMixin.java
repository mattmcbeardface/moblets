package com.moblets.mixin;

import com.moblets.BabyCreeperFleeGoal;
import com.moblets.BabyCreepers;
import com.moblets.balance.MobletBalance;
import com.moblets.taming.MobletCreeperAttackGoal;
import com.moblets.taming.MobletCreeperGuardGoal;
import com.moblets.taming.MobletCreeperState;
import com.moblets.taming.MobletCuriosityGoal;
import com.moblets.taming.MobletFollowOwnerGoal;
import com.moblets.taming.MobletOwnerHurtByTargetGoal;
import com.moblets.taming.MobletOwnerHurtTargetGoal;
import com.moblets.taming.MobletStayGoal;
import com.moblets.taming.MobletTamedTargetGuardGoal;
import com.moblets.taming.MobletTameState;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin
        extends Monster
        implements MobletCreeperState {

    @Unique
    private static final int MOBLETS_EXPLOSION_COOLDOWN =
            160;

    @Unique
    private int moblets$explosionCooldown;

    protected CreeperMixin(
            EntityType<? extends Monster> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    @Override
    public int moblets$getExplosionCooldown() {
        return this.moblets$explosionCooldown;
    }

    @Override
    public void moblets$setExplosionCooldown(
            int ticks
    ) {
        this.moblets$explosionCooldown =
                Math.max(0, ticks);
    }

    @Inject(
            method = "registerGoals",
            at = @At("TAIL")
    )
    private void babyMobs$addBabyCreeperGoals(
            CallbackInfo ci
    ) {
        Creeper creeper =
                (Creeper) (Object) this;

        /*
         * Owner-directed combat.
         */
        this.targetSelector.addGoal(
                0,
                new MobletOwnerHurtByTargetGoal(
                        creeper
                )
        );

        this.targetSelector.addGoal(
                1,
                new MobletOwnerHurtTargetGoal(
                        creeper
                )
        );

        this.targetSelector.addGoal(
                2,
                new MobletTamedTargetGuardGoal(
                        creeper
                )
        );

        /*
         * Stay owns idle positioning.
         */
        this.goalSelector.addGoal(
                0,
                new MobletStayGoal(
                        creeper
                )
        );

        /*
         * Presenting the Firework Rocket overrides the wild
         * shy/flee behavior while attempting to tame.
         */
        this.goalSelector.addGoal(
                0,
                new MobletCuriosityGoal(
                        creeper
                )
        );

        /*
         * Controlled explosive combat.
         */
        this.goalSelector.addGoal(
                1,
                new MobletCreeperAttackGoal(
                        creeper
                )
        );

        /*
         * Stay-mode proactive hostile detection.
         */
        this.goalSelector.addGoal(
                1,
                new MobletCreeperGuardGoal(
                        creeper
                )
        );

        /*
         * Wild Baby Creepers remain shy.
         */
        this.goalSelector.addGoal(
                1,
                new BabyCreeperFleeGoal(
                        creeper
                )
        );

        /*
         * Follow behavior for tamed Creepers.
         */
        this.goalSelector.addGoal(
                2,
                new MobletFollowOwnerGoal(
                        creeper
                )
        );
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void moblets$tickExplosionCooldown(
            CallbackInfo ci
    ) {
        if (this.moblets$explosionCooldown > 0) {
            --this.moblets$explosionCooldown;
        }
    }

    /*
     * Replace vanilla suicide with a controlled explosion for
     * tamed Baby Creepers.
     *
     * The normal Creeper swell timer and hiss still lead here.
     */
    @Inject(
            method = "explodeCreeper",
            at = @At("HEAD"),
            cancellable = true
    )
    private void moblets$controlledExplosion(
            CallbackInfo ci
    ) {
        Creeper creeper =
                (Creeper) (Object) this;

        if (!BabyCreepers.isBaby(creeper)
                || !((MobletTameState) creeper)
                        .moblets$isTamed()) {
            return;
        }

        /*
         * Vanilla explodeCreeper must not run: it discards the
         * Creeper after detonating.
         */
        ci.cancel();

        if (creeper.level().isClientSide()) {
            return;
        }

        ServerLevel level =
                (ServerLevel) creeper.level();

        /*
         * Use the configured Tamed Moblet base radius. Charged
         * Creepers retain vanilla's doubled blast radius.
         *
         * ExplosionInteraction.NONE guarantees zero terrain or
         * block destruction.
         */
        float radius = MobletBalance.blastRadius(creeper);

        if (creeper.isPowered()) {
            radius *= 2.0F;
        }

        level.explode(
                creeper,
                creeper.getX(),
                creeper.getY(),
                creeper.getZ(),
                radius,
                false,
                Level.ExplosionInteraction.NONE
        );

        /*
         * The controlled blast costs exactly half a heart.
         */
        creeper.hurtServer(
                level,
                creeper.damageSources()
                        .explosion(
                                creeper,
                                creeper
                        ),
                1.0F
        );

        /*
         * Vanilla reached full swell immediately before invoking
         * explodeCreeper(). Reset it so the Moblet can survive and
         * later perform another complete fuse cycle.
         */
        CreeperAccessor accessor =
                (CreeperAccessor) creeper;

        accessor.babyMobs$setSwell(0);
        accessor.babyMobs$setOldSwell(0);

        creeper.setSwellDir(-1);

        this.moblets$explosionCooldown =
                MOBLETS_EXPLOSION_COOLDOWN;
    }
}

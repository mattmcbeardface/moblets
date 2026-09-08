package com.moblets.mixin;

import com.moblets.BabyPillagers;
import com.moblets.taming.MobletSentryMovement;
import com.moblets.taming.MobletTameState;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedCrossbowAttackGoal.class)
public abstract class RangedCrossbowAttackGoalMixin {

    @Unique
    private static final double MOBLETS_EVADE_START_DISTANCE_SQR =
            56.25D;

    @Unique
    private static final double MOBLETS_EVADE_STOP_DISTANCE_SQR =
            81.0D;

    @Unique
    private static final double MOBLETS_FOLLOW_COMBAT_RADIUS =
            10.0D;

    @Unique
    private static final double MOBLETS_EVADE_VERTICAL_RANGE =
            2.0D;

    @Unique
    private static final int MOBLETS_STRAFE_DIRECTION_INTERVAL =
            20;

    @Shadow
    @Final
    private Monster mob;

    @Unique
    private boolean moblets$followEvading;

    @Unique
    private boolean moblets$strafeClockwise;

    @Unique
    private int moblets$strafeDirectionTicks;

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(Lnet/minecraft/world/entity/Entity;D)Z"
            )
    )
    private boolean moblets$disableSentryCrossbowChase(
            PathNavigation navigation,
            Entity target,
            double speed
    ) {
        if (moblets$isSentry()) {
            return false;
        }

        return navigation.moveTo(
                target,
                speed
        );
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;stop()V"
            )
    )
    private void moblets$controlCrossbowCombatMovement(
            PathNavigation navigation
    ) {
        /*
         * Stay mode movement belongs entirely to the sentry
         * movement goal.
         */
        if (moblets$isSentry()) {
            return;
        }

        /*
         * Follow mode normally inherits vanilla Pillager
         * behavior, which simply stands still once in firing
         * range. Give tamed baby Pillagers ranged spacing
         * without interrupting crossbow charging/firing.
         */
        if (moblets$tryFollowEvasion()) {
            return;
        }

        navigation.stop();
    }

    @Inject(
            method = "stop",
            at = @At("HEAD")
    )
    private void moblets$resetEvasionWhenGoalStops(
            CallbackInfo ci
    ) {
        moblets$resetFollowEvasion();
    }

    @Unique
    private boolean moblets$tryFollowEvasion() {
        if (!(this.mob instanceof Pillager pillager)
                || !BabyPillagers.isBaby(pillager)
                || !(this.mob instanceof MobletTameState state)
                || !state.moblets$isTamed()
                || state.moblets$isOrderedToStay()) {

            moblets$resetFollowEvasion();
            return false;
        }

        LivingEntity target =
                this.mob.getTarget();

        if (target == null
                || !target.isAlive()
                || target instanceof Player) {

            moblets$resetFollowEvasion();
            return false;
        }

        boolean sameVerticalBand =
                Math.abs(
                        target.getY()
                                - this.mob.getY()
                ) <= MOBLETS_EVADE_VERTICAL_RANGE;

        if (!sameVerticalBand) {
            moblets$resetFollowEvasion();
            return false;
        }

        double distanceSqr =
                this.mob.distanceToSqr(target);

        if (!this.moblets$followEvading
                && distanceSqr
                        < MOBLETS_EVADE_START_DISTANCE_SQR) {

            this.moblets$followEvading = true;
            this.moblets$strafeClockwise =
                    this.mob.getRandom().nextBoolean();

            this.moblets$strafeDirectionTicks = 0;
        }

        if (this.moblets$followEvading
                && distanceSqr
                        > MOBLETS_EVADE_STOP_DISTANCE_SQR) {

            moblets$resetFollowEvasion();
            return false;
        }

        if (!this.moblets$followEvading) {
            return false;
        }

        ++this.moblets$strafeDirectionTicks;

        if (this.moblets$strafeDirectionTicks
                >= MOBLETS_STRAFE_DIRECTION_INTERVAL) {

            this.moblets$strafeDirectionTicks = 0;

            if (this.mob.getRandom()
                    .nextFloat() < 0.30F) {

                this.moblets$strafeClockwise =
                        !this.moblets$strafeClockwise;
            }
        }

        Player owner =
                state.moblets$getOwnerUuid() == null
                        ? null
                        : this.mob.level()
                                .getPlayerByUUID(
                                        state.moblets$getOwnerUuid()
                                );

        if (owner == null
                || !owner.isAlive()) {
            return false;
        }

        BlockPos ownerAnchor =
                owner.blockPosition();

        if (MobletSentryMovement.tryEvasionStep(
                this.mob,
                ownerAnchor,
                target,
                this.moblets$strafeClockwise,
                MOBLETS_FOLLOW_COMBAT_RADIUS)) {

            return true;
        }

        if (MobletSentryMovement.tryEvasionStep(
                this.mob,
                ownerAnchor,
                target,
                !this.moblets$strafeClockwise,
                MOBLETS_FOLLOW_COMBAT_RADIUS)) {

            this.moblets$strafeClockwise =
                    !this.moblets$strafeClockwise;

            return true;
        }

        /*
         * No safe retreat exists. Stand and keep shooting
         * rather than walking off an edge or abandoning owner.
         */
        return false;
    }

    @Unique
    private void moblets$resetFollowEvasion() {
        this.moblets$followEvading = false;
        this.moblets$strafeDirectionTicks = 0;
    }

    @Unique
    private boolean moblets$isSentry() {
        if (!(this.mob instanceof MobletTameState state)) {
            return false;
        }

        return state.moblets$isTamed()
                && state.moblets$isOrderedToStay();
    }
}

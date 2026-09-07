package com.moblets.mixin;

import com.moblets.taming.MobletTameState;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RangedBowAttackGoal.class)
public abstract class RangedBowAttackGoalMixin {
    @Shadow
    @Final
    private Monster mob;

    /*
     * Vanilla bow AI tries to path directly toward its target
     * whenever it is too far away or lacks sustained sight.
     *
     * Sentries do not chase. Their own safe repositioning
     * logic decides where they may move.
     */
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(Lnet/minecraft/world/entity/Entity;D)Z"
            )
    )
    private boolean moblets$disableSentryBowChase(
            PathNavigation navigation,
            Entity target,
            double speed
    ) {
        if (moblets$isSentry()) {
            return false;
        }

        return navigation.moveTo(target, speed);
    }

    /*
     * Vanilla ranged combat begins random clockwise/backwards
     * strafing after maintaining line of sight.
     *
     * That is specifically undesirable for sentries placed on
     * walls, roofs, bridges and towers.
     */
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/control/MoveControl;strafe(FF)V"
            )
    )
    private void moblets$disableSentryBowStrafe(
            MoveControl moveControl,
            float forwards,
            float sideways
    ) {
        if (moblets$isSentry()) {
            return;
        }

        moveControl.strafe(
                forwards,
                sideways
        );
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

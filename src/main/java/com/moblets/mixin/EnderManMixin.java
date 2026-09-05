package com.moblets.mixin;

import com.moblets.BabyEndermanItemTheftGoal;
import com.moblets.BabyEndermanLootFleeGoal;
import com.moblets.BabyEndermanStareFleeGoal;
import com.moblets.BabyEndermen;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public abstract class EnderManMixin extends Monster {

    protected EnderManMixin(
            EntityType<? extends Monster> type,
            Level level
    ) {
        super(type, level);
    }

    @Inject(
            method = "registerGoals",
            at = @At("TAIL")
    )
    private void babyMobs$addBabyGoals(
            CallbackInfo ci
    ) {
        EnderMan enderman =
                (EnderMan) (Object) this;

        /*
         * Staring is the highest-priority juvenile behavior.
         */
        this.goalSelector.addGoal(
                1,
                new BabyEndermanStareFleeGoal(
                        enderman
                )
        );

        /*
         * Once it has stolen something, chasing the thief
         * makes it run away.
         */
        this.goalSelector.addGoal(
                3,
                new BabyEndermanLootFleeGoal(
                        enderman
                )
        );

        /*
         * Item curiosity outranks ordinary wandering.
         */
        this.goalSelector.addGoal(
                6,
                new BabyEndermanItemTheftGoal(
                        enderman
                )
        );
    }

    @Inject(
            method = "setTarget",
            at = @At("HEAD"),
            cancellable = true
    )
    private void babyMobs$neverTargetAnything(
            @Nullable LivingEntity target,
            CallbackInfo ci
    ) {
        EnderMan enderman =
                (EnderMan) (Object) this;

        if (target != null
                && BabyEndermen.isBaby(enderman)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "requiresCustomPersistence",
            at = @At("HEAD"),
            cancellable = true
    )
    private void babyMobs$keepStolenLootSafe(
            CallbackInfoReturnable<Boolean> cir
    ) {
        EnderMan enderman =
                (EnderMan) (Object) this;

        if (BabyEndermen.isBaby(enderman)
                && !enderman
                        .getItemBySlot(
                                EquipmentSlot.MAINHAND
                        )
                        .isEmpty()) {
            cir.setReturnValue(true);
        }
    }
}

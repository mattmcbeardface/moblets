package com.pagesofatlas.babymobs.mixin;

import com.pagesofatlas.babymobs.BabyEndermen;

import net.minecraft.world.entity.monster.EnderMan;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
        targets = "net.minecraft.world.entity.monster.EnderMan$EndermanLeaveBlockGoal"
)
public abstract class EndermanLeaveBlockGoalMixin {

    @Accessor("enderman")
    abstract EnderMan babyMobs$getEnderman();

    @Inject(
            method = "canUse",
            at = @At("HEAD"),
            cancellable = true
    )
    private void babyMobs$disableBabyBlockPlacing(
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (BabyEndermen.isBaby(
                this.babyMobs$getEnderman()
        )) {
            cir.setReturnValue(false);
        }
    }
}

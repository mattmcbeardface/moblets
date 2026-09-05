package com.moblets.mixin;

import com.moblets.BabyEndermen;

import net.minecraft.world.entity.monster.EnderMan;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
        targets = "net.minecraft.world.entity.monster.EnderMan$EndermanTakeBlockGoal"
)
public abstract class EndermanTakeBlockGoalMixin {

    @Accessor("enderman")
    abstract EnderMan babyMobs$getEnderman();

    @Inject(
            method = "canUse",
            at = @At("HEAD"),
            cancellable = true
    )
    private void babyMobs$disableBabyBlockTaking(
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (BabyEndermen.isBaby(
                this.babyMobs$getEnderman()
        )) {
            cir.setReturnValue(false);
        }
    }
}

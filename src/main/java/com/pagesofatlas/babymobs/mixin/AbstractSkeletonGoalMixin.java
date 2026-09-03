package com.pagesofatlas.babymobs.mixin;

import com.pagesofatlas.babymobs.BabySkeletonRushGoal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonGoalMixin extends Monster {

    protected AbstractSkeletonGoalMixin(
            EntityType<? extends Monster> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    @Inject(
            method = "registerGoals",
            at = @At("TAIL")
    )
    private void babyMobs$addBabySkeletonRushGoal(
            CallbackInfo ci
    ) {
        AbstractSkeleton skeleton =
                (AbstractSkeleton) (Object) this;

        /*
         * Priority 3 beats Minecraft's bowGoal at priority 4.
         *
         * Existing sun/wolf avoidance goals at priority 3 remain
         * available and were registered before this goal.
         */
        this.goalSelector.addGoal(
                3,
                new BabySkeletonRushGoal(skeleton)
        );
    }
}

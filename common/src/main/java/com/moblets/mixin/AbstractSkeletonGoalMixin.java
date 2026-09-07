package com.moblets.mixin;

import com.moblets.AdultSkeletonWolfAvoidGoal;
import com.moblets.BabySkeletonRushGoal;
import com.moblets.BabySkeletonWolfCuriosityGoal;
import com.moblets.BabySkeletonWolfFleeGoal;
import com.moblets.taming.MobletCuriosityGoal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
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
    private void babyMobs$addBabySkeletonGoals(
            CallbackInfo ci
    ) {
        AbstractSkeleton skeleton =
                (AbstractSkeleton) (Object) this;

        /*
         * AbstractSkeleton registers exactly one AvoidEntityGoal:
         * vanilla 6-block wolf avoidance.
         *
         * Replace it with an equivalent adult-only version so
         * Moblets are free to make terrible dog-related decisions.
         */
        this.goalSelector.removeAllGoals(
                goal -> goal instanceof AvoidEntityGoal<?>
        );

        this.goalSelector.addGoal(
                3,
                new AdultSkeletonWolfAvoidGoal(skeleton)
        );

        /*
         * Being bitten by a wolf overrides everything else.
         */
        this.goalSelector.addGoal(
                0,
                new BabySkeletonWolfFleeGoal(skeleton)
        );

        /*
         * A wild Skeleton Moblet becomes inquisitive when a
         * nearby player presents its taming item.
         */
        this.goalSelector.addGoal(
                1,
                new MobletCuriosityGoal(skeleton)
        );

        /*
         * Wolf curiosity beats normal bow combat and player rushing.
         */
        this.goalSelector.addGoal(
                2,
                new BabySkeletonWolfCuriosityGoal(skeleton)
        );

        /*
         * Existing short-range player combat behavior.
         */
        this.goalSelector.addGoal(
                3,
                new BabySkeletonRushGoal(skeleton)
        );
    }
}

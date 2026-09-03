package com.pagesofatlas.babymobs;

import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

public final class AdultSkeletonWolfAvoidGoal extends AvoidEntityGoal<Wolf> {
    private final AbstractSkeleton skeleton;

    public AdultSkeletonWolfAvoidGoal(AbstractSkeleton skeleton) {
        /*
         * Exact vanilla AbstractSkeleton wolf avoidance values.
         */
        super(
                skeleton,
                Wolf.class,
                6.0F,
                1.0D,
                1.2D
        );

        this.skeleton = skeleton;
    }

    @Override
    public boolean canUse() {
        return !BabySkeletons.isBaby(this.skeleton)
                && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !BabySkeletons.isBaby(this.skeleton)
                && super.canContinueToUse();
    }
}

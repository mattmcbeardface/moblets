package com.pagesofatlas.babymobs.mixin.client;

import com.pagesofatlas.babymobs.BabySkeletons;
import com.pagesofatlas.babymobs.client.BabyVariantRenderState;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeletonRenderer.class)
public abstract class AbstractSkeletonRendererMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;F)V",
            at = @At("TAIL")
    )
    private void babyMobs$markBabySkeleton(
            AbstractSkeleton entity,
            SkeletonRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        boolean baby = entity instanceof Skeleton skeleton
                && BabySkeletons.isBaby(skeleton);

        ((BabyVariantRenderState) state).babyMobs$setBabyVariant(baby);
    }
}

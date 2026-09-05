package com.moblets.mixin.client;

import com.moblets.client.BabyVariantRenderState;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkeletonModel.class)
public abstract class SkeletonModelMixin {

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;)V",
            at = @At("TAIL")
    )
    private void babyMobs$applyBabyProportions(
            SkeletonRenderState state,
            CallbackInfo ci
    ) {
        SkeletonModel<?> model = (SkeletonModel<?>) (Object) this;

        boolean baby =
                ((BabyVariantRenderState) state).babyMobs$isBabyVariant();

        //
        // Oversized skull.
        //
        float headScale = baby ? 1.25F : 1.0F;

        model.head.xScale = headScale;
        model.head.yScale = headScale;
        model.head.zScale = headScale;

        //
        // Slightly broader/deeper ribcage.
        //
        model.body.xScale = baby ? 1.10F : 1.0F;
        model.body.yScale = 1.0F;
        model.body.zScale = baby ? 1.15F : 1.0F;

        //
        // Shorter arms, normal thickness.
        //
        model.rightArm.xScale = 1.0F;
        model.rightArm.yScale = baby ? 0.90F : 1.0F;
        model.rightArm.zScale = 1.0F;

        model.leftArm.xScale = 1.0F;
        model.leftArm.yScale = baby ? 0.90F : 1.0F;
        model.leftArm.zScale = 1.0F;

        //
        // Shorter legs, normal thickness.
        //
        model.rightLeg.xScale = 1.0F;
        model.rightLeg.yScale = baby ? 0.90F : 1.0F;
        model.rightLeg.zScale = 1.0F;

        model.leftLeg.xScale = 1.0F;
        model.leftLeg.yScale = baby ? 0.90F : 1.0F;
        model.leftLeg.zScale = 1.0F;
    }
}

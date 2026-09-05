package com.moblets.mixin.client;

import com.moblets.client.BabyVariantRenderState;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndermanModel.class)
public abstract class EndermanModelMixin {

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/EndermanRenderState;)V",
            at = @At("TAIL")
    )
    private void babyMobs$applyBabyProportions(
            EndermanRenderState state,
            CallbackInfo ci
    ) {
        EndermanModel<?> model = (EndermanModel<?>) (Object) this;

        boolean baby =
                ((BabyVariantRenderState) state).babyMobs$isBabyVariant();

        float headScale = baby ? 1.20F : 1.0F;

        model.head.xScale = headScale;
        model.head.yScale = 1.0F;
        model.head.zScale = headScale;

        // Shorter arms, but retain their normal thickness.
        model.rightArm.xScale = 1.0F;
        model.rightArm.yScale = baby ? 0.80F : 1.0F;
        model.rightArm.zScale = 1.0F;

        model.leftArm.xScale = 1.0F;
        model.leftArm.yScale = baby ? 0.80F : 1.0F;
        model.leftArm.zScale = 1.0F;

        // Slightly shorter legs, also retaining normal thickness.
        model.rightLeg.xScale = 1.0F;
        model.rightLeg.yScale = baby ? 0.90F : 1.0F;
        model.rightLeg.zScale = 1.0F;

        model.leftLeg.xScale = 1.0F;
        model.leftLeg.yScale = baby ? 0.90F : 1.0F;
        model.leftLeg.zScale = 1.0F;
    }
}

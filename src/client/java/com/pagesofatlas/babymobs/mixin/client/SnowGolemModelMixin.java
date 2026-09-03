package com.pagesofatlas.babymobs.mixin.client;

import com.pagesofatlas.babymobs.client.BabyVariantRenderState;
import net.minecraft.client.model.animal.golem.SnowGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowGolemModel.class)
public abstract class SnowGolemModelMixin {

    @Unique
    private ModelPart babyMobs$head;

    @Unique
    private ModelPart babyMobs$upperBody;

    @Unique
    private ModelPart babyMobs$lowerBody;

    @Unique
    private ModelPart babyMobs$leftArm;

    @Unique
    private ModelPart babyMobs$rightArm;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void babyMobs$captureSnowGolemParts(
            ModelPart root,
            CallbackInfo ci
    ) {
        this.babyMobs$head =
                root.getChild("head");

        this.babyMobs$upperBody =
                root.getChild("upper_body");

        this.babyMobs$lowerBody =
                root.getChild("lower_body");

        this.babyMobs$leftArm =
                root.getChild("left_arm");

        this.babyMobs$rightArm =
                root.getChild("right_arm");
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)V",
            at = @At("TAIL")
    )
    private void babyMobs$applyBabySnowGolemProportions(
            LivingEntityRenderState state,
            CallbackInfo ci
    ) {
        boolean baby =
                ((BabyVariantRenderState) state)
                        .babyMobs$isBabyVariant();

        float headScale =
                baby ? 1.25F : 1.0F;

        float upperBodyScale =
                baby ? 1.05F : 1.0F;

        float lowerBodyScale =
                baby ? 0.95F : 1.0F;

        float armLength =
                baby ? 0.90F : 1.0F;

        this.babyMobs$head.xScale = headScale;
        this.babyMobs$head.yScale = headScale;
        this.babyMobs$head.zScale = headScale;

        this.babyMobs$upperBody.xScale =
                upperBodyScale;
        this.babyMobs$upperBody.yScale =
                upperBodyScale;
        this.babyMobs$upperBody.zScale =
                upperBodyScale;

        this.babyMobs$lowerBody.xScale =
                lowerBodyScale;
        this.babyMobs$lowerBody.yScale =
                lowerBodyScale;
        this.babyMobs$lowerBody.zScale =
                lowerBodyScale;

        this.babyMobs$leftArm.xScale = 1.0F;
        this.babyMobs$leftArm.yScale = armLength;
        this.babyMobs$leftArm.zScale = 1.0F;

        this.babyMobs$rightArm.xScale = 1.0F;
        this.babyMobs$rightArm.yScale = armLength;
        this.babyMobs$rightArm.zScale = 1.0F;
    }
}

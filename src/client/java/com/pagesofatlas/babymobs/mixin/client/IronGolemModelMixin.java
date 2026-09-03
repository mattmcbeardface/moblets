package com.pagesofatlas.babymobs.mixin.client;

import com.pagesofatlas.babymobs.client.BabyVariantRenderState;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronGolemModel.class)
public abstract class IronGolemModelMixin {

    @Unique
    private ModelPart babyMobs$head;

    @Unique
    private ModelPart babyMobs$body;

    @Unique
    private ModelPart babyMobs$rightArm;

    @Unique
    private ModelPart babyMobs$leftArm;

    @Unique
    private ModelPart babyMobs$rightLeg;

    @Unique
    private ModelPart babyMobs$leftLeg;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void babyMobs$captureIronGolemParts(
            ModelPart root,
            CallbackInfo ci
    ) {
        this.babyMobs$head =
                root.getChild("head");

        this.babyMobs$body =
                root.getChild("body");

        this.babyMobs$rightArm =
                root.getChild("right_arm");

        this.babyMobs$leftArm =
                root.getChild("left_arm");

        this.babyMobs$rightLeg =
                root.getChild("right_leg");

        this.babyMobs$leftLeg =
                root.getChild("left_leg");
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/IronGolemRenderState;)V",
            at = @At("TAIL")
    )
    private void babyMobs$applyBabyIronGolemProportions(
            IronGolemRenderState state,
            CallbackInfo ci
    ) {
        boolean baby =
                ((BabyVariantRenderState) state)
                        .babyMobs$isBabyVariant();

        float headScale =
                baby ? 1.25F : 1.0F;

        float bodyX =
                baby ? 1.05F : 1.0F;

        float bodyY =
                baby ? 0.95F : 1.0F;

        float bodyZ =
                baby ? 1.05F : 1.0F;

        float armLength =
                baby ? 0.85F : 1.0F;

        float legLength =
                baby ? 0.90F : 1.0F;

        this.babyMobs$head.xScale = headScale;
        this.babyMobs$head.yScale = headScale;
        this.babyMobs$head.zScale = headScale;

        this.babyMobs$body.xScale = bodyX;
        this.babyMobs$body.yScale = bodyY;
        this.babyMobs$body.zScale = bodyZ;

        this.babyMobs$rightArm.xScale = 1.0F;
        this.babyMobs$rightArm.yScale = armLength;
        this.babyMobs$rightArm.zScale = 1.0F;

        this.babyMobs$leftArm.xScale = 1.0F;
        this.babyMobs$leftArm.yScale = armLength;
        this.babyMobs$leftArm.zScale = 1.0F;

        this.babyMobs$rightLeg.xScale = 1.0F;
        this.babyMobs$rightLeg.yScale = legLength;
        this.babyMobs$rightLeg.zScale = 1.0F;

        this.babyMobs$leftLeg.xScale = 1.0F;
        this.babyMobs$leftLeg.yScale = legLength;
        this.babyMobs$leftLeg.zScale = 1.0F;
    }
}

package com.moblets.mixin.client;

import com.moblets.client.BabyVariantRenderState;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.animal.camel.CamelModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.CamelRenderState;
import net.minecraft.world.entity.EntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CamelModel.class)
public abstract class CamelModelMixin {

    @Unique
    private ModelPart babyMobs$body;

    @Unique
    private ModelPart babyMobs$head;

    @Unique
    private ModelPart babyMobs$backLeftLeg;

    @Unique
    private ModelPart babyMobs$backRightLeg;

    @Unique
    private ModelPart babyMobs$frontLeftLeg;

    @Unique
    private ModelPart babyMobs$frontRightLeg;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void babyMobs$captureCamelParts(
            ModelPart root,
            AnimationDefinition walk,
            AnimationDefinition sit,
            AnimationDefinition sitPose,
            AnimationDefinition standup,
            AnimationDefinition idle,
            AnimationDefinition dash,
            CallbackInfo ci
    ) {
        this.babyMobs$body =
                root.getChild("body");

        this.babyMobs$head =
                this.babyMobs$body.getChild("head");

        this.babyMobs$backLeftLeg =
                root.getChild("left_hind_leg");

        this.babyMobs$backRightLeg =
                root.getChild("right_hind_leg");

        this.babyMobs$frontLeftLeg =
                root.getChild("left_front_leg");

        this.babyMobs$frontRightLeg =
                root.getChild("right_front_leg");
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/CamelRenderState;)V",
            at = @At("TAIL")
    )
    private void babyMobs$applyBabyCamelHuskProportions(
            CamelRenderState state,
            CallbackInfo ci
    ) {
        /*
         * This mixin also runs for ordinary Camel models.
         * Do absolutely nothing unless this render state belongs to
         * a Camel Husk.
         */
        if (state.entityType != EntityTypes.CAMEL_HUSK) {
            return;
        }

        boolean baby =
                ((BabyVariantRenderState) state)
                        .babyMobs$isBabyVariant();

        float bodyX =
                baby ? 1.05F : 1.0F;

        float bodyY =
                baby ? 0.95F : 1.0F;

        float bodyZ =
                baby ? 1.05F : 1.0F;

        float headScaleXZ =
                baby ? 0.95F : 1.0F;

        float headScaleY =
                baby ? 0.80F : 1.0F;

        float legLength =
                baby ? 0.85F : 1.0F;

        /*
         * Slightly rounder/shorter body.
         */
        this.babyMobs$body.xScale = bodyX;
        this.babyMobs$body.yScale = bodyY;
        this.babyMobs$body.zScale = bodyZ;

        /*
         * Oversized juvenile head.
         */
        this.babyMobs$head.xScale = headScaleXZ;
        this.babyMobs$head.yScale = headScaleY;
        this.babyMobs$head.zScale = headScaleXZ;

        /*
         * Shorter legs while preserving their normal thickness.
         */
        setLegScale(
                this.babyMobs$backLeftLeg,
                legLength
        );

        setLegScale(
                this.babyMobs$backRightLeg,
                legLength
        );

        setLegScale(
                this.babyMobs$frontLeftLeg,
                legLength
        );

        setLegScale(
                this.babyMobs$frontRightLeg,
                legLength
        );
    }

    @Unique
    private static void setLegScale(
            ModelPart leg,
            float length
    ) {
        leg.xScale = 1.0F;
        leg.yScale = length;
        leg.zScale = 1.0F;
    }
}

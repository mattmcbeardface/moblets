package com.moblets.mixin.client;

import com.moblets.client.BabyVariantRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerModel.class)
public abstract class VillagerModelMixin {

    @Unique
    private ModelPart babyMobs$head;

    @Unique
    private ModelPart babyMobs$body;

    @Unique
    private ModelPart babyMobs$arms;

    @Unique
    private ModelPart babyMobs$rightLeg;

    @Unique
    private ModelPart babyMobs$leftLeg;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void babyMobs$captureVillagerParts(
            ModelPart root,
            CallbackInfo ci
    ) {
        this.babyMobs$head =
                root.getChild("head");

        this.babyMobs$body =
                root.getChild("body");

        this.babyMobs$arms =
                root.getChild("arms");

        this.babyMobs$rightLeg =
                root.getChild("right_leg");

        this.babyMobs$leftLeg =
                root.getChild("left_leg");
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/VillagerRenderState;)V",
            at = @At("TAIL")
    )
    private void babyMobs$applyBabyWanderingTraderProportions(
            VillagerRenderState state,
            CallbackInfo ci
    ) {
        boolean baby =
                ((BabyVariantRenderState) state)
                        .babyMobs$isBabyVariant();

        float headScale =
                baby ? 1.25F : 1.0F;

        float bodyX =
                baby ? 1.10F : 1.0F;

        float bodyZ =
                baby ? 1.15F : 1.0F;

        float armLength =
                baby ? 0.90F : 1.0F;

        float legLength =
                baby ? 0.90F : 1.0F;

        /*
         * Hat and nose are children of the head, so both naturally follow
         * the enlarged juvenile head.
         */
        this.babyMobs$head.xScale = headScale;
        this.babyMobs$head.yScale = headScale;
        this.babyMobs$head.zScale = headScale;

        /*
         * The trader robe/jacket is a child of body.
         */
        this.babyMobs$body.xScale = bodyX;
        this.babyMobs$body.yScale = 1.0F;
        this.babyMobs$body.zScale = bodyZ;

        this.babyMobs$arms.xScale = 1.0F;
        this.babyMobs$arms.yScale = armLength;
        this.babyMobs$arms.zScale = 1.0F;

        this.babyMobs$rightLeg.xScale = 1.0F;
        this.babyMobs$rightLeg.yScale = legLength;
        this.babyMobs$rightLeg.zScale = 1.0F;

        this.babyMobs$leftLeg.xScale = 1.0F;
        this.babyMobs$leftLeg.yScale = legLength;
        this.babyMobs$leftLeg.zScale = 1.0F;
    }
}

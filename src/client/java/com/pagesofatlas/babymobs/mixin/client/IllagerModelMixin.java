package com.pagesofatlas.babymobs.mixin.client;

import com.pagesofatlas.babymobs.client.BabyVariantRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IllagerModel.class)
public abstract class IllagerModelMixin {

    @Unique
    private ModelPart babyMobs$head;

    @Unique
    private ModelPart babyMobs$body;

    @Unique
    private ModelPart babyMobs$crossedArms;

    @Unique
    private ModelPart babyMobs$rightArm;

    @Unique
    private ModelPart babyMobs$leftArm;

    @Unique
    private ModelPart babyMobs$rightLeg;

    @Unique
    private ModelPart babyMobs$leftLeg;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void babyMobs$captureParts(ModelPart root, CallbackInfo ci) {
        this.babyMobs$head = root.getChild("head");
        this.babyMobs$body = root.getChild("body");
        this.babyMobs$crossedArms = root.getChild("arms");
        this.babyMobs$rightArm = root.getChild("right_arm");
        this.babyMobs$leftArm = root.getChild("left_arm");
        this.babyMobs$rightLeg = root.getChild("right_leg");
        this.babyMobs$leftLeg = root.getChild("left_leg");
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/IllagerRenderState;)V",
            at = @At("TAIL")
    )
    private void babyMobs$applyBabyPillagerProportions(
            IllagerRenderState state,
            CallbackInfo ci
    ) {
        boolean baby =
                ((BabyVariantRenderState) state).babyMobs$isBabyVariant();

        float head = baby ? 1.25F : 1.0F;
        float bodyX = baby ? 1.10F : 1.0F;
        float bodyZ = baby ? 1.15F : 1.0F;
        float limbY = baby ? 0.90F : 1.0F;

        this.babyMobs$head.xScale = head;
        this.babyMobs$head.yScale = head;
        this.babyMobs$head.zScale = head;

        this.babyMobs$body.xScale = bodyX;
        this.babyMobs$body.yScale = 1.0F;
        this.babyMobs$body.zScale = bodyZ;

        this.babyMobs$crossedArms.xScale = 1.0F;
        this.babyMobs$crossedArms.yScale = limbY;
        this.babyMobs$crossedArms.zScale = 1.0F;

        this.babyMobs$rightArm.xScale = 1.0F;
        this.babyMobs$rightArm.yScale = limbY;
        this.babyMobs$rightArm.zScale = 1.0F;

        this.babyMobs$leftArm.xScale = 1.0F;
        this.babyMobs$leftArm.yScale = limbY;
        this.babyMobs$leftArm.zScale = 1.0F;

        this.babyMobs$rightLeg.xScale = 1.0F;
        this.babyMobs$rightLeg.yScale = limbY;
        this.babyMobs$rightLeg.zScale = 1.0F;

        this.babyMobs$leftLeg.xScale = 1.0F;
        this.babyMobs$leftLeg.yScale = limbY;
        this.babyMobs$leftLeg.zScale = 1.0F;
    }
}

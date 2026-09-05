package com.moblets.mixin.client;

import com.moblets.client.BabyVariantRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperModel.class)
public abstract class CreeperModelMixin {

    @Unique
    private ModelPart babyMobs$head;

    @Unique
    private ModelPart babyMobs$body;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void babyMobs$captureParts(ModelPart root, CallbackInfo ci) {
        this.babyMobs$head = root.getChild("head");
        this.babyMobs$body = root.getChild("body");
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/CreeperRenderState;)V",
            at = @At("TAIL")
    )
    private void babyMobs$applyBabyProportions(
            CreeperRenderState state,
            CallbackInfo ci
    ) {
        boolean baby =
                ((BabyVariantRenderState) state).babyMobs$isBabyVariant();

        float headScale = baby ? 1.20F : 1.0F;

        this.babyMobs$head.xScale = headScale;
        this.babyMobs$head.yScale = headScale;
        this.babyMobs$head.zScale = headScale;

        this.babyMobs$body.xScale = baby ? 1.15F : 1.0F;
        this.babyMobs$body.yScale = baby ? 1.05F : 1.0F;
        this.babyMobs$body.zScale = baby ? 1.30F : 1.0F;

        // Baby creeper legs deliberately remain normal.
    }
}

package com.pagesofatlas.babymobs.mixin.client;

import com.pagesofatlas.babymobs.client.BabyVariantRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitchModel.class)
public abstract class WitchModelMixin {

    @Unique
    private ModelPart babyMobs$head;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void babyMobs$captureParts(ModelPart root, CallbackInfo ci) {
        this.babyMobs$head = root.getChild("head");
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/WitchRenderState;)V",
            at = @At("TAIL")
    )
    private void babyMobs$applyBabyProportions(
            WitchRenderState state,
            CallbackInfo ci
    ) {
        boolean baby =
                ((BabyVariantRenderState) state).babyMobs$isBabyVariant();

        // Wider/deeper head and hat, but keep normal vertical height.
        this.babyMobs$head.xScale = baby ? 1.25F : 1.0F;
        this.babyMobs$head.yScale = 1.0F;
        this.babyMobs$head.zScale = baby ? 1.25F : 1.0F;
    }
}

package com.moblets.mixin.client;

import com.moblets.BabySnowGolems;
import com.moblets.client.BabyVariantRenderState;
import net.minecraft.client.renderer.entity.SnowGolemRenderer;
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowGolemRenderer.class)
public abstract class SnowGolemRendererMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/animal/golem/SnowGolem;Lnet/minecraft/client/renderer/entity/state/SnowGolemRenderState;F)V",
            at = @At("TAIL")
    )
    private void babyMobs$markBabySnowGolem(
            SnowGolem entity,
            SnowGolemRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        ((BabyVariantRenderState) state)
                .babyMobs$setBabyVariant(
                        BabySnowGolems.isBaby(entity)
                );
    }
}

package com.moblets.mixin.client;

import com.moblets.BabyCamelHusks;
import com.moblets.client.BabyVariantRenderState;
import net.minecraft.client.renderer.entity.CamelHuskRenderer;
import net.minecraft.client.renderer.entity.state.CamelRenderState;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CamelHuskRenderer.class)
public abstract class CamelHuskRendererMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/animal/camel/Camel;Lnet/minecraft/client/renderer/entity/state/CamelRenderState;F)V",
            at = @At("TAIL")
    )
    private void babyMobs$markBabyCamelHusk(
            Camel entity,
            CamelRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        boolean baby =
                entity instanceof CamelHusk camelHusk
                        && BabyCamelHusks.isBaby(camelHusk);

        ((BabyVariantRenderState) state)
                .babyMobs$setBabyVariant(baby);
    }
}

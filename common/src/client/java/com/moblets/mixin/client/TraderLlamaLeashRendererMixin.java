package com.moblets.mixin.client;

import com.moblets.BabyWanderingTraders;
import net.minecraft.client.renderer.entity.LlamaRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LlamaRenderer.class)
public abstract class TraderLlamaLeashRendererMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/animal/equine/Llama;Lnet/minecraft/client/renderer/entity/state/LlamaRenderState;F)V",
            at = @At("TAIL")
    )
    private void babyMobs$fixBabyTraderCaravanLeash(
            Llama entity,
            LlamaRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        if (!(entity instanceof TraderLlama)
                || !entity.isBaby()) {
            return;
        }

        Entity holder = entity.getLeashHolder();

        if (!(holder instanceof WanderingTrader trader)
                || !BabyWanderingTraders.isBaby(trader)) {
            return;
        }

        if (state.leashStates == null
                || state.leashStates.isEmpty()) {
            return;
        }

        /*
         * EntityRenderer has already constructed the leash state at this
         * point. Replace its final endpoints directly.
         */

        float llamaYaw =
                entity.getPreciseBodyRotation(partialTicks)
                        * (float) (Math.PI / 180.0);

        /*
         * Attach around the baby llama's lower-neck / upper-chest area.
         *
         * Z gives the lead a small forward offset instead of having it
         * emerge from the center of the body.
         */
        Vec3 llamaOffset =
                new Vec3(
                        0.0D,
                        entity.getBbHeight() * 0.72D,
                        entity.getBbWidth() * 0.30D
                ).yRot(-llamaYaw);

        Vec3 llamaPosition =
                entity.getPosition(partialTicks);

        /*
         * Put the holder endpoint around the baby trader's crossed-arm
         * area rather than using the generic adult-style rope position.
         */
        Vec3 traderPosition =
                trader.getPosition(partialTicks);

        Vec3 traderHoldPosition =
                traderPosition.add(
                        0.0D,
                        trader.getBbHeight() * 0.62D,
                        0.0D
                );

        EntityRenderState.LeashState leashState =
                state.leashStates.getFirst();

        leashState.offset =
                llamaOffset;

        leashState.start =
                llamaPosition.add(llamaOffset);

        leashState.end =
                traderHoldPosition;
    }
}

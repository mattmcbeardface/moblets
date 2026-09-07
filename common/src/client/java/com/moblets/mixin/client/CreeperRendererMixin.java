package com.moblets.mixin.client;

import com.moblets.BabyCreepers;
import com.moblets.client.BabyVariantRenderState;
import com.moblets.client.CreeperHelmetLayer;
import com.moblets.client.CreeperHelmetRenderState;

import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperRenderer.class)
public abstract class CreeperRendererMixin
        extends MobRenderer<
                Creeper,
                CreeperRenderState,
                CreeperModel
        > {

    protected CreeperRendererMixin(
            EntityRendererProvider.Context context,
            CreeperModel model,
            float shadowRadius
    ) {
        super(
                context,
                model,
                shadowRadius
        );
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void moblets$addHelmetLayer(
            EntityRendererProvider.Context context,
            CallbackInfo ci
    ) {
        this.addLayer(
                new CreeperHelmetLayer(
                        this,
                        context
                )
        );
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/monster/Creeper;Lnet/minecraft/client/renderer/entity/state/CreeperRenderState;F)V",
            at = @At("TAIL")
    )
    private void moblets$extractBabyCreeperState(
            Creeper entity,
            CreeperRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        boolean baby =
                BabyCreepers.isBaby(
                        entity
                );

        ((BabyVariantRenderState) state)
                .babyMobs$setBabyVariant(
                        baby
                );

        ((CreeperHelmetRenderState) state)
                .moblets$setHelmet(
                        baby
                                ? entity.getItemBySlot(
                                        EquipmentSlot.HEAD
                                )
                                : ItemStack.EMPTY
                );
    }
}

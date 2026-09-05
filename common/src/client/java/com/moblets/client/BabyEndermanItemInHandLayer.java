package com.moblets.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public final class BabyEndermanItemInHandLayer
        extends ItemInHandLayer<
                EndermanRenderState,
                EndermanModel<EndermanRenderState>
        > {

    /*
     * Vanilla ItemInHandLayer uses -10 model pixels here.
     *
     * That positions an item correctly at the end of a normal
     * humanoid arm, but an Enderman's arms are dramatically
     * longer. The Moblet's custom arm proportion is also 80%
     * of adult Enderman length.
     *
     * Because the arm's Y scale is already part of the current
     * pose transform, -30 becomes roughly 24 effective model
     * pixels down the Moblet arm.
     */
    private static final float HAND_REACH =
            -30.0F;

    public BabyEndermanItemInHandLayer(
            RenderLayerParent<
                    EndermanRenderState,
                    EndermanModel<EndermanRenderState>
            > renderer
    ) {
        super(renderer);
    }

    @Override
    protected void submitArmWithItem(
            EndermanRenderState state,
            ItemStackRenderState item,
            ItemStack itemStack,
            HumanoidArm arm,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords
    ) {
        /*
         * This layer exists only for Moblet Endermen.
         *
         * Adult Endermen retain vanilla rendering behavior.
         */
        if (!((BabyVariantRenderState) state)
                .babyMobs$isBabyVariant()
                || item.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        /*
         * Begin at the Enderman's shoulder and follow all of
         * the arm's current animation/scale transforms.
         */
        this.getParentModel()
                .translateToHand(
                        state,
                        arm,
                        poseStack
                );

        /*
         * Same basic item orientation as vanilla's
         * ItemInHandLayer.
         */
        poseStack.mulPose(
                Axis.XP.rotationDegrees(-90.0F)
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(180.0F)
        );

        boolean leftHand =
                arm == HumanoidArm.LEFT;

        /*
         * Small sideways/forward offsets remain vanilla-like,
         * but move much farther down the Enderman arm.
         */
        poseStack.translate(
                (leftHand ? -1.0F : 1.0F) / 16.0F,
                2.0F / 16.0F,
                HAND_REACH / 16.0F
        );

        item.submit(
                poseStack,
                submitNodeCollector,
                lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor
        );

        poseStack.popPose();
    }
}

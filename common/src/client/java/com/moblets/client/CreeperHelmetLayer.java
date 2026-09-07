package com.moblets.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public final class CreeperHelmetLayer
        extends RenderLayer<
                CreeperRenderState,
                CreeperModel
        > {

    private final CreeperHelmetModel helmetModel;
    private final EquipmentLayerRenderer equipmentRenderer;

    public CreeperHelmetLayer(
            RenderLayerParent<
                    CreeperRenderState,
                    CreeperModel
            > parent,
            EntityRendererProvider.Context context
    ) {
        super(parent);

        this.helmetModel =
                new CreeperHelmetModel(
                        context.bakeLayer(
                                ModelLayers.SKELETON_ARMOR.head()
                        )
                );

        this.equipmentRenderer =
                context.getEquipmentRenderer();
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            CreeperRenderState creeperState,
            float yRot,
            float xRot
    ) {
        if (!((BabyVariantRenderState) creeperState)
                .babyMobs$isBabyVariant()) {
            return;
        }

        ItemStack helmet =
                ((CreeperHelmetRenderState) creeperState)
                        .moblets$getHelmet();

        if (helmet.isEmpty()) {
            return;
        }

        Equippable equippable =
                helmet.get(
                        DataComponents.EQUIPPABLE
                );

        if (equippable == null
                || equippable.slot()
                != EquipmentSlot.HEAD
                || equippable.assetId().isEmpty()) {
            return;
        }

        HumanoidRenderState armorState =
                new HumanoidRenderState();

        armorState.outlineColor =
                creeperState.outlineColor;

        ModelPart creeperRoot =
                this.getParentModel().root();

        /*
         * LivingEntityRenderer calls the parent model's setupAnim
         * immediately before submitting its layers. EMF injects
         * its custom animation at that point, so these are the
         * final Patrix transforms used for this frame.
         */
        poseStack.pushPose();

        if (!EmfCreeperHeadCompat.translateToVisibleHead(
                creeperRoot,
                poseStack
        )) {
            if (!creeperRoot.hasChild("head")) {
                poseStack.popPose();
                return;
            }

            creeperRoot.translateAndRotate(
                    poseStack
            );

            creeperRoot.getChild("head")
                    .translateAndRotate(
                            poseStack
                    );
        }

        /*
         * EquipmentLayerRenderer snapshots the current matrix in
         * its deferred model submit. Its later setupAnim call can
         * reset only helmetModel's local parts, not this captured
         * EMF attachment transform.
         */
        this.equipmentRenderer.renderLayers(
                EquipmentClientInfo.LayerType.HUMANOID,
                equippable.assetId().orElseThrow(),
                this.helmetModel,
                armorState,
                helmet,
                poseStack,
                collector,
                packedLight,
                creeperState.outlineColor
        );

        poseStack.popPose();
    }
}

package com.moblets.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public final class CreeperHelmetModel
        extends HumanoidModel<HumanoidRenderState> {

    public CreeperHelmetModel(
            ModelPart root
    ) {
        super(root);
    }

    @Override
    public void setupAnim(
            HumanoidRenderState state
    ) {
        this.resetPose();

        for (ModelPart part : this.allParts()) {
            part.visible = false;
        }

        this.root().visible = true;
        this.head.visible = true;
        this.hat.visible = true;
    }
}

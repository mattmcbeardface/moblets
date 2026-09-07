package com.moblets.client;

import java.lang.reflect.Field;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelPart;

public final class EmfCreeperHeadCompat {

    private static final Class<?> EMF_ROOT_PART;
    private static final Class<?> EMF_CUSTOM_PART;
    private static final Field CUSTOM_ID;

    static {
        Class<?> rootPart = null;
        Class<?> customPart = null;
        Field customId = null;

        try {
            rootPart =
                    Class.forName(
                            "traben.entity_model_features.models.parts.EMFModelPartRoot"
                    );

            customPart =
                    Class.forName(
                            "traben.entity_model_features.models.parts.EMFModelPartCustom"
                    );

            customId =
                    customPart.getField(
                            "id"
                    );

        } catch (ReflectiveOperationException
                | LinkageError ignored) {

            rootPart = null;
            customPart = null;
            customId = null;
        }

        EMF_ROOT_PART =
                rootPart;

        EMF_CUSTOM_PART =
                customPart;

        CUSTOM_ID =
                customId;
    }

    private EmfCreeperHeadCompat() {
    }

    public static boolean translateToVisibleHead(
            ModelPart root,
            PoseStack poseStack
    ) {
        if (EMF_ROOT_PART == null
                || EMF_CUSTOM_PART == null
                || root == null
                || !EMF_ROOT_PART.isInstance(root)
                || !root.hasChild("body")) {
            return false;
        }

        ModelPart body =
                root.getChild("body");

        try {
            ModelPart visibleBody =
                    findDirectCustomChild(
                            body,
                            "body"
                    );

            ModelPart visibleHead =
                    findDirectCustomChild(
                            visibleBody,
                            "head2"
                    );

            if (visibleHead == null) {
                return false;
            }

            return translatePartChain(
                    poseStack,
                    root,
                    body,
                    visibleBody,
                    visibleHead
            );

        } catch (IllegalAccessException ignored) {
        }

        return false;
    }

    private static boolean translatePartChain(
            PoseStack poseStack,
            ModelPart... parts
    ) {
        for (ModelPart part : parts) {
            part.translateAndRotate(
                    poseStack
            );
        }

        return true;
    }

    private static ModelPart findDirectCustomChild(
            ModelPart parent,
            String jemId
    ) throws IllegalAccessException {
        if (parent == null) {
            return null;
        }

        String emfId =
                "EMF_" + jemId;

        if (parent.hasChild(emfId)) {
            ModelPart child =
                    parent.getChild(emfId);

            if (hasId(
                    child,
                    jemId,
                    emfId
            )) {
                return child;
            }
        }

        if (parent.hasChild(jemId)) {
            ModelPart child =
                    parent.getChild(jemId);

            if (hasId(
                    child,
                    jemId,
                    emfId
            )) {
                return child;
            }
        }

        return null;
    }

    private static boolean hasId(
            ModelPart part,
            String jemId,
            String emfId
    ) throws IllegalAccessException {
        if (!EMF_CUSTOM_PART.isInstance(part)) {
            return false;
        }

        Object id =
                CUSTOM_ID.get(
                        part
                );

        return jemId.equals(id)
                || emfId.equals(id);
    }
}

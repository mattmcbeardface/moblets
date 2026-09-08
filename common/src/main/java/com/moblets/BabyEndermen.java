package com.moblets;

import com.moblets.mixin.EnderManDataAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;

public final class BabyEndermen {
    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_enderman_scale");

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_enderman_speed");

    private static final Identifier BABY_DAMAGE_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_enderman_damage");

    private static final AttributeModifier BABY_SCALE =
            new AttributeModifier(
                    BABY_SCALE_ID,
                    -0.55D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private static final AttributeModifier BABY_SPEED =
            new AttributeModifier(
                    BABY_SPEED_ID,
                    0.25D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private static final AttributeModifier BABY_DAMAGE =
            new AttributeModifier(
                    BABY_DAMAGE_ID,
                    -0.65D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private BabyEndermen() {
    }

    public static void applyBaby(EnderMan enderman) {
        AttributeInstance scale = enderman.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.addOrReplacePermanentModifier(BABY_SCALE);
        }

        AttributeInstance speed = enderman.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addOrReplacePermanentModifier(BABY_SPEED);
        }

        AttributeInstance damage = enderman.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.addOrReplacePermanentModifier(BABY_DAMAGE);
        }
    }

    public static boolean isBaby(EnderMan enderman) {
        AttributeInstance scale = enderman.getAttribute(Attributes.SCALE);

        return scale != null && scale.hasModifier(BABY_SCALE_ID);
    }

    public static void setCreepy(
            EnderMan enderman,
            boolean creepy
    ) {
        enderman.getEntityData().set(
                EnderManDataAccessor.babyMobs$getDataCreepy(),
                creepy
        );
    }
}

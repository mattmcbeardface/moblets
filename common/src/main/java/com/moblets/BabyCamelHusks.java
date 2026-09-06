package com.moblets;

import com.moblets.config.MobletsConfig;
import com.moblets.registry.EncounterRegistry;

import net.minecraft.server.MinecraftServer;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

public final class BabyCamelHusks {
    static final float NATURAL_BABY_CHANCE = 0.08F;

    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(
                    Moblets.MOD_ID,
                    "baby_camel_husk_scale"
            );

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(
                    Moblets.MOD_ID,
                    "baby_camel_husk_speed"
            );

    private static final AttributeModifier BABY_SCALE =
            new AttributeModifier(
                    BABY_SCALE_ID,
                    -0.50D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    private static final AttributeModifier BABY_SPEED =
            new AttributeModifier(
                    BABY_SPEED_ID,
                    0.20D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    /*
     * Keep baby Camel Husks tracked so their normal Husk + Parched
     * passengers are also forced into baby variants whenever they
     * are attached.
     *
     * This deliberately does not replace Mojang's rider spawning.
     * Vanilla remains responsible for building the encounter.
     */
    private static final Set<CamelHusk> TRACKED_BABIES =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private BabyCamelHusks() {
    }

    public static void onEntityLoaded(Entity entity) {
        if (entity instanceof CamelHusk camelHusk
                && isBaby(camelHusk)) {
            TRACKED_BABIES.add(camelHusk);
        }
    }

    public static void onServerTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            maintainBabyRiders(level);
        }
    }

    public static void applyBaby(CamelHusk camelHusk) {
        AttributeInstance scale =
                camelHusk.getAttribute(Attributes.SCALE);

        if (scale != null) {
            scale.addOrReplacePermanentModifier(BABY_SCALE);
        }

        AttributeInstance speed =
                camelHusk.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed != null) {
            speed.addOrReplacePermanentModifier(BABY_SPEED);
        }

        TRACKED_BABIES.add(camelHusk);

        /*
         * In case passengers are already present when this method runs.
         */
        if (MobletsConfig.encounterEnabled(
                EncounterRegistry.CAMEL_HUSK_RIDERS
        )) {
            applyBabyToPassengers(camelHusk);
        }
    }

    public static boolean isBaby(CamelHusk camelHusk) {
        AttributeInstance scale =
                camelHusk.getAttribute(Attributes.SCALE);

        return scale != null
                && scale.hasModifier(BABY_SCALE_ID);
    }

    private static void maintainBabyRiders(ServerLevel level) {
        CamelHusk[] snapshot =
                TRACKED_BABIES.toArray(CamelHusk[]::new);

        for (CamelHusk camelHusk : snapshot) {
            if (camelHusk.isRemoved()) {
                TRACKED_BABIES.remove(camelHusk);
                continue;
            }

            if (camelHusk.level() != level) {
                continue;
            }

            if (!isBaby(camelHusk)) {
                TRACKED_BABIES.remove(camelHusk);
                continue;
            }

            if (MobletsConfig.encounterEnabled(
                    EncounterRegistry.CAMEL_HUSK_RIDERS
            )) {
                applyBabyToPassengers(camelHusk);
            }
        }
    }

    private static void applyBabyToPassengers(
            CamelHusk camelHusk
    ) {
        for (Entity passenger : camelHusk.getPassengers()) {

            /*
             * Husk already has a genuine vanilla baby state, so use it.
             * This gives us the normal baby-zombie proportions and
             * behavior rather than inventing another custom Husk marker.
             */
            if (passenger instanceof Husk husk) {
                if (!husk.isBaby()) {
                    husk.setBaby(true);
                }

                continue;
            }

            /*
             * Parched is part of the AbstractSkeleton family, for which
             * Moblets already has a complete custom baby implementation.
             */
            if (passenger instanceof AbstractSkeleton skeleton
                    && passenger.getType() == EntityTypes.PARCHED) {

                if (!BabySkeletons.isBaby(skeleton)) {
                    BabySkeletons.applyBaby(skeleton);
                }
            }
        }
    }
}

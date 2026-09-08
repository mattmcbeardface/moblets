package com.moblets;

import com.moblets.config.MobletsConfig;
import com.moblets.registry.EncounterRegistry;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.SnowGolem;

public final class BabySnowGolems {
    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(
                    Moblets.MOD_ID,
                    "baby_snow_golem_scale"
            );

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(
                    Moblets.MOD_ID,
                    "baby_snow_golem_speed"
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
     * Adult Snow Golems that are currently loaded.
     *
     * We continually make sure a loaded adult has at least one nearby
     * Moblets Snow Golem. Several adults in the same area share the
     * same baby population instead of each creating their own.
     */
    private static final Set<SnowGolem> WATCHED_ADULTS =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private BabySnowGolems() {
    }

    public static void onEntityLoaded(Entity entity) {
        if (entity instanceof SnowGolem snowGolem) {
            WATCHED_ADULTS.add(snowGolem);
        }
    }

    public static void onServerTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            maintainBabyPopulation(level);
        }
    }

    public static void applyBaby(SnowGolem snowGolem) {
        AttributeInstance scale =
                snowGolem.getAttribute(Attributes.SCALE);

        if (scale != null) {
            scale.addOrReplacePermanentModifier(BABY_SCALE);
        }

        AttributeInstance speed =
                snowGolem.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed != null) {
            speed.addOrReplacePermanentModifier(BABY_SPEED);
        }
    }

    public static boolean isBaby(SnowGolem snowGolem) {
        AttributeInstance scale =
                snowGolem.getAttribute(Attributes.SCALE);

        return scale != null
                && scale.hasModifier(BABY_SCALE_ID);
    }

    private static void maintainBabyPopulation(ServerLevel level) {
        SnowGolem[] snapshot =
                WATCHED_ADULTS.toArray(SnowGolem[]::new);

        for (SnowGolem adult : snapshot) {
            if (adult.isRemoved()) {
                WATCHED_ADULTS.remove(adult);
                continue;
            }

            if (adult.level() != level) {
                continue;
            }

            /*
             * A baby may briefly enter WATCHED_ADULTS because ENTITY_LOAD
             * can fire before applyBaby() is called on a newly created one.
             * Remove it once its permanent baby marker is present.
             */
            if (isBaby(adult)) {
                WATCHED_ADULTS.remove(adult);
                continue;
            }

            if (!MobletsConfig.encounterEnabled(
                    EncounterRegistry.SNOW_GOLEM_COMPANION
            )) {
                continue;
            }

            if (!hasBabyNearby(level, adult)) {
                spawnBabyNear(level, adult);
            }
        }
    }

    private static boolean hasBabyNearby(
            ServerLevel level,
            SnowGolem adult
    ) {
        return !level.getEntitiesOfClass(
                        SnowGolem.class,
                        adult.getBoundingBox().inflate(
                                32.0D,
                                16.0D,
                                32.0D
                        )
                )
                .stream()
                .filter(golem -> golem != adult)
                .filter(BabySnowGolems::isBaby)
                .toList()
                .isEmpty();
    }

    private static void spawnBabyNear(
            ServerLevel level,
            SnowGolem adult
    ) {
        BlockPos origin = adult.blockPosition();

        /*
         * Try several nearby positions. EntityType.spawn() can return null,
         * so this also gives us graceful fallbacks if one candidate cannot
         * be used.
         */
        BlockPos[] candidates = {
                origin.offset(1, 0, 0),
                origin.offset(-1, 0, 0),
                origin.offset(0, 0, 1),
                origin.offset(0, 0, -1),
                origin.offset(1, 0, 1),
                origin.offset(-1, 0, 1),
                origin.offset(1, 0, -1),
                origin.offset(-1, 0, -1),
                origin
        };

        for (BlockPos candidate : candidates) {
            SnowGolem baby = EntityType.SNOW_GOLEM.spawn(
                    level,
                    candidate,
                    EntitySpawnReason.EVENT
            );

            if (baby != null) {
                applyBaby(baby);
                return;
            }
        }
    }
}

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
import net.minecraft.world.entity.animal.golem.IronGolem;

public final class BabyIronGolems {
    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(
                    Moblets.MOD_ID,
                    "baby_iron_golem_scale"
            );

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(
                    Moblets.MOD_ID,
                    "baby_iron_golem_speed"
            );

    private static final Identifier BABY_DAMAGE_ID =
            Identifier.fromNamespaceAndPath(
                    Moblets.MOD_ID,
                    "baby_iron_golem_damage"
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

    private static final AttributeModifier BABY_DAMAGE =
            new AttributeModifier(
                    BABY_DAMAGE_ID,
                    -0.50D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    /*
     * Loaded Iron Golems are watched so that village-created adults
     * maintain at least one nearby baby.
     *
     * Player-created adults do not participate.
     */
    private static final Set<IronGolem> WATCHED_GOLEMS =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private BabyIronGolems() {
    }

    public static void onEntityLoaded(Entity entity) {
        if (entity instanceof IronGolem ironGolem) {
            WATCHED_GOLEMS.add(ironGolem);
        }
    }

    public static void onServerTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            maintainBabyPopulation(level);
        }
    }

    public static void applyBaby(IronGolem ironGolem) {
        AttributeInstance scale =
                ironGolem.getAttribute(Attributes.SCALE);

        if (scale != null) {
            scale.addOrReplacePermanentModifier(BABY_SCALE);
        }

        AttributeInstance speed =
                ironGolem.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed != null) {
            speed.addOrReplacePermanentModifier(BABY_SPEED);
        }

        AttributeInstance damage =
                ironGolem.getAttribute(Attributes.ATTACK_DAMAGE);

        if (damage != null) {
            damage.addOrReplacePermanentModifier(BABY_DAMAGE);
        }
    }

    public static boolean isBaby(IronGolem ironGolem) {
        AttributeInstance scale =
                ironGolem.getAttribute(Attributes.SCALE);

        return scale != null
                && scale.hasModifier(BABY_SCALE_ID);
    }

    private static void maintainBabyPopulation(ServerLevel level) {
        IronGolem[] snapshot =
                WATCHED_GOLEMS.toArray(IronGolem[]::new);

        for (IronGolem adult : snapshot) {
            if (adult.isRemoved()) {
                WATCHED_GOLEMS.remove(adult);
                continue;
            }

            if (adult.level() != level) {
                continue;
            }

            if (isBaby(adult)) {
                WATCHED_GOLEMS.remove(adult);
                continue;
            }

            /*
             * Player-built golems never create babies.
             */
            if (adult.isPlayerCreated()) {
                WATCHED_GOLEMS.remove(adult);
                continue;
            }

            /*
             * Only village golems qualify.
             */
            if (!level.isVillage(adult.blockPosition())) {
                continue;
            }

            if (!MobletsConfig.encounterEnabled(
                    EncounterRegistry.IRON_GOLEM_COMPANION
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
            IronGolem adult
    ) {
        return level.getEntitiesOfClass(
                        IronGolem.class,
                        adult.getBoundingBox().inflate(
                                48.0D,
                                24.0D,
                                48.0D
                        )
                )
                .stream()
                .anyMatch(golem ->
                        golem != adult
                                && BabyIronGolems.isBaby(golem)
                );
    }

    private static void spawnBabyNear(
            ServerLevel level,
            IronGolem adult
    ) {
        BlockPos origin = adult.blockPosition();

        BlockPos[] candidates = {
                origin.offset(2, 0, 0),
                origin.offset(-2, 0, 0),
                origin.offset(0, 0, 2),
                origin.offset(0, 0, -2),
                origin.offset(2, 0, 2),
                origin.offset(-2, 0, 2),
                origin.offset(2, 0, -2),
                origin.offset(-2, 0, -2),
                origin
        };

        for (BlockPos candidate : candidates) {
            IronGolem baby = EntityType.IRON_GOLEM.spawn(
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

package com.moblets;

import com.moblets.config.MobletsConfig;
import com.moblets.registry.EncounterRegistry;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public final class BabyPillagers {
    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_pillager_scale");

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_pillager_speed");

    private static final Identifier BABY_DAMAGE_ID =
            Identifier.fromNamespaceAndPath(Moblets.MOD_ID, "baby_pillager_damage");

    private static final AttributeModifier BABY_SCALE =
            new AttributeModifier(
                    BABY_SCALE_ID,
                    -0.50D,
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
                    -0.50D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    /*
     * Pillagers are queued until END_SERVER_TICK.
     *
     * This is intentional. During chunk loading we want all of an
     * outpost's already-existing baby Pillagers to be loaded before
     * deciding whether the outpost needs another one.
     */
    private static final Set<Pillager> PENDING =
            java.util.Collections.newSetFromMap(new IdentityHashMap<>());

    private BabyPillagers() {
    }

    public static void onEntityLoaded(Entity entity) {
        if (entity instanceof Pillager pillager) {
            PENDING.add(pillager);
        }
    }

    public static void onServerTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            processPending(level);
        }
    }

    public static void applyBaby(Pillager pillager) {
        AttributeInstance scale = pillager.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.addOrReplacePermanentModifier(BABY_SCALE);
        }

        AttributeInstance speed = pillager.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addOrReplacePermanentModifier(BABY_SPEED);
        }

        AttributeInstance damage = pillager.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.addOrReplacePermanentModifier(BABY_DAMAGE);
        }
    }

    public static boolean isBaby(Pillager pillager) {
        AttributeInstance scale = pillager.getAttribute(Attributes.SCALE);
        return scale != null && scale.hasModifier(BABY_SCALE_ID);
    }

    private static void processPending(ServerLevel level) {
        List<Pillager> currentWorld = new ArrayList<>();

        for (Pillager pillager : PENDING) {
            if (pillager.level() == level) {
                currentWorld.add(pillager);
            }
        }

        PENDING.removeAll(currentWorld);

        if (!MobletsConfig.encounterEnabled(
                EncounterRegistry.PILLAGER_OUTPOST
        )) {
            return;
        }

        for (Pillager pillager : currentWorld) {
            if (pillager.isRemoved()) {
                continue;
            }

            ensureOutpostBabyPopulation(level, pillager);
        }
    }

    private static void ensureOutpostBabyPopulation(
            ServerLevel level,
            Pillager triggeringPillager
    ) {
        StructureStart outpost = getOutpostAt(level, triggeringPillager);

        if (!outpost.isValid()) {
            return;
        }

        /*
         * Every outpost deterministically gets either one or two babies.
         *
         * Using the structure's start chunk makes the target stable across
         * server restarts instead of rerolling every time the area loads.
         */
        int targetBabyCount =
                1 + Math.floorMod(
                        outpost.getChunkPos().x() * 31 + outpost.getChunkPos().z(),
                        2
                );

        List<Pillager> sameOutpost = level
                .getEntitiesOfClass(
                        Pillager.class,
                        triggeringPillager.getBoundingBox().inflate(96.0, 48.0, 96.0)
                )
                .stream()
                .filter(pillager -> belongsToSameOutpost(level, pillager, outpost))
                .toList();

        long currentBabies = sameOutpost.stream()
                .filter(BabyPillagers::isBaby)
                .count();

        if (currentBabies >= targetBabyCount) {
            return;
        }

        int babiesNeeded = targetBabyCount - (int) currentBabies;

        for (Pillager pillager : sameOutpost) {
            if (babiesNeeded <= 0) {
                break;
            }

            if (isBaby(pillager)) {
                continue;
            }

            applyBaby(pillager);
            babiesNeeded--;
        }
    }

    private static boolean belongsToSameOutpost(
            ServerLevel level,
            Pillager pillager,
            StructureStart expected
    ) {
        StructureStart actual = getOutpostAt(level, pillager);

        return actual.isValid()
                && actual.getChunkPos().equals(expected.getChunkPos());
    }

    private static StructureStart getOutpostAt(
            ServerLevel level,
            Pillager pillager
    ) {
        return level.structureManager().getStructureWithPieceAt(
                pillager.blockPosition(),
                structure -> structure.is(BuiltinStructures.PILLAGER_OUTPOST)
        );
    }
}

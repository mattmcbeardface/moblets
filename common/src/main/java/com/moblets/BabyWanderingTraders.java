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
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;

public final class BabyWanderingTraders {
    private static final Identifier BABY_SCALE_ID =
            Identifier.fromNamespaceAndPath(
                    Moblets.MOD_ID,
                    "baby_wandering_trader_scale"
            );

    private static final Identifier BABY_SPEED_ID =
            Identifier.fromNamespaceAndPath(
                    Moblets.MOD_ID,
                    "baby_wandering_trader_speed"
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
     * Permanent marker placed on an adult once this caravan has
     * received its ONE baby trader companion.
     *
     * The baby is now allowed to leave the caravan while pursuing
     * customers, so proximity can no longer determine whether the
     * adult needs another baby.
     */
    private static final String BABY_COMPANION_ASSIGNED_TAG =
            "baby_mobs:baby_trader_companion_assigned";

    /*
     * A vanilla wandering-trader caravan has a positive despawn timer.
     *
     * We watch loaded traders and ensure those caravans have one custom
     * baby trader companion. The custom marker deliberately does NOT use
     * WanderingTrader.isBaby(), because vanilla baby traders cannot trade
     * and WanderingTrader also forces its vanilla age back to adult when
     * loaded from disk.
     */
    private static final Set<WanderingTrader> WATCHED_TRADERS =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private BabyWanderingTraders() {
    }

    public static void onEntityLoaded(Entity entity) {
        if (entity instanceof WanderingTrader trader) {
            WATCHED_TRADERS.add(trader);
        }
    }

    public static void onServerTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            maintainCaravans(level);
        }
    }

    public static void applyBaby(WanderingTrader trader) {
        AttributeInstance scale =
                trader.getAttribute(Attributes.SCALE);

        if (scale != null) {
            scale.addOrReplacePermanentModifier(BABY_SCALE);
        }

        AttributeInstance speed =
                trader.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed != null) {
            speed.addOrReplacePermanentModifier(BABY_SPEED);
        }
    }

    public static boolean isBaby(WanderingTrader trader) {
        AttributeInstance scale =
                trader.getAttribute(Attributes.SCALE);

        return scale != null
                && scale.hasModifier(BABY_SCALE_ID);
    }

    private static void maintainCaravans(ServerLevel level) {
        WanderingTrader[] snapshot =
                WATCHED_TRADERS.toArray(WanderingTrader[]::new);

        for (WanderingTrader adult : snapshot) {
            if (adult.isRemoved()) {
                WATCHED_TRADERS.remove(adult);
                continue;
            }

            if (adult.level() != level) {
                continue;
            }

            /*
             * Baby traders themselves get added by ENTITY_LOAD while they
             * are being created. Once their marker is present, remove them
             * from the adult watch list.
             */
            if (isBaby(adult)) {
                WATCHED_TRADERS.remove(adult);
                continue;
            }

            /*
             * Vanilla wandering-trader caravans receive a despawn timer.
             * Command-created permanent traders normally have zero and are
             * therefore left alone.
             */
            if (adult.getDespawnDelay() <= 0) {
                continue;
            }

            if (!MobletsConfig.encounterEnabled(
                    EncounterRegistry.WANDERING_TRADER_CARAVAN
            )) {
                continue;
            }

            /*
             * One baby companion per adult trader lifetime.
             *
             * The salesman Moblet is allowed to wander far away while
             * pursuing a customer. Once this adult has been assigned a
             * baby, distance from that baby must never cause another one
             * to spawn.
             */
            if (adult.entityTags()
                    .contains(
                            BABY_COMPANION_ASSIGNED_TAG
                    )) {
                continue;
            }

            /*
             * Migration/compatibility case:
             *
             * If this adult already has a nearby baby from a world saved
             * before this marker existed, adopt that relationship instead
             * of spawning a duplicate.
             */
            if (hasBabyTraderNearby(
                    level,
                    adult
            )) {
                adult.addTag(
                        BABY_COMPANION_ASSIGNED_TAG
                );

                continue;
            }

            if (spawnBabyCaravan(
                    level,
                    adult
            )) {
                adult.addTag(
                        BABY_COMPANION_ASSIGNED_TAG
                );
            }
        }
    }

    private static boolean hasBabyTraderNearby(
            ServerLevel level,
            WanderingTrader adult
    ) {
        return level.getEntitiesOfClass(
                        WanderingTrader.class,
                        adult.getBoundingBox().inflate(
                                32.0D,
                                16.0D,
                                32.0D
                        )
                )
                .stream()
                .anyMatch(trader ->
                        trader != adult
                                && BabyWanderingTraders.isBaby(trader)
                );
    }

    private static boolean spawnBabyCaravan(
            ServerLevel level,
            WanderingTrader adult
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
            WanderingTrader baby =
                    EntityType.WANDERING_TRADER.spawn(
                            level,
                            candidate,
                            EntitySpawnReason.EVENT
                    );

            if (baby == null) {
                continue;
            }

            applyBaby(baby);

            /*
             * Match the adult caravan's remaining lifetime.
             */
            baby.setDespawnDelay(
                    adult.getDespawnDelay()
            );

            /*
             * Keep the baby caravan centered in the same general area.
             */
            baby.setWanderTarget(
                    adult.blockPosition()
            );

            baby.setHomeTo(
                    adult.blockPosition(),
                    16
            );

            spawnBabyLlamas(level, baby);

            return true;
        }

        /*
         * No valid spawn position this tick. Leave the adult unmarked
         * so the server may try again later.
         */
        return false;
    }

    private static void spawnBabyLlamas(
            ServerLevel level,
            WanderingTrader babyTrader
    ) {
        BlockPos origin =
                babyTrader.blockPosition();

        BlockPos[] candidates = {
                origin.offset(1, 0, 0),
                origin.offset(-1, 0, 0),
                origin.offset(0, 0, 1),
                origin.offset(0, 0, -1),
                origin.offset(1, 0, 1),
                origin.offset(-1, 0, 1),
                origin.offset(1, 0, -1),
                origin.offset(-1, 0, -1)
        };

        int spawned = 0;

        for (BlockPos candidate : candidates) {
            if (spawned >= 2) {
                break;
            }

            TraderLlama llama =
                    EntityType.TRADER_LLAMA.spawn(
                            level,
                            candidate,
                            EntitySpawnReason.EVENT
                    );

            if (llama == null) {
                continue;
            }

            /*
             * TraderLlama.finalizeSpawn(EVENT) explicitly sets age to
             * adult, so we set the genuine vanilla baby state AFTER spawn.
             */
            llama.setBaby(true);

            llama.setDespawnDelay(
                    babyTrader.getDespawnDelay()
            );

            llama.setLeashedTo(
                    babyTrader,
                    true
            );

            spawned++;
        }
    }
}

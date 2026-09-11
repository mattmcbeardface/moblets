package com.moblets.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.moblets.BabyCamelHusks;
import com.moblets.BabyCreepers;
import com.moblets.BabyEndermen;
import com.moblets.BabyIronGolems;
import com.moblets.BabyPillagers;
import com.moblets.BabySkeletons;
import com.moblets.BabySnowGolems;
import com.moblets.BabyWanderingTraders;
import com.moblets.BabyWitches;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;

import static com.moblets.registry.BalanceStat.ACCURACY;
import static com.moblets.registry.BalanceStat.DAMAGE;
import static com.moblets.registry.BalanceStat.HEALTH;
import static com.moblets.registry.BalanceStat.MOVEMENT_SPEED;

public final class MobletRegistry {
    private static final float DEFAULT_RANDOM_SPAWN_CHANCE = 0.08F;

    private static final List<MobletDefinition> DEFINITIONS =
            new ArrayList<>();

    private static final Map<EntityType<?>, MobletDefinition> BY_ENTITY_TYPE =
            new IdentityHashMap<>();

    public static final MobletDefinition SKELETON = register(
            MobletDefinition.builder(
                            "skeleton",
                            "Skeleton",
                            EntityTypes.SKELETON,
                            mob -> BabySkeletons.applyBaby(
                                    (AbstractSkeleton) mob
                            ),
                            mob -> BabySkeletons.isBaby(
                                    (AbstractSkeleton) mob
                            )
                    )
                    .randomSpawn(DEFAULT_RANDOM_SPAWN_CHANCE)
                    .tameable(0.25F)
                    .advanced(
                            HEALTH,
                            DAMAGE,
                            MOVEMENT_SPEED,
                            ACCURACY
                    )
                    .build()
    );

    public static final MobletDefinition STRAY = register(
            MobletDefinition.builder(
                            "stray",
                            "Stray",
                            EntityTypes.STRAY,
                            mob -> BabySkeletons.applyBaby(
                                    (AbstractSkeleton) mob
                            ),
                            mob -> BabySkeletons.isBaby(
                                    (AbstractSkeleton) mob
                            )
                    )
                    .randomSpawn(DEFAULT_RANDOM_SPAWN_CHANCE)
                    .tameable(0.40F)
                    .advanced(
                            HEALTH,
                            DAMAGE,
                            MOVEMENT_SPEED,
                            ACCURACY
                    )
                    .build()
    );

    public static final MobletDefinition BOGGED = register(
            MobletDefinition.builder(
                            "bogged",
                            "Bogged",
                            EntityTypes.BOGGED,
                            mob -> BabySkeletons.applyBaby(
                                    (AbstractSkeleton) mob
                            ),
                            mob -> BabySkeletons.isBaby(
                                    (AbstractSkeleton) mob
                            )
                    )
                    .randomSpawn(DEFAULT_RANDOM_SPAWN_CHANCE)
                    .tameable(0.25F)
                    .advanced(
                            HEALTH,
                            DAMAGE,
                            MOVEMENT_SPEED,
                            ACCURACY
                    )
                    .build()
    );

    public static final MobletDefinition PARCHED = register(
            MobletDefinition.builder(
                            "parched",
                            "Parched",
                            EntityTypes.PARCHED,
                            mob -> BabySkeletons.applyBaby(
                                    (AbstractSkeleton) mob
                            ),
                            mob -> BabySkeletons.isBaby(
                                    (AbstractSkeleton) mob
                            )
                    )
                    .randomSpawn(DEFAULT_RANDOM_SPAWN_CHANCE)
                    .tameable(0.50F)
                    .advanced(
                            HEALTH,
                            DAMAGE,
                            MOVEMENT_SPEED,
                            ACCURACY
                    )
                    .build()
    );

    public static final MobletDefinition WITHER_SKELETON = register(
            MobletDefinition.builder(
                            "wither_skeleton",
                            "Wither Skeleton",
                            EntityTypes.WITHER_SKELETON,
                            mob -> BabySkeletons.applyBaby(
                                    (AbstractSkeleton) mob
                            ),
                            mob -> BabySkeletons.isBaby(
                                    (AbstractSkeleton) mob
                            )
                    )
                    .randomSpawn(DEFAULT_RANDOM_SPAWN_CHANCE)
                    .tameable(0.20F)
                    .advanced(
                            HEALTH,
                            DAMAGE,
                            MOVEMENT_SPEED
                    )
                    .build()
    );

    public static final MobletDefinition CREEPER = register(
            MobletDefinition.builder(
                            "creeper",
                            "Creeper",
                            EntityTypes.CREEPER,
                            mob -> BabyCreepers.applyBaby(
                                    (Creeper) mob
                            ),
                            mob -> BabyCreepers.isBaby(
                                    (Creeper) mob
                            )
                    )
                    .randomSpawn(DEFAULT_RANDOM_SPAWN_CHANCE)
                    .tameable(0.25F)
                    .advanced(
                            HEALTH,
                            DAMAGE,
                            MOVEMENT_SPEED
                    )
                    .blastRadius(2)
                    .build()
    );

    public static final MobletDefinition ENDERMAN = register(
            MobletDefinition.builder(
                            "enderman",
                            "Enderman",
                            EntityTypes.ENDERMAN,
                            mob -> BabyEndermen.applyBaby(
                                    (EnderMan) mob
                            ),
                            mob -> BabyEndermen.isBaby(
                                    (EnderMan) mob
                            )
                    )
                    .randomSpawn(DEFAULT_RANDOM_SPAWN_CHANCE)
                    .advanced(
                            HEALTH,
                            DAMAGE,
                            MOVEMENT_SPEED
                    )
                    .build()
    );

    public static final MobletDefinition WITCH = register(
            MobletDefinition.builder(
                            "witch",
                            "Witch",
                            EntityTypes.WITCH,
                            mob -> BabyWitches.applyBaby(
                                    (Witch) mob
                            ),
                            mob -> BabyWitches.isBaby(
                                    (Witch) mob
                            )
                    )
                    .randomSpawn(DEFAULT_RANDOM_SPAWN_CHANCE)
                    .tameable(0.25F)
                    .advanced(
                            HEALTH,
                            DAMAGE,
                            MOVEMENT_SPEED,
                            ACCURACY
                    )
                    .tamedAdvanced(
                            HEALTH,
                            MOVEMENT_SPEED,
                            ACCURACY
                    )
                    .build()
    );

    public static final MobletDefinition CAMEL_HUSK = register(
            MobletDefinition.builder(
                            "camel_husk",
                            "Camel Husk",
                            EntityTypes.CAMEL_HUSK,
                            mob -> BabyCamelHusks.applyBaby(
                                    (CamelHusk) mob
                            ),
                            mob -> BabyCamelHusks.isBaby(
                                    (CamelHusk) mob
                            )
                    )
                    .randomSpawn(DEFAULT_RANDOM_SPAWN_CHANCE)
                    .advanced(
                            HEALTH,
                            MOVEMENT_SPEED
                    )
                    .build()
    );

    public static final MobletDefinition PILLAGER = register(
            MobletDefinition.builder(
                            "pillager",
                            "Pillager",
                            EntityTypes.PILLAGER,
                            mob -> BabyPillagers.applyBaby(
                                    (Pillager) mob
                            ),
                            mob -> BabyPillagers.isBaby(
                                    (Pillager) mob
                            )
                    )
                    .tameable(0.33F)
                    .advanced(
                            HEALTH,
                            DAMAGE,
                            MOVEMENT_SPEED,
                            ACCURACY
                    )
                    .build()
    );

    public static final MobletDefinition IRON_GOLEM = register(
            MobletDefinition.builder(
                            "iron_golem",
                            "Iron Golem",
                            EntityTypes.IRON_GOLEM,
                            mob -> BabyIronGolems.applyBaby(
                                    (IronGolem) mob
                            ),
                            mob -> BabyIronGolems.isBaby(
                                    (IronGolem) mob
                            )
                    )
                    .advanced(
                            HEALTH,
                            DAMAGE,
                            MOVEMENT_SPEED
                    )
                    .build()
    );

    public static final MobletDefinition SNOW_GOLEM = register(
            MobletDefinition.builder(
                            "snow_golem",
                            "Snow Golem",
                            EntityTypes.SNOW_GOLEM,
                            mob -> BabySnowGolems.applyBaby(
                                    (SnowGolem) mob
                            ),
                            mob -> BabySnowGolems.isBaby(
                                    (SnowGolem) mob
                            )
                    )
                    .advanced(
                            HEALTH,
                            MOVEMENT_SPEED
                    )
                    .build()
    );

    public static final MobletDefinition WANDERING_TRADER = register(
            MobletDefinition.builder(
                            "wandering_trader",
                            "Wandering Trader",
                            EntityTypes.WANDERING_TRADER,
                            mob -> BabyWanderingTraders.applyBaby(
                                    (WanderingTrader) mob
                            ),
                            mob -> BabyWanderingTraders.isBaby(
                                    (WanderingTrader) mob
                            )
                    )
                    .advanced(
                            HEALTH,
                            MOVEMENT_SPEED
                    )
                    .build()
    );

    private MobletRegistry() {
    }

    private static MobletDefinition register(
            MobletDefinition definition
    ) {
        DEFINITIONS.add(definition);
        BY_ENTITY_TYPE.put(
                definition.entityType(),
                definition
        );
        return definition;
    }

    public static List<MobletDefinition> all() {
        return Collections.unmodifiableList(DEFINITIONS);
    }

    public static MobletDefinition byEntityType(
            EntityType<?> entityType
    ) {
        return BY_ENTITY_TYPE.get(entityType);
    }

    public static List<MobletDefinition> randomSpawnMoblets() {
        return DEFINITIONS.stream()
                .filter(MobletDefinition::usesRandomSpawn)
                .toList();
    }

    public static List<MobletDefinition> tameableMoblets() {
        return DEFINITIONS.stream()
                .filter(MobletDefinition::supportsTaming)
                .toList();
    }

    public static List<MobletDefinition> advancedMoblets() {
        return DEFINITIONS.stream()
                .filter(MobletDefinition::hasAdvancedBalance)
                .toList();
    }
}

package com.moblets.taming;

import com.moblets.BabyCreepers;
import com.moblets.BabyPillagers;
import com.moblets.BabyWitches;
import java.util.IdentityHashMap;
import java.util.Map;

import com.moblets.BabySkeletons;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public final class MobletTamingRegistry {
    private static final Map<EntityType<?>, MobletTamingRule> RULES =
            new IdentityHashMap<>();

    static {
        register(
                EntityTypes.SKELETON,
                new MobletTamingRule(
                        mob -> mob instanceof AbstractSkeleton skeleton
                                && BabySkeletons.isBaby(skeleton),
                        stack -> stack.is(Items.STRING),
                        0.25F
                )
        );

        register(
                EntityTypes.STRAY,
                new MobletTamingRule(
                        mob -> mob instanceof AbstractSkeleton skeleton
                                && BabySkeletons.isBaby(skeleton),
                        stack -> stack.is(Items.RABBIT_HIDE),
                        0.40F
                )
        );

        register(
                EntityTypes.BOGGED,
                new MobletTamingRule(
                        mob -> mob instanceof AbstractSkeleton skeleton
                                && BabySkeletons.isBaby(skeleton),
                        stack -> stack.is(Items.RED_MUSHROOM)
                                || stack.is(Items.BROWN_MUSHROOM),
                        0.25F
                )
        );

        register(
                EntityTypes.PARCHED,
                new MobletTamingRule(
                        mob -> mob instanceof AbstractSkeleton skeleton
                                && BabySkeletons.isBaby(skeleton),
                        stack -> {
                            if (!stack.is(Items.POTION)) {
                                return false;
                            }

                            PotionContents contents =
                                    stack.get(
                                            DataComponents.POTION_CONTENTS
                                    );

                            return contents != null
                                    && contents.is(Potions.WATER);
                        },
                        0.50F
                )
        );

        register(
                EntityTypes.WITHER_SKELETON,
                new MobletTamingRule(
                        mob -> mob instanceof AbstractSkeleton skeleton
                                && BabySkeletons.isBaby(skeleton),
                        stack -> stack.is(Items.NETHER_WART),
                        0.20F
                )
        );
        register(
                EntityTypes.PILLAGER,
                new MobletTamingRule(
                        mob -> mob instanceof Pillager pillager
                                && BabyPillagers.isBaby(pillager),
                        stack -> stack.is(Items.GOLD_INGOT),
                        0.33F
                )
        );

        register(
                EntityTypes.WITCH,
                new MobletTamingRule(
                        mob -> mob instanceof Witch witch
                                && BabyWitches.isBaby(witch),
                        stack -> stack.is(
                                Items.FERMENTED_SPIDER_EYE
                        ),
                        0.25F
                )
        );

        register(
                EntityTypes.CREEPER,
                new MobletTamingRule(
                        mob -> mob instanceof Creeper creeper
                                && BabyCreepers.isBaby(creeper),
                        stack -> stack.is(Items.FIREWORK_ROCKET),
                        0.25F
                )
        );
    }

    private MobletTamingRegistry() {
    }

    private static void register(
            EntityType<? extends Mob> entityType,
            MobletTamingRule rule
    ) {
        RULES.put(entityType, rule);
    }

    public static MobletTamingRule byEntityType(
            EntityType<?> entityType
    ) {
        return RULES.get(entityType);
    }
}

package com.moblets.taming;

import java.util.IdentityHashMap;
import java.util.Map;

import com.moblets.BabySkeletons;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.Items;

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

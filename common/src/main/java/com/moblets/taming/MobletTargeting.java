package com.moblets.taming;

import com.moblets.registry.MobletDefinition;
import com.moblets.registry.MobletRegistry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

public final class MobletTargeting {
    private MobletTargeting() {
    }

    public static boolean isTamedMoblet(LivingEntity entity) {
        if (!(entity instanceof Mob mob)
                || !(mob instanceof MobletTameState state)
                || !state.moblets$isTamed()) {
            return false;
        }

        MobletDefinition definition =
                MobletRegistry.byEntityType(mob.getType());

        return definition != null
                && definition.supportsTaming()
                && definition.isMoblet(mob);
    }

    public static boolean canTarget(
            Mob attacker,
            LivingEntity target
    ) {
        boolean tamedMoblet = isTamedMoblet(target);

        return canTarget(
                attacker.getType(),
                tamedMoblet,
                tamedMoblet
                        && target instanceof AbstractSkeleton
        );
    }

    static boolean canTarget(
            EntityType<?> attackerType,
            boolean tamedMoblet,
            boolean skeletonFamily
    ) {
        if (!tamedMoblet) {
            return true;
        }

        if (attackerType == EntityType.IRON_GOLEM
                || attackerType == EntityType.SNOW_GOLEM) {
            return false;
        }

        return attackerType != EntityType.WOLF
                || !skeletonFamily;
    }
}

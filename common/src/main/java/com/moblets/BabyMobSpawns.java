package com.moblets;

import com.moblets.config.MobletsConfig;
import com.moblets.registry.MobletDefinition;
import com.moblets.registry.MobletRegistry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public final class BabyMobSpawns {
    private BabyMobSpawns() {
    }

    public static void handleNaturalSpawn(Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return;
        }

        MobletDefinition definition =
                MobletRegistry.byEntityType(entity.getType());

        if (definition == null
                || !definition.usesRandomSpawn()) {
            return;
        }

        if (!MobletsConfig.spawnEnabled(definition)) {
            return;
        }

        if (roll(mob, MobletsConfig.spawnChance(definition))) {
            definition.applyBaby(mob);
        }
    }

    private static boolean roll(Mob mob, float chance) {
        return mob.getRandom().nextFloat() < chance;
    }
}

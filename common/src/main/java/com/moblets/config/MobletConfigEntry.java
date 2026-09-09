package com.moblets.config;

import java.util.LinkedHashMap;
import java.util.Map;

import com.moblets.registry.BalanceStat;
import com.moblets.registry.MobletDefinition;

final class MobletConfigEntry {
    private Boolean spawningEnabled;
    private Float spawnPercent;
    private Boolean tamingEnabled;
    private Float tamingPercent;
    private Map<String, Double> balance = new LinkedHashMap<>();

    boolean normalize(MobletDefinition definition) {
        boolean changed = false;

        if (definition.usesRandomSpawn()) {
            if (spawningEnabled == null) {
                spawningEnabled = true;
                changed = true;
            }

            float defaultPercent =
                    definition.defaultSpawnChance() * 100.0F;

            if (spawnPercent == null
                    || !Float.isFinite(spawnPercent)) {
                spawnPercent = defaultPercent;
                changed = true;
            } else {
                float clamped = clamp(
                        spawnPercent,
                        0.0F,
                        100.0F
                );

                if (Float.compare(clamped, spawnPercent) != 0) {
                    spawnPercent = clamped;
                    changed = true;
                }
            }
        }

        if (definition.supportsTaming()) {
            if (tamingEnabled == null) {
                tamingEnabled = true;
                changed = true;
            }

            float defaultPercent =
                    definition.defaultTamingChance()
                            * 100.0F;

            if (tamingPercent == null
                    || !Float.isFinite(tamingPercent)) {
                tamingPercent = defaultPercent;
                changed = true;
            } else {
                float clamped = clamp(
                        tamingPercent,
                        0.0F,
                        100.0F
                );

                if (Float.compare(clamped, tamingPercent) != 0) {
                    tamingPercent = clamped;
                    changed = true;
                }
            }
        }

        if (balance == null) {
            balance = new LinkedHashMap<>();
            changed = true;
        }

        for (BalanceStat stat : definition.balanceStats()) {
            String key = stat.configKey();
            Double value = balance.get(key);

            if (value == null || !Double.isFinite(value)) {
                balance.put(key, 1.0D);
                changed = true;
                continue;
            }

            double clamped = clamp(value, 0.0D, 5.0D);

            if (Double.compare(clamped, value) != 0) {
                balance.put(key, clamped);
                changed = true;
            }
        }

        return changed;
    }

    float spawnPercent(MobletDefinition definition) {
        if (spawnPercent != null
                && Float.isFinite(spawnPercent)) {
            return clamp(
                    spawnPercent,
                    0.0F,
                    100.0F
            );
        }

        return definition.defaultSpawnChance() * 100.0F;
    }

    boolean spawningEnabled() {
        return spawningEnabled == null || spawningEnabled;
    }

    void setSpawningEnabled(boolean enabled) {
        spawningEnabled = enabled;
    }

    void setSpawnPercent(float value) {
        spawnPercent = clamp(value, 0.0F, 100.0F);
    }

    void resetSpawning(MobletDefinition definition) {
        spawningEnabled = true;
        spawnPercent = definition.defaultSpawnChance()
                * 100.0F;
    }

    boolean tamingEnabled() {
        return tamingEnabled == null || tamingEnabled;
    }

    void setTamingEnabled(boolean enabled) {
        tamingEnabled = enabled;
    }

    float tamingPercent(MobletDefinition definition) {
        if (tamingPercent != null
                && Float.isFinite(tamingPercent)) {
            return clamp(
                    tamingPercent,
                    0.0F,
                    100.0F
            );
        }

        return definition.defaultTamingChance()
                * 100.0F;
    }

    void setTamingPercent(float value) {
        tamingPercent = clamp(value, 0.0F, 100.0F);
    }

    void resetTaming(MobletDefinition definition) {
        tamingEnabled = true;
        tamingPercent = definition.defaultTamingChance()
                * 100.0F;
    }

    double balanceMultiplier(BalanceStat stat) {
        if (balance == null) {
            return 1.0D;
        }

        return balance.getOrDefault(
                stat.configKey(),
                1.0D
        );
    }

    void setBalanceMultiplier(
            BalanceStat stat,
            double value
    ) {
        if (balance == null) {
            balance = new LinkedHashMap<>();
        }

        balance.put(
                stat.configKey(),
                clamp(value, 0.0D, 5.0D)
        );
    }

    private static float clamp(
            float value,
            float min,
            float max
    ) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(
            double value,
            double min,
            double max
    ) {
        return Math.max(min, Math.min(max, value));
    }
}

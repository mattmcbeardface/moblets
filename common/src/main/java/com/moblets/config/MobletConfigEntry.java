package com.moblets.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.moblets.registry.BalanceStat;
import com.moblets.registry.MobletDefinition;
import com.moblets.registry.MobletProfile;

final class MobletConfigEntry {
    private Boolean spawningEnabled;
    private Float spawnPercent;
    private Boolean tamingEnabled;
    private Float tamingPercent;
    private Map<String, Double> balance;
    private Map<String, Double> wildBalance;
    private Map<String, Double> tamedBalance;
    private Integer wildBlastRadius;
    private Integer tamedBlastRadius;

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

        Map<String, Double> legacyBalance = balance;

        if (wildBalance == null) {
            wildBalance = copyBalance(legacyBalance);
            changed = true;
        }

        if (normalizeBalance(
                wildBalance,
                definition.balanceStats(MobletProfile.WILD)
        )) {
            changed = true;
        }

        if (definition.supportsTaming()) {
            if (tamedBalance == null) {
                tamedBalance = copyBalance(legacyBalance);
                changed = true;
            }

            if (normalizeBalance(
                    tamedBalance,
                    definition.balanceStats(MobletProfile.TAMED)
            )) {
                changed = true;
            }
        } else if (tamedBalance != null) {
            tamedBalance = null;
            changed = true;
        }

        if (balance != null) {
            balance = null;
            changed = true;
        }

        if (definition.hasBlastRadius()) {
            int defaultRadius = definition.defaultBlastRadius();

            int normalizedWildRadius = wildBlastRadius == null
                    ? defaultRadius
                    : clamp(wildBlastRadius, 0, 10);

            if (wildBlastRadius == null
                    || wildBlastRadius != normalizedWildRadius) {
                wildBlastRadius = normalizedWildRadius;
                changed = true;
            }

            if (definition.supportsTaming()) {
                int normalizedTamedRadius =
                        tamedBlastRadius == null
                                ? defaultRadius
                                : clamp(tamedBlastRadius, 0, 10);

                if (tamedBlastRadius == null
                        || tamedBlastRadius
                                != normalizedTamedRadius) {
                    tamedBlastRadius = normalizedTamedRadius;
                    changed = true;
                }
            } else if (tamedBlastRadius != null) {
                tamedBlastRadius = null;
                changed = true;
            }
        } else {
            if (wildBlastRadius != null) {
                wildBlastRadius = null;
                changed = true;
            }

            if (tamedBlastRadius != null) {
                tamedBlastRadius = null;
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

    double balanceMultiplier(
            MobletProfile profile,
            BalanceStat stat
    ) {
        Map<String, Double> profileBalance = balance(profile);

        if (profileBalance == null) {
            return 1.0D;
        }

        return profileBalance.getOrDefault(
                stat.configKey(),
                1.0D
        );
    }

    void setBalanceMultiplier(
            MobletProfile profile,
            BalanceStat stat,
            double value
    ) {
        Map<String, Double> profileBalance =
                ensureBalance(profile);

        profileBalance.put(
                stat.configKey(),
                clamp(
                        value,
                        0.0D,
                        maximumMultiplier(stat)
                )
        );
    }

    int blastRadius(
            MobletDefinition definition,
            MobletProfile profile
    ) {
        Integer value = profile == MobletProfile.TAMED
                ? tamedBlastRadius
                : wildBlastRadius;

        if (value == null) {
            return definition.defaultBlastRadius();
        }

        return clamp(value, 0, 10);
    }

    void setBlastRadius(
            MobletProfile profile,
            int radius
    ) {
        if (profile == MobletProfile.TAMED) {
            tamedBlastRadius = clamp(radius, 0, 10);
        } else {
            wildBlastRadius = clamp(radius, 0, 10);
        }
    }

    void resetAdvanced(MobletDefinition definition) {
        wildBalance = new LinkedHashMap<>();
        normalizeBalance(
                wildBalance,
                definition.balanceStats(MobletProfile.WILD)
        );

        if (definition.supportsTaming()) {
            tamedBalance = new LinkedHashMap<>();
            normalizeBalance(
                    tamedBalance,
                    definition.balanceStats(MobletProfile.TAMED)
            );
        } else {
            tamedBalance = null;
        }

        if (definition.hasBlastRadius()) {
            wildBlastRadius = definition.defaultBlastRadius();

            if (definition.supportsTaming()) {
                tamedBlastRadius = definition.defaultBlastRadius();
            } else {
                tamedBlastRadius = null;
            }
        } else {
            wildBlastRadius = null;
            tamedBlastRadius = null;
        }
    }

    private Map<String, Double> balance(MobletProfile profile) {
        return profile == MobletProfile.TAMED
                ? tamedBalance
                : wildBalance;
    }

    private Map<String, Double> ensureBalance(
            MobletProfile profile
    ) {
        if (profile == MobletProfile.TAMED) {
            if (tamedBalance == null) {
                tamedBalance = new LinkedHashMap<>();
            }

            return tamedBalance;
        }

        if (wildBalance == null) {
            wildBalance = new LinkedHashMap<>();
        }

        return wildBalance;
    }

    private static Map<String, Double> copyBalance(
            Map<String, Double> source
    ) {
        return source == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(source);
    }

    private static boolean normalizeBalance(
            Map<String, Double> profileBalance,
            Set<BalanceStat> stats
    ) {
        boolean changed = false;

        if (profileBalance.keySet().removeIf(
                key -> !supportsStat(stats, key)
        )) {
            changed = true;
        }

        for (BalanceStat stat : stats) {
            String key = stat.configKey();
            Double value = profileBalance.get(key);

            if (value == null || !Double.isFinite(value)) {
                profileBalance.put(key, 1.0D);
                changed = true;
                continue;
            }

            double clamped = clamp(
                    value,
                    0.0D,
                    maximumMultiplier(stat)
            );

            if (Double.compare(clamped, value) != 0) {
                profileBalance.put(key, clamped);
                changed = true;
            }
        }

        return changed;
    }

    private static boolean supportsStat(
            Set<BalanceStat> stats,
            String configKey
    ) {
        for (BalanceStat stat : stats) {
            if (stat.configKey().equals(configKey)) {
                return true;
            }
        }

        return false;
    }

    private static double maximumMultiplier(BalanceStat stat) {
        return stat == BalanceStat.ACCURACY
                ? MobletsConfig.PERFECT_AIM_MULTIPLIER
                : MobletsConfig.MAX_ADVANCED_MULTIPLIER;
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

    private static int clamp(
            int value,
            int min,
            int max
    ) {
        return Math.max(min, Math.min(max, value));
    }
}

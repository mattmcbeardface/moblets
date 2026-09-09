package com.moblets.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.moblets.Moblets;
import com.moblets.registry.BalanceStat;
import com.moblets.registry.EncounterDefinition;
import com.moblets.registry.EncounterRegistry;
import com.moblets.registry.MobletDefinition;
import com.moblets.registry.MobletRegistry;

public final class MobletsConfig {
    public static final int MIN_PILLAGER_OUTPOST_MOBLETS = 0;
    public static final int MAX_PILLAGER_OUTPOST_MOBLETS = 10;
    public static final int DEFAULT_PILLAGER_OUTPOST_MOBLETS = 2;

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final String FILE_NAME =
            "moblets.json";

    private static ConfigData data =
            new ConfigData();

    private static Path configFile;

    private MobletsConfig() {
    }

    public static synchronized void initialize(
            Path configDirectory
    ) {
        configFile =
                configDirectory.resolve(FILE_NAME);

        load();
    }

    public static synchronized float spawnChance(
            MobletDefinition definition
    ) {
        return spawnPercent(definition) / 100.0F;
    }

    public static synchronized boolean spawningEnabled() {
        return data.spawningEnabled == null
                || data.spawningEnabled;
    }

    public static synchronized void setSpawningEnabled(
            boolean enabled
    ) {
        data.spawningEnabled = enabled;
    }

    public static synchronized boolean spawnEnabled(
            MobletDefinition definition
    ) {
        if (!definition.usesRandomSpawn()) {
            return false;
        }

        return spawningEnabled()
                && speciesSpawningEnabled(definition);
    }

    public static synchronized boolean speciesSpawningEnabled(
            MobletDefinition definition
    ) {
        if (!definition.usesRandomSpawn()) {
            return false;
        }

        return entry(definition).spawningEnabled();
    }

    public static synchronized void setSpeciesSpawningEnabled(
            MobletDefinition definition,
            boolean enabled
    ) {
        if (!definition.usesRandomSpawn()) {
            return;
        }

        entry(definition).setSpawningEnabled(enabled);
    }

    public static synchronized float spawnPercent(
            MobletDefinition definition
    ) {
        return entry(definition)
                .spawnPercent(definition);
    }

    public static synchronized void setSpawnPercent(
            MobletDefinition definition,
            float percent
    ) {
        if (!definition.usesRandomSpawn()) {
            return;
        }

        float value = Float.isFinite(percent)
                ? percent
                : definition.defaultSpawnChance()
                        * 100.0F;

        entry(definition)
                .setSpawnPercent(value);
    }

    public static synchronized boolean pillagerOutpostSpawningEnabled() {
        return spawningEnabled()
                && encounterEnabled(
                        EncounterRegistry.PILLAGER_OUTPOST
                );
    }

    public static synchronized int pillagerOutpostMobletCount() {
        if (data.pillagerOutpostMobletCount == null) {
            return DEFAULT_PILLAGER_OUTPOST_MOBLETS;
        }

        return clampPillagerOutpostMobletCount(
                data.pillagerOutpostMobletCount
        );
    }

    public static synchronized void setPillagerOutpostMobletCount(
            int count
    ) {
        data.pillagerOutpostMobletCount =
                clampPillagerOutpostMobletCount(count);
    }

    public static synchronized void resetSpawning() {
        data.spawningEnabled = true;

        for (MobletDefinition definition
                : MobletRegistry.randomSpawnMoblets()) {
            entry(definition).resetSpawning(definition);
        }

        data.encounters.put(
                EncounterRegistry.PILLAGER_OUTPOST.id(),
                EncounterRegistry.PILLAGER_OUTPOST
                        .defaultEnabled()
        );
        data.pillagerOutpostMobletCount =
                DEFAULT_PILLAGER_OUTPOST_MOBLETS;
    }

    public static synchronized boolean tamingEnabled(
            MobletDefinition definition
    ) {
        if (!definition.supportsTaming()) {
            return false;
        }

        return globalTamingEnabled()
                && speciesTamingEnabled(definition);
    }

    public static synchronized boolean globalTamingEnabled() {
        return data.tamingEnabled == null
                || data.tamingEnabled;
    }

    public static synchronized void setGlobalTamingEnabled(
            boolean enabled
    ) {
        data.tamingEnabled = enabled;
    }

    public static synchronized boolean speciesTamingEnabled(
            MobletDefinition definition
    ) {
        if (!definition.supportsTaming()) {
            return false;
        }

        return entry(definition).tamingEnabled();
    }

    public static synchronized void setSpeciesTamingEnabled(
            MobletDefinition definition,
            boolean enabled
    ) {
        if (!definition.supportsTaming()) {
            return;
        }

        entry(definition)
                .setTamingEnabled(enabled);
    }

    public static synchronized float tamingChance(
            MobletDefinition definition
    ) {
        return tamingPercent(definition) / 100.0F;
    }

    public static synchronized float tamingPercent(
            MobletDefinition definition
    ) {
        if (!definition.supportsTaming()) {
            return 0.0F;
        }

        return entry(definition)
                .tamingPercent(definition);
    }

    public static synchronized void setTamingPercent(
            MobletDefinition definition,
            float percent
    ) {
        if (!definition.supportsTaming()) {
            return;
        }

        float value = Float.isFinite(percent)
                ? percent
                : definition.defaultTamingChance()
                        * 100.0F;

        entry(definition)
                .setTamingPercent(value);
    }

    public static synchronized void resetTaming() {
        data.tamingEnabled = true;

        for (MobletDefinition definition
                : MobletRegistry.tameableMoblets()) {
            entry(definition).resetTaming(definition);
        }
    }

    public static synchronized double balanceMultiplier(
            MobletDefinition definition,
            BalanceStat stat
    ) {
        if (!definition.balanceStats().contains(stat)) {
            return 1.0D;
        }

        return entry(definition)
                .balanceMultiplier(stat);
    }

    public static synchronized void setBalanceMultiplier(
            MobletDefinition definition,
            BalanceStat stat,
            double multiplier
    ) {
        if (!definition.balanceStats().contains(stat)) {
            return;
        }

        entry(definition)
                .setBalanceMultiplier(
                        stat,
                        multiplier
                );
    }

    public static synchronized boolean encounterEnabled(
            EncounterDefinition definition
    ) {
        return data.encounters.getOrDefault(
                definition.id(),
                definition.defaultEnabled()
        );
    }

    public static synchronized void setEncounterEnabled(
            EncounterDefinition definition,
            boolean enabled
    ) {
        data.encounters.put(
                definition.id(),
                enabled
        );
    }

    public static synchronized void resetMoblet(
            MobletDefinition definition
    ) {
        data.mobs.put(
                definition.id(),
                new MobletConfigEntry()
        );

        entry(definition);
    }

    public static synchronized void resetAll() {
        data = new ConfigData();
        normalize();
    }

    public static synchronized void save() {
        if (configFile == null) {
            return;
        }

        normalize();

        try {
            Files.createDirectories(
                    configFile.getParent()
            );

            Path temporaryFile =
                    configFile.resolveSibling(
                            configFile.getFileName()
                                    + ".tmp"
                    );

            Files.writeString(
                    temporaryFile,
                    GSON.toJson(data),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            try {
                Files.move(
                        temporaryFile,
                        configFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(
                        temporaryFile,
                        configFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (IOException exception) {
            Moblets.LOGGER.error(
                    "Failed to save Moblets config.",
                    exception
            );
        }
    }

    private static void load() {
        boolean needsSave =
                !Files.exists(configFile);

        if (!needsSave) {
            try (Reader reader =
                         Files.newBufferedReader(
                                 configFile,
                                 StandardCharsets.UTF_8
                         )) {

                ConfigData loaded =
                        GSON.fromJson(
                                reader,
                                ConfigData.class
                        );

                data = loaded == null
                        ? new ConfigData()
                        : loaded;
            } catch (Exception exception) {
                Moblets.LOGGER.error(
                        "Failed to read Moblets config. "
                                + "Backing up the broken file "
                                + "and restoring defaults.",
                        exception
                );

                backupBrokenConfig();
                data = new ConfigData();
                needsSave = true;
            }
        } else {
            data = new ConfigData();
        }

        boolean changed = normalize();

        if (needsSave || changed) {
            save();
        }
    }

    private static boolean normalize() {
        boolean changed = false;

        if (data == null) {
            data = new ConfigData();
            changed = true;
        }

        if (data.mobs == null) {
            data.mobs = new LinkedHashMap<>();
            changed = true;
        }

        if (data.spawningEnabled == null) {
            data.spawningEnabled = true;
            changed = true;
        }

        if (data.tamingEnabled == null) {
            data.tamingEnabled = true;
            changed = true;
        }

        int normalizedOutpostCount =
                data.pillagerOutpostMobletCount == null
                        ? DEFAULT_PILLAGER_OUTPOST_MOBLETS
                        : clampPillagerOutpostMobletCount(
                                data.pillagerOutpostMobletCount
                        );

        if (data.pillagerOutpostMobletCount == null
                || data.pillagerOutpostMobletCount
                        != normalizedOutpostCount) {
            data.pillagerOutpostMobletCount =
                    normalizedOutpostCount;
            changed = true;
        }

        if (data.encounters == null) {
            data.encounters = new LinkedHashMap<>();
            changed = true;
        }

        for (MobletDefinition definition
                : MobletRegistry.all()) {

            if (!isConfigurable(definition)) {
                continue;
            }

            MobletConfigEntry entry =
                    data.mobs.get(definition.id());

            if (entry == null) {
                entry = new MobletConfigEntry();

                data.mobs.put(
                        definition.id(),
                        entry
                );

                changed = true;
            }

            if (entry.normalize(definition)) {
                changed = true;
            }
        }

        for (EncounterDefinition definition
                : EncounterRegistry.all()) {

            if (!data.encounters.containsKey(
                    definition.id()
            )) {
                data.encounters.put(
                        definition.id(),
                        definition.defaultEnabled()
                );

                changed = true;
            }
        }

        return changed;
    }

    private static MobletConfigEntry entry(
            MobletDefinition definition
    ) {
        MobletConfigEntry entry =
                data.mobs.computeIfAbsent(
                        definition.id(),
                        ignored ->
                                new MobletConfigEntry()
                );

        entry.normalize(definition);

        return entry;
    }

    private static boolean isConfigurable(
            MobletDefinition definition
    ) {
        return definition.usesRandomSpawn()
                || definition.supportsTaming()
                || definition.hasAdvancedBalance();
    }

    private static int clampPillagerOutpostMobletCount(
            int count
    ) {
        return Math.max(
                MIN_PILLAGER_OUTPOST_MOBLETS,
                Math.min(
                        MAX_PILLAGER_OUTPOST_MOBLETS,
                        count
                )
        );
    }

    private static void backupBrokenConfig() {
        if (!Files.exists(configFile)) {
            return;
        }

        Path backup =
                configFile.resolveSibling(
                        FILE_NAME
                                + ".broken-"
                                + System.currentTimeMillis()
                );

        try {
            Files.move(
                    configFile,
                    backup,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException exception) {
            Moblets.LOGGER.error(
                    "Failed to back up broken Moblets config.",
                    exception
            );
        }
    }

    private static final class ConfigData {
        private Boolean spawningEnabled;
        private Boolean tamingEnabled;
        private Integer pillagerOutpostMobletCount;

        private Map<String, MobletConfigEntry> mobs =
                new LinkedHashMap<>();

        private Map<String, Boolean> encounters =
                new LinkedHashMap<>();
    }
}

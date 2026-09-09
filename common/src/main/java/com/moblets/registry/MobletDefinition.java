package com.moblets.registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class MobletDefinition {
    private final String id;
    private final String displayName;
    private final EntityType<? extends Mob> entityType;
    private final Consumer<Mob> babyApplicator;

    private final boolean randomSpawn;
    private final float defaultSpawnChance;

    private final boolean supportsTaming;
    private final float defaultTamingChance;
    private final boolean advancedBalance;

    private final Set<BalanceStat> balanceStats;

    private MobletDefinition(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.entityType = builder.entityType;
        this.babyApplicator = builder.babyApplicator;
        this.randomSpawn = builder.randomSpawn;
        this.defaultSpawnChance = builder.defaultSpawnChance;
        this.supportsTaming = builder.supportsTaming;
        this.defaultTamingChance = builder.defaultTamingChance;
        this.advancedBalance = builder.advancedBalance;

        if (builder.balanceStats.isEmpty()) {
            this.balanceStats =
                    Collections.unmodifiableSet(
                            EnumSet.noneOf(BalanceStat.class)
                    );
        } else {
            this.balanceStats =
                    Collections.unmodifiableSet(
                            EnumSet.copyOf(builder.balanceStats)
                    );
        }
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public EntityType<? extends Mob> entityType() {
        return entityType;
    }

    public boolean usesRandomSpawn() {
        return randomSpawn;
    }

    public float defaultSpawnChance() {
        return defaultSpawnChance;
    }

    public boolean supportsTaming() {
        return supportsTaming;
    }

    public float defaultTamingChance() {
        return defaultTamingChance;
    }

    public boolean hasAdvancedBalance() {
        return advancedBalance;
    }

    public Set<BalanceStat> balanceStats() {
        return balanceStats;
    }

    public void applyBaby(Mob mob) {
        babyApplicator.accept(mob);
    }

    public static Builder builder(
            String id,
            String displayName,
            EntityType<? extends Mob> entityType,
            Consumer<Mob> babyApplicator
    ) {
        return new Builder(
                id,
                displayName,
                entityType,
                babyApplicator
        );
    }

    public static final class Builder {
        private final String id;
        private final String displayName;
        private final EntityType<? extends Mob> entityType;
        private final Consumer<Mob> babyApplicator;

        private boolean randomSpawn;
        private float defaultSpawnChance;

        private boolean supportsTaming;
        private float defaultTamingChance;
        private boolean advancedBalance;

        private final EnumSet<BalanceStat> balanceStats =
                EnumSet.noneOf(BalanceStat.class);

        private Builder(
                String id,
                String displayName,
                EntityType<? extends Mob> entityType,
                Consumer<Mob> babyApplicator
        ) {
            this.id = id;
            this.displayName = displayName;
            this.entityType = entityType;
            this.babyApplicator = babyApplicator;
        }

        public Builder randomSpawn(float defaultChance) {
            this.randomSpawn = true;
            this.defaultSpawnChance = defaultChance;
            return this;
        }

        public Builder tameable(float defaultChance) {
            if (!Float.isFinite(defaultChance)
                    || defaultChance < 0.0F
                    || defaultChance > 1.0F) {
                throw new IllegalArgumentException(
                        "Default taming chance must be between 0 and 1."
                );
            }

            this.supportsTaming = true;
            this.defaultTamingChance = defaultChance;
            return this;
        }

        public Builder advanced(BalanceStat... stats) {
            this.advancedBalance = true;
            this.balanceStats.addAll(Arrays.asList(stats));
            return this;
        }

        public MobletDefinition build() {
            return new MobletDefinition(this);
        }
    }
}

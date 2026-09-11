package com.moblets.registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class MobletDefinition {
    private final String id;
    private final String displayName;
    private final EntityType<? extends Mob> entityType;
    private final Consumer<Mob> babyApplicator;
    private final Predicate<Mob> babyPredicate;

    private final boolean randomSpawn;
    private final float defaultSpawnChance;

    private final boolean supportsTaming;
    private final float defaultTamingChance;
    private final Set<BalanceStat> wildBalanceStats;
    private final Set<BalanceStat> tamedBalanceStats;
    private final Integer defaultBlastRadius;

    private MobletDefinition(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.entityType = builder.entityType;
        this.babyApplicator = builder.babyApplicator;
        this.babyPredicate = builder.babyPredicate;
        this.randomSpawn = builder.randomSpawn;
        this.defaultSpawnChance = builder.defaultSpawnChance;
        this.supportsTaming = builder.supportsTaming;
        this.defaultTamingChance = builder.defaultTamingChance;
        this.wildBalanceStats = immutableStats(
                builder.wildBalanceStats
        );

        EnumSet<BalanceStat> tamedStats;

        if (builder.tamedBalanceExplicit) {
            tamedStats = builder.tamedBalanceStats;
        } else if (builder.supportsTaming) {
            tamedStats = builder.wildBalanceStats;
        } else {
            tamedStats = EnumSet.noneOf(BalanceStat.class);
        }

        this.tamedBalanceStats = immutableStats(tamedStats);
        this.defaultBlastRadius = builder.defaultBlastRadius;
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
        return !wildBalanceStats.isEmpty()
                || !tamedBalanceStats.isEmpty()
                || defaultBlastRadius != null;
    }

    public Set<BalanceStat> balanceStats() {
        return wildBalanceStats;
    }

    public Set<BalanceStat> balanceStats(MobletProfile profile) {
        if (profile == MobletProfile.TAMED) {
            return supportsTaming
                    ? tamedBalanceStats
                    : Collections.emptySet();
        }

        return wildBalanceStats;
    }

    public boolean supportsBalance(
            MobletProfile profile,
            BalanceStat stat
    ) {
        return balanceStats(profile).contains(stat);
    }

    public boolean hasBlastRadius() {
        return defaultBlastRadius != null;
    }

    public int defaultBlastRadius() {
        if (defaultBlastRadius == null) {
            throw new IllegalStateException(
                    displayName + " does not define a blast radius."
            );
        }

        return defaultBlastRadius;
    }

    public void applyBaby(Mob mob) {
        babyApplicator.accept(mob);
    }

    public boolean isMoblet(Mob mob) {
        return entityType == mob.getType()
                && babyPredicate.test(mob);
    }

    public static Builder builder(
            String id,
            String displayName,
            EntityType<? extends Mob> entityType,
            Consumer<Mob> babyApplicator,
            Predicate<Mob> babyPredicate
    ) {
        return new Builder(
                id,
                displayName,
                entityType,
                babyApplicator,
                babyPredicate
        );
    }

    private static Set<BalanceStat> immutableStats(
            EnumSet<BalanceStat> stats
    ) {
        if (stats.isEmpty()) {
            return Collections.unmodifiableSet(
                    EnumSet.noneOf(BalanceStat.class)
            );
        }

        return Collections.unmodifiableSet(
                EnumSet.copyOf(stats)
        );
    }

    public static final class Builder {
        private final String id;
        private final String displayName;
        private final EntityType<? extends Mob> entityType;
        private final Consumer<Mob> babyApplicator;
        private final Predicate<Mob> babyPredicate;

        private boolean randomSpawn;
        private float defaultSpawnChance;

        private boolean supportsTaming;
        private float defaultTamingChance;
        private final EnumSet<BalanceStat> wildBalanceStats =
                EnumSet.noneOf(BalanceStat.class);
        private final EnumSet<BalanceStat> tamedBalanceStats =
                EnumSet.noneOf(BalanceStat.class);
        private boolean tamedBalanceExplicit;
        private Integer defaultBlastRadius;

        private Builder(
                String id,
                String displayName,
                EntityType<? extends Mob> entityType,
                Consumer<Mob> babyApplicator,
                Predicate<Mob> babyPredicate
        ) {
            this.id = id;
            this.displayName = displayName;
            this.entityType = entityType;
            this.babyApplicator = babyApplicator;
            this.babyPredicate = babyPredicate;
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
            this.wildBalanceStats.addAll(Arrays.asList(stats));
            return this;
        }

        public Builder tamedAdvanced(BalanceStat... stats) {
            this.tamedBalanceStats.clear();
            this.tamedBalanceStats.addAll(Arrays.asList(stats));
            this.tamedBalanceExplicit = true;
            return this;
        }

        public Builder blastRadius(int defaultValue) {
            if (defaultValue < 0 || defaultValue > 10) {
                throw new IllegalArgumentException(
                        "Default blast radius must be between 0 and 10."
                );
            }

            this.defaultBlastRadius = defaultValue;
            return this;
        }

        public MobletDefinition build() {
            return new MobletDefinition(this);
        }
    }
}

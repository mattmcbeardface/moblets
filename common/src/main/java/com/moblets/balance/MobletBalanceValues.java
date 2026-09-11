package com.moblets.balance;

import com.moblets.config.MobletsConfig;
import com.moblets.registry.BalanceStat;
import com.moblets.registry.MobletDefinition;
import com.moblets.registry.MobletProfile;

public final class MobletBalanceValues {
    public static final double MAX_PERCENT = 500.0D;
    public static final double MAX_SPREAD = 100.0D;
    public static final double ZERO_ACCURACY_SPREAD = 1000.0D;
    public static final double SPREAD_STEP = 0.1D;

    private static final double PERFECT_AIM_MULTIPLIER =
            MobletsConfig.PERFECT_AIM_MULTIPLIER;

    private MobletBalanceValues() {
    }

    public enum Presentation {
        ADULT_PERCENT,
        SCALE_PERCENT,
        SPREAD
    }

    public static Presentation presentation(
            MobletDefinition definition,
            BalanceStat stat
    ) {
        if (stat == BalanceStat.ACCURACY) {
            return Presentation.SPREAD;
        }

        if (definition.id().equals("creeper")
                && stat == BalanceStat.DAMAGE) {
            return Presentation.SCALE_PERCENT;
        }

        return Presentation.ADULT_PERCENT;
    }

    public static double displayedValue(
            MobletDefinition definition,
            MobletProfile profile,
            BalanceStat stat,
            int difficultyId
    ) {
        double baseline = defaultDisplayedValue(
                definition,
                profile,
                stat,
                difficultyId
        );
        double multiplier = MobletsConfig.balanceMultiplier(
                definition,
                profile,
                stat
        );

        if (presentation(definition, stat)
                == Presentation.SPREAD) {
            if (multiplier <= 0.0D) {
                return ZERO_ACCURACY_SPREAD;
            }

            if (multiplier >= PERFECT_AIM_MULTIPLIER) {
                return 0.0D;
            }

            return baseline / multiplier;
        }

        return baseline * multiplier;
    }

    public static void setDisplayedValue(
            MobletDefinition definition,
            MobletProfile profile,
            BalanceStat stat,
            int difficultyId,
            double displayedValue
    ) {
        double baseline = defaultDisplayedValue(
                definition,
                profile,
                stat,
                difficultyId
        );
        double multiplier;

        if (presentation(definition, stat)
                == Presentation.SPREAD) {
            multiplier = displayedValue <= 0.0D
                    ? PERFECT_AIM_MULTIPLIER
                    : baseline / displayedValue;
        } else {
            multiplier = displayedValue / baseline;
        }

        MobletsConfig.setBalanceMultiplier(
                definition,
                profile,
                stat,
                multiplier
        );
    }

    public static double defaultDisplayedValue(
            MobletDefinition definition,
            MobletProfile profile,
            BalanceStat stat,
            int difficultyId
    ) {
        if (!definition.supportsBalance(profile, stat)) {
            throw new IllegalArgumentException(
                    definition.displayName()
                            + " does not support "
                            + stat
                            + " for "
                            + profile
            );
        }

        return switch (stat) {
            case HEALTH -> defaultHealthPercent(
                    definition.id(),
                    profile
            );
            case DAMAGE -> defaultDamagePercent(
                    definition.id(),
                    profile
            );
            case MOVEMENT_SPEED -> defaultSpeedPercent(
                    definition.id()
            );
            case ACCURACY -> defaultSpread(
                    definition.id(),
                    profile,
                    difficultyId
            );
        };
    }

    private static double defaultHealthPercent(
            String id,
            MobletProfile profile
    ) {
        if (profile != MobletProfile.TAMED) {
            return 100.0D;
        }

        return switch (id) {
            case "skeleton", "stray", "wither_skeleton" ->
                    35.0D / 20.0D * 100.0D;
            case "bogged", "parched" ->
                    28.0D / 16.0D * 100.0D;
            case "creeper" -> 30.0D / 20.0D * 100.0D;
            case "witch" -> 35.0D / 26.0D * 100.0D;
            case "pillager" -> 35.0D / 24.0D * 100.0D;
            default -> throw unknownBaseline(id, BalanceStat.HEALTH);
        };
    }

    private static double defaultDamagePercent(
            String id,
            MobletProfile profile
    ) {
        return switch (id) {
            case "skeleton", "stray", "bogged", "parched" ->
                    profile == MobletProfile.TAMED
                            ? 125.0D
                            : 50.0D;
            case "wither_skeleton" ->
                    profile == MobletProfile.TAMED
                            ? 100.0D
                            : 50.0D;
            case "creeper" ->
                    profile == MobletProfile.TAMED
                            ? 100.0D
                            : 100.0D / 3.0D;
            case "enderman" -> 35.0D;
            case "witch" -> 50.0D;
            case "pillager" ->
                    profile == MobletProfile.TAMED
                            ? 150.0D
                            : 50.0D;
            case "iron_golem" -> 50.0D;
            default -> throw unknownBaseline(id, BalanceStat.DAMAGE);
        };
    }

    private static double defaultSpeedPercent(String id) {
        return switch (id) {
            case "skeleton", "stray", "bogged", "parched",
                    "wither_skeleton", "enderman", "pillager" ->
                    125.0D;
            case "creeper" -> 135.0D;
            case "witch", "camel_husk", "iron_golem",
                    "snow_golem", "wandering_trader" -> 120.0D;
            default -> throw unknownBaseline(
                    id,
                    BalanceStat.MOVEMENT_SPEED
            );
        };
    }

    private static double defaultSpread(
            String id,
            MobletProfile profile,
            int difficultyId
    ) {
        if (id.equals("witch")) {
            return profile == MobletProfile.TAMED
                    ? 0.10D
                    : 40.0D;
        }

        if (id.equals("pillager")) {
            return profile == MobletProfile.TAMED
                    ? adultDifficultySpread(difficultyId) * 0.50D
                    : 20.0D;
        }

        if (id.equals("skeleton")
                || id.equals("stray")
                || id.equals("bogged")
                || id.equals("parched")) {
            return profile == MobletProfile.TAMED
                    ? adultDifficultySpread(difficultyId) * 0.75D
                    : 26.0D;
        }

        throw unknownBaseline(id, BalanceStat.ACCURACY);
    }

    private static double adultDifficultySpread(int difficultyId) {
        int safeDifficultyId = Math.max(0, Math.min(3, difficultyId));
        return 14.0D - safeDifficultyId * 4.0D;
    }

    private static IllegalArgumentException unknownBaseline(
            String id,
            BalanceStat stat
    ) {
        return new IllegalArgumentException(
                "Missing Advanced display baseline for "
                        + id
                        + " "
                        + stat
        );
    }
}

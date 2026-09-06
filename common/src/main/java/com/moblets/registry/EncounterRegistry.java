package com.moblets.registry;

import java.util.List;

public final class EncounterRegistry {
    public static final EncounterDefinition PILLAGER_OUTPOST =
            new EncounterDefinition(
                    "pillager_outpost",
                    "Pillager Outpost",
                    true
            );

    public static final EncounterDefinition WANDERING_TRADER_CARAVAN =
            new EncounterDefinition(
                    "wandering_trader_caravan",
                    "Wandering Trader Caravan",
                    true
            );

    public static final EncounterDefinition SNOW_GOLEM_COMPANION =
            new EncounterDefinition(
                    "snow_golem_companion",
                    "Snow Golem Companion",
                    true
            );

    public static final EncounterDefinition IRON_GOLEM_COMPANION =
            new EncounterDefinition(
                    "iron_golem_companion",
                    "Iron Golem Companion",
                    true
            );

    public static final EncounterDefinition CAMEL_HUSK_RIDERS =
            new EncounterDefinition(
                    "camel_husk_riders",
                    "Baby Camel Husk Riders",
                    true
            );

    private static final List<EncounterDefinition> DEFINITIONS =
            List.of(
                    PILLAGER_OUTPOST,
                    WANDERING_TRADER_CARAVAN,
                    SNOW_GOLEM_COMPANION,
                    IRON_GOLEM_COMPANION,
                    CAMEL_HUSK_RIDERS
            );

    private EncounterRegistry() {
    }

    public static List<EncounterDefinition> all() {
        return DEFINITIONS;
    }
}

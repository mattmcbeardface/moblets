package com.moblets.registry;

public record EncounterDefinition(
        String id,
        String displayName,
        boolean defaultEnabled
) {
}

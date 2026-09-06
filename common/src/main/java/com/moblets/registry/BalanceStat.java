package com.moblets.registry;

public enum BalanceStat {
    HEALTH("health"),
    DAMAGE("damage"),
    MOVEMENT_SPEED("movementSpeed"),
    ACCURACY("accuracy");

    private final String configKey;

    BalanceStat(String configKey) {
        this.configKey = configKey;
    }

    public String configKey() {
        return configKey;
    }
}

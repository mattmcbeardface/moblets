package com.moblets.taming;

import java.util.UUID;

import net.minecraft.core.BlockPos;

import net.minecraft.world.entity.player.Player;

public interface MobletTameState {
    boolean moblets$isTamed();

    UUID moblets$getOwnerUuid();

    void moblets$setOwnerUuid(UUID ownerUuid);

    UUID moblets$getCuriousPlayerUuid();

    void moblets$setCuriousPlayerUuid(UUID playerUuid);

    boolean moblets$isConsideringTame();

    UUID moblets$getConsideringPlayerUuid();

    void moblets$beginConsideringTame(
            UUID playerUuid,
            boolean success,
            int ticks
    );

    boolean moblets$getPendingTameSuccess();

    int moblets$getConsideringTicks();

    void moblets$setConsideringTicks(int ticks);

    void moblets$clearConsideringTame();

    boolean moblets$isOrderedToStay();

    void moblets$setOrderedToStay(boolean stay);

    BlockPos moblets$getStayAnchor();

    void moblets$setStayAnchor(BlockPos anchor);

    default boolean moblets$isOwnedBy(Player player) {
        UUID ownerUuid = moblets$getOwnerUuid();

        return ownerUuid != null
                && ownerUuid.equals(player.getUUID());
    }

    default boolean moblets$isCuriousAbout(Player player) {
        UUID playerUuid = moblets$getCuriousPlayerUuid();

        return playerUuid != null
                && playerUuid.equals(player.getUUID());
    }
}

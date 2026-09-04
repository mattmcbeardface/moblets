package com.pagesofatlas.babymobs;

import java.util.List;
import java.util.UUID;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;

public final class BabyTraderSalesState {

    private static final String SALES_ATTEMPTED_TAG =
            "baby_mobs:sales_attempted";

    private static final String OFFER_TAG_PREFIX =
            "baby_mobs:trader_offer:";

    private static final String DEBTOR_TAG_PREFIX =
            "baby_mobs:debtor:";

    /*
     * Remembers which player this trader is currently trying
     * to sell to.
     *
     * This allows the interaction to resume if vanilla trader
     * AI temporarily interrupts our sales goal.
     */
    private static final String SALES_CUSTOMER_TAG_PREFIX =
            "baby_mobs:sales_customer:";

    private BabyTraderSalesState() {
    }

    public static boolean hasSalesAttempted(
            WanderingTrader trader
    ) {
        return trader.entityTags()
                .contains(SALES_ATTEMPTED_TAG);
    }

    public static boolean claimSalesAttempt(
            WanderingTrader trader
    ) {
        return trader.addTag(
                SALES_ATTEMPTED_TAG
        );
    }

    public static void tagOffer(
            ItemEntity offer,
            WanderingTrader trader
    ) {
        offer.addTag(
                OFFER_TAG_PREFIX
                        + trader.getUUID()
        );
    }

    public static boolean isOfferFrom(
            ItemEntity offer,
            WanderingTrader trader
    ) {
        return offer.entityTags()
                .contains(
                        OFFER_TAG_PREFIX
                                + trader.getUUID()
                );
    }

    public static void setSalesCustomer(
            WanderingTrader trader,
            Player player
    ) {
        clearTagsWithPrefix(
                trader,
                SALES_CUSTOMER_TAG_PREFIX
        );

        trader.addTag(
                SALES_CUSTOMER_TAG_PREFIX
                        + player.getUUID()
        );
    }

    public static UUID getSalesCustomerUuid(
            WanderingTrader trader
    ) {
        return getUuidTag(
                trader,
                SALES_CUSTOMER_TAG_PREFIX
        );
    }

    public static void clearSalesCustomer(
            WanderingTrader trader
    ) {
        clearTagsWithPrefix(
                trader,
                SALES_CUSTOMER_TAG_PREFIX
        );
    }

    public static void setDebtor(
            WanderingTrader trader,
            Player player
    ) {
        clearTagsWithPrefix(
                trader,
                DEBTOR_TAG_PREFIX
        );

        trader.addTag(
                DEBTOR_TAG_PREFIX
                        + player.getUUID()
        );
    }

    public static UUID getDebtorUuid(
            WanderingTrader trader
    ) {
        return getUuidTag(
                trader,
                DEBTOR_TAG_PREFIX
        );
    }

    public static boolean hasDebtor(
            WanderingTrader trader
    ) {
        return getDebtorUuid(trader)
                != null;
    }

    public static void clearDebtor(
            WanderingTrader trader
    ) {
        clearTagsWithPrefix(
                trader,
                DEBTOR_TAG_PREFIX
        );
    }

    private static UUID getUuidTag(
            WanderingTrader trader,
            String prefix
    ) {
        for (String tag :
                trader.entityTags()) {

            if (!tag.startsWith(prefix)) {
                continue;
            }

            String value =
                    tag.substring(
                            prefix.length()
                    );

            try {
                return UUID.fromString(value);
            } catch (IllegalArgumentException ignored) {
                /*
                 * Ignore malformed stale tags.
                 */
            }
        }

        return null;
    }

    private static void clearTagsWithPrefix(
            WanderingTrader trader,
            String prefix
    ) {
        for (String tag :
                List.copyOf(
                        trader.entityTags()
                )) {

            if (tag.startsWith(prefix)) {
                trader.removeTag(tag);
            }
        }
    }
}

package com.moblets.taming;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public final class MobletWitchTrades {
    public static final int HEDGE_WITCH = 1;
    public static final int CAULDRON_KEEPER = 2;
    public static final int HEXBINDER = 3;
    public static final int COVEN_ELDER = 4;
    public static final int HIGH_WITCH = 5;

    /*
     * Same progression thresholds used by the familiar
     * five-level villager-style advancement curve.
     */
    private static final int CAULDRON_KEEPER_XP = 10;
    private static final int HEXBINDER_XP = 70;
    private static final int COVEN_ELDER_XP = 150;
    private static final int HIGH_WITCH_XP = 250;

    private static final int OFFERS_PER_TIER = 3;

    /*
     * Every effect-bearing vanilla Potion definition in 26.2.
     *
     * Water, Mundane, Thick and Awkward are intentionally not
     * sold because they are brewing intermediates rather than
     * useful finished potions.
     */
    private static final List<PotionSpec> POTIONS =
            List.of(
                    // ------------------------------------------------
                    // Hedge Witch
                    // ------------------------------------------------
                    new PotionSpec(
                            Potions.NIGHT_VISION,
                            HEDGE_WITCH
                    ),
                    new PotionSpec(
                            Potions.LEAPING,
                            HEDGE_WITCH
                    ),
                    new PotionSpec(
                            Potions.FIRE_RESISTANCE,
                            HEDGE_WITCH
                    ),
                    new PotionSpec(
                            Potions.SWIFTNESS,
                            HEDGE_WITCH
                    ),
                    new PotionSpec(
                            Potions.WATER_BREATHING,
                            HEDGE_WITCH
                    ),
                    new PotionSpec(
                            Potions.HEALING,
                            HEDGE_WITCH
                    ),

                    // ------------------------------------------------
                    // Cauldron Keeper
                    // ------------------------------------------------
                    new PotionSpec(
                            Potions.LONG_NIGHT_VISION,
                            CAULDRON_KEEPER
                    ),
                    new PotionSpec(
                            Potions.INVISIBILITY,
                            CAULDRON_KEEPER
                    ),
                    new PotionSpec(
                            Potions.LONG_LEAPING,
                            CAULDRON_KEEPER
                    ),
                    new PotionSpec(
                            Potions.STRONG_LEAPING,
                            CAULDRON_KEEPER
                    ),
                    new PotionSpec(
                            Potions.LONG_FIRE_RESISTANCE,
                            CAULDRON_KEEPER
                    ),
                    new PotionSpec(
                            Potions.LONG_SWIFTNESS,
                            CAULDRON_KEEPER
                    ),
                    new PotionSpec(
                            Potions.STRONG_SWIFTNESS,
                            CAULDRON_KEEPER
                    ),
                    new PotionSpec(
                            Potions.LONG_WATER_BREATHING,
                            CAULDRON_KEEPER
                    ),
                    new PotionSpec(
                            Potions.REGENERATION,
                            CAULDRON_KEEPER
                    ),
                    new PotionSpec(
                            Potions.STRENGTH,
                            CAULDRON_KEEPER
                    ),
                    new PotionSpec(
                            Potions.SLOW_FALLING,
                            CAULDRON_KEEPER
                    ),

                    // ------------------------------------------------
                    // Hexbinder
                    //
                    // Offensive brews begin here.
                    // ------------------------------------------------
                    new PotionSpec(
                            Potions.LONG_INVISIBILITY,
                            HEXBINDER
                    ),
                    new PotionSpec(
                            Potions.STRONG_HEALING,
                            HEXBINDER
                    ),
                    new PotionSpec(
                            Potions.POISON,
                            HEXBINDER
                    ),
                    new PotionSpec(
                            Potions.SLOWNESS,
                            HEXBINDER
                    ),
                    new PotionSpec(
                            Potions.WEAKNESS,
                            HEXBINDER
                    ),
                    new PotionSpec(
                            Potions.HARMING,
                            HEXBINDER
                    ),
                    new PotionSpec(
                            Potions.LONG_REGENERATION,
                            HEXBINDER
                    ),
                    new PotionSpec(
                            Potions.LONG_STRENGTH,
                            HEXBINDER
                    ),
                    new PotionSpec(
                            Potions.LONG_SLOW_FALLING,
                            HEXBINDER
                    ),

                    // ------------------------------------------------
                    // Coven Elder
                    // ------------------------------------------------
                    new PotionSpec(
                            Potions.STRONG_HARMING,
                            COVEN_ELDER
                    ),
                    new PotionSpec(
                            Potions.LONG_POISON,
                            COVEN_ELDER
                    ),
                    new PotionSpec(
                            Potions.STRONG_POISON,
                            COVEN_ELDER
                    ),
                    new PotionSpec(
                            Potions.LONG_SLOWNESS,
                            COVEN_ELDER
                    ),
                    new PotionSpec(
                            Potions.STRONG_SLOWNESS,
                            COVEN_ELDER
                    ),
                    new PotionSpec(
                            Potions.LONG_WEAKNESS,
                            COVEN_ELDER
                    ),
                    new PotionSpec(
                            Potions.STRONG_REGENERATION,
                            COVEN_ELDER
                    ),
                    new PotionSpec(
                            Potions.STRONG_STRENGTH,
                            COVEN_ELDER
                    ),
                    new PotionSpec(
                            Potions.TURTLE_MASTER,
                            COVEN_ELDER
                    ),
                    new PotionSpec(
                            Potions.LONG_TURTLE_MASTER,
                            COVEN_ELDER
                    ),
                    new PotionSpec(
                            Potions.STRONG_TURTLE_MASTER,
                            COVEN_ELDER
                    ),

                    // ------------------------------------------------
                    // High Witch
                    //
                    // Rare / unusual brews.
                    // ------------------------------------------------
                    new PotionSpec(
                            Potions.LUCK,
                            HIGH_WITCH
                    ),
                    new PotionSpec(
                            Potions.INFESTED,
                            HIGH_WITCH
                    ),
                    new PotionSpec(
                            Potions.OOZING,
                            HIGH_WITCH
                    ),
                    new PotionSpec(
                            Potions.WEAVING,
                            HIGH_WITCH
                    ),
                    new PotionSpec(
                            Potions.WIND_CHARGED,
                            HIGH_WITCH
                    )
            );

    private MobletWitchTrades() {
    }

    public static int rankForXp(
            int xp
    ) {
        if (xp >= HIGH_WITCH_XP) {
            return HIGH_WITCH;
        }

        if (xp >= COVEN_ELDER_XP) {
            return COVEN_ELDER;
        }

        if (xp >= HEXBINDER_XP) {
            return HEXBINDER;
        }

        if (xp >= CAULDRON_KEEPER_XP) {
            return CAULDRON_KEEPER;
        }

        return HEDGE_WITCH;
    }

    public static String rankName(
            int rank
    ) {
        return switch (rank) {
            case CAULDRON_KEEPER ->
                    "Cauldron Keeper";

            case HEXBINDER ->
                    "Hexbinder";

            case COVEN_ELDER ->
                    "Coven Elder";

            case HIGH_WITCH ->
                    "High Witch";

            default ->
                    "Hedge Witch";
        };
    }

    public static MerchantOffers createOffers(
            Witch witch,
            int currentRank
    ) {
        MerchantOffers offers =
                new MerchantOffers();

        /*
         * Each unlocked rank contributes its own three offers.
         *
         * The Witch UUID seeds every tier independently. This
         * means her inventory:
         *
         *   - differs from other witches,
         *   - stays stable when reopening the screen,
         *   - stays stable across world reloads,
         *   - preserves earlier-rank choices after promotion.
         */
        for (int tier = HEDGE_WITCH;
             tier <= currentRank;
             ++tier) {

            addRandomTierOffers(
                    offers,
                    witch,
                    tier
            );
        }

        return offers;
    }

    private static void addRandomTierOffers(
            MerchantOffers offers,
            Witch witch,
            int tier
    ) {
        List<TradeCandidate> candidates =
                new ArrayList<>();

        for (PotionSpec potion :
                POTIONS) {

            for (BottleKind bottle :
                    BottleKind.values()) {

                int effectiveTier =
                        Math.min(
                                HIGH_WITCH,
                                potion.tier()
                                        + bottle.tierOffset()
                        );

                if (effectiveTier != tier) {
                    continue;
                }

                candidates.add(
                        new TradeCandidate(
                                potion.potion(),
                                bottle,
                                effectiveTier
                        )
                );
            }
        }

        long seed =
                witch.getUUID()
                        .getMostSignificantBits()
                        ^ witch.getUUID()
                        .getLeastSignificantBits()
                        ^ (
                                0x9E3779B97F4A7C15L
                                        * tier
                        );

        RandomSource random =
                RandomSource.create(seed);

        /*
         * Fisher-Yates so we do not depend on java.util.Random.
         */
        for (int i = candidates.size() - 1;
             i > 0;
             --i) {

            int j =
                    random.nextInt(
                            i + 1
                    );

            TradeCandidate temp =
                    candidates.get(i);

            candidates.set(
                    i,
                    candidates.get(j)
            );

            candidates.set(
                    j,
                    temp
            );
        }

        int count =
                Math.min(
                        OFFERS_PER_TIER,
                        candidates.size()
                );

        for (int i = 0;
             i < count;
             ++i) {

            offers.add(
                    createOffer(
                            candidates.get(i),
                            random
                    )
            );
        }
    }

    private static MerchantOffer createOffer(
            TradeCandidate candidate,
            RandomSource random
    ) {
        int tier =
                candidate.tier();

        Item currency =
                chooseCurrency(
                        tier,
                        random
                );

        int currencyCount =
                currencyCount(
                        tier,
                        random
                );

        ItemCost primary =
                new ItemCost(
                        currency,
                        currencyCount
                );

        Optional<ItemCost> secondary =
                switch (candidate.bottle()) {
                    case DRINKABLE ->
                            Optional.empty();

                    case SPLASH ->
                            Optional.of(
                                    new ItemCost(
                                            Items.GUNPOWDER,
                                            1
                                    )
                            );

                    case LINGERING ->
                            Optional.of(
                                    new ItemCost(
                                            Items.GUNPOWDER,
                                            2
                                    )
                            );
                };

        var result =
                PotionContents.createItemStack(
                        candidate.bottle().item(),
                        candidate.potion()
                );

        int tradeXp =
                switch (tier) {
                    case CAULDRON_KEEPER -> 5;
                    case HEXBINDER -> 10;
                    case COVEN_ELDER -> 15;
                    case HIGH_WITCH -> 20;
                    default -> 2;
                };

        /*
         * Essentially permanent stock.
         *
         * The economy is limited by ingredients, not artificial
         * villager restock cycles.
         */
        return new MerchantOffer(
                primary,
                secondary,
                result,
                999,
                tradeXp,
                0.0F
        );
    }

    private static Item chooseCurrency(
            int tier,
            RandomSource random
    ) {
        return switch (tier) {
            case CAULDRON_KEEPER ->
                    switch (random.nextInt(3)) {
                        case 0 -> Items.SUGAR;
                        case 1 -> Items.REDSTONE;
                        default ->
                                Items.FERMENTED_SPIDER_EYE;
                    };

            case HEXBINDER ->
                    random.nextBoolean()
                            ? Items.REDSTONE
                            : Items.FERMENTED_SPIDER_EYE;

            case COVEN_ELDER ->
                    random.nextBoolean()
                            ? Items.FERMENTED_SPIDER_EYE
                            : Items.GLOWSTONE_DUST;

            case HIGH_WITCH ->
                    random.nextInt(3) == 0
                            ? Items.FERMENTED_SPIDER_EYE
                            : Items.GLOWSTONE_DUST;

            default ->
                    random.nextBoolean()
                            ? Items.SUGAR
                            : Items.REDSTONE;
        };
    }

    private static int currencyCount(
            int tier,
            RandomSource random
    ) {
        return switch (tier) {
            case CAULDRON_KEEPER ->
                    2 + random.nextInt(2);

            case HEXBINDER ->
                    2 + random.nextInt(3);

            case COVEN_ELDER ->
                    3 + random.nextInt(3);

            case HIGH_WITCH ->
                    4 + random.nextInt(3);

            default ->
                    1 + random.nextInt(2);
        };
    }

    private record PotionSpec(
            Holder<Potion> potion,
            int tier
    ) {
    }

    private record TradeCandidate(
            Holder<Potion> potion,
            BottleKind bottle,
            int tier
    ) {
    }

    private enum BottleKind {
        DRINKABLE(
                Items.POTION,
                0
        ),

        SPLASH(
                Items.SPLASH_POTION,
                0
        ),

        /*
         * Lingering versions unlock one Witch rank later than
         * their drinkable/splash equivalent.
         */
        LINGERING(
                Items.LINGERING_POTION,
                1
        );

        private final Item item;
        private final int tierOffset;

        BottleKind(
                Item item,
                int tierOffset
        ) {
            this.item = item;
            this.tierOffset =
                    tierOffset;
        }

        public Item item() {
            return this.item;
        }

        public int tierOffset() {
            return this.tierOffset;
        }
    }
}

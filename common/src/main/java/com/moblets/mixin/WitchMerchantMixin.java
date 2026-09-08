package com.moblets.mixin;

import com.moblets.BabyWitches;
import com.moblets.taming.MobletTameState;
import com.moblets.taming.MobletWitchMerchant;
import com.moblets.taming.MobletWitchTrades;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Witch.class)
public abstract class WitchMerchantMixin
        extends Raider
        implements MobletWitchMerchant {

    @Unique
    private static final int MOBLETS_PROMOTION_DELAY_TICKS =
            40;

    @Unique
    private Player moblets$tradingPlayer;

    @Unique
    private int moblets$pendingPromotionTicks;

    @Unique
    private MerchantOffers moblets$witchOffers;

    @Unique
    private int moblets$witchOffersRank;

    @Unique
    private int moblets$witchTradeXp;

    protected WitchMerchantMixin(
            EntityType<? extends Raider> entityType,
            Level level
    ) {
        super(
                entityType,
                level
        );
    }

    @Override
    public void moblets$openWitchShop(
            Player player
    ) {
        Witch witch =
                (Witch) (Object) this;

        if (!BabyWitches.isBaby(witch)
                || !(witch instanceof MobletTameState state)
                || !state.moblets$isTamed()) {
            return;
        }

        int rank =
                MobletWitchTrades.rankForXp(
                        this.moblets$witchTradeXp
                );

        /*
         * Refresh only when a newly unlocked rank changes the
         * available tier pool.
         */
        moblets$ensureOffers(
                rank
        );

        /*
         * Merchant.openTradingScreen does not give our Witch the
         * same AbstractVillager lifecycle automatically, so make
         * the active customer explicit before opening the menu.
         */
        this.setTradingPlayer(
                player
        );

        this.getNavigation().stop();
        this.getMoveControl().setWait();

        this.openTradingScreen(
                player,
                Component.literal(
                        "Witch - "
                                + MobletWitchTrades.rankName(
                                        rank
                                )
                ),
                rank
        );
    }

    @Unique
    private void moblets$ensureOffers(
            int rank
    ) {
        if (this.moblets$witchOffers != null
                && this.moblets$witchOffersRank == rank) {
            return;
        }

        Witch witch =
                (Witch) (Object) this;

        this.moblets$witchOffers =
                MobletWitchTrades.createOffers(
                        witch,
                        rank
                );

        this.moblets$witchOffersRank =
                rank;
    }

    @Override
    public void setTradingPlayer(
            Player player
    ) {
        this.moblets$tradingPlayer =
                player;

        /*
         * Stop immediately when a customer begins trading.
         * The aiStep hook below keeps her planted afterward.
         */
        if (player != null) {
            this.getNavigation().stop();
            this.getMoveControl().setWait();

            this.xxa = 0.0F;
            this.zza = 0.0F;

            var movement =
                    this.getDeltaMovement();

            this.setDeltaMovement(
                    0.0D,
                    movement.y,
                    0.0D
            );
        }
    }

    @Override
    public Player getTradingPlayer() {
        return this.moblets$tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        int rank =
                MobletWitchTrades.rankForXp(
                        this.moblets$witchTradeXp
                );

        moblets$ensureOffers(
                rank
        );

        return this.moblets$witchOffers;
    }

    @Override
    public void overrideOffers(
            MerchantOffers offers
    ) {
        this.moblets$witchOffers =
                offers;

        this.moblets$witchOffersRank =
                MobletWitchTrades.rankForXp(
                        this.moblets$witchTradeXp
                );
    }

    @Override
    public void notifyTrade(
            MerchantOffer offer
    ) {
        int oldRank =
                MobletWitchTrades.rankForXp(
                        this.moblets$witchTradeXp
                );

        offer.increaseUses();

        this.moblets$witchTradeXp +=
                offer.getXp();

        int newRank =
                MobletWitchTrades.rankForXp(
                        this.moblets$witchTradeXp
                );

        if (newRank > oldRank) {
            /*
             * Rebuild on the next offer request so the freshly
             * unlocked tier appears.
             */
            this.moblets$witchOffersRank = 0;

            /*
             * Villagers don't explode into promotion particles
             * the instant the final trade is clicked. Give the
             * player time to leave the merchant screen first.
             *
             * The timer itself does not count down until there
             * is no active customer.
             */
            this.moblets$pendingPromotionTicks =
                    MOBLETS_PROMOTION_DELAY_TICKS;
        }
    }

    @Override
    public void notifyTradeUpdated(
            ItemStack itemStack
    ) {
        /*
         * Merchant requires this callback. The Witch does not
         * need villager-style yes/no preview sounds.
         */
    }

    @Override
    public int getVillagerXp() {
        return this.moblets$witchTradeXp;
    }

    @Override
    public void overrideXp(
            int xp
    ) {
        int oldRank =
                MobletWitchTrades.rankForXp(
                        this.moblets$witchTradeXp
                );

        this.moblets$witchTradeXp =
                Math.max(
                        0,
                        xp
                );

        int newRank =
                MobletWitchTrades.rankForXp(
                        this.moblets$witchTradeXp
                );

        if (newRank != oldRank) {
            this.moblets$witchOffersRank = 0;
        }
    }

    @Override
    public boolean showProgressBar() {
        return true;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WITCH_CELEBRATE;
    }

    @Override
    public boolean isClientSide() {
        return this.level()
                .isClientSide();
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        Witch witch =
                (Witch) (Object) this;

        return witch.isAlive()
                && witch.distanceToSqr(
                        player
                ) <= 64.0D;
    }

    /*
     * Don't let her wander away while somebody has the
     * merchant screen open.
     */
    @Inject(
            method = "aiStep",
            at = @At("TAIL")
    )
    private void moblets$merchantAiStep(
            CallbackInfo ci
    ) {
        Player customer =
                this.moblets$tradingPlayer;

        /*
         * While a merchant screen is open, behave like a proper
         * trader:
         *
         *   - no wandering
         *   - no Follow movement
         *   - no residual sliding
         *   - keep attention on the customer
         */
        if (customer != null) {
            this.getNavigation().stop();
            this.getMoveControl().setWait();

            this.xxa = 0.0F;
            this.zza = 0.0F;

            var movement =
                    this.getDeltaMovement();

            this.setDeltaMovement(
                    0.0D,
                    movement.y,
                    0.0D
            );

            this.getLookControl()
                    .setLookAt(
                            customer,
                            30.0F,
                            30.0F
                    );

            /*
             * Promotion delay deliberately pauses while the GUI
             * remains open, guaranteeing the player can actually
             * see the celebration afterward.
             */
            return;
        }

        if (this.moblets$pendingPromotionTicks <= 0) {
            return;
        }

        --this.moblets$pendingPromotionTicks;

        if (this.moblets$pendingPromotionTicks == 0) {
            moblets$celebratePromotion();
        }
    }

    @Unique
    private void moblets$celebratePromotion() {
        Witch witch =
                (Witch) (Object) this;

        /*
         * Use the Witch's normal ambient laugh rather than a
         * villager rank-up sound.
         */
        /*
         * Promotion cackle.
         *
         * Use the Witch celebration vocalization and explicitly
         * pitch it up to the baby-Moblet voice range.
         */
        witch.playSound(
                SoundEvents.WITCH_CELEBRATE,
                1.0F,
                1.50F
        );

        if (witch.level()
                instanceof ServerLevel serverLevel) {

            serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    witch.getX(),
                    witch.getY()
                            + witch.getBbHeight()
                            * 0.65D,
                    witch.getZ(),
                    30,
                    0.45D,
                    0.55D,
                    0.45D,
                    0.05D
            );
        }
    }

}

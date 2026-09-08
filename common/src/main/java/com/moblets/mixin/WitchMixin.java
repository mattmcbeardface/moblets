package com.moblets.mixin;

import com.moblets.BabyWitches;
import com.moblets.taming.MobletWitchFleeGoal;
import com.moblets.taming.MobletTameState;
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import java.util.UUID;
import com.moblets.taming.MobletWitchMerchant;
import com.moblets.taming.MobletCuriosityGoal;
import com.moblets.taming.MobletFollowOwnerGoal;
import com.moblets.taming.MobletWitchStayWanderGoal;
import com.moblets.taming.MobletWitchTradeAttentionGoal;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Witch.class)
public abstract class WitchMixin
        extends Raider {

    @Unique
    private static final float MOBLETS_SUPPORT_HEALTH_THRESHOLD =
            0.50F;

    @Unique
    private static final double MOBLETS_SUPPORT_RANGE_SQR =
            100.0D;

    @Unique
    private static final int MOBLETS_SUPPORT_COOLDOWN_TICKS =
            200;

    @Unique
    private int moblets$nextSupportHealTick;


    protected WitchMixin(
            EntityType<? extends Raider> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    /*
     * Baby Witches use the same child voice treatment as the
     * other Moblets. This affects ordinary ambient, hurt, death,
     * celebrate, and other entity-driven Witch vocalizations.
     */
    @Override
    public float getVoicePitch() {
        float vanillaPitch =
                super.getVoicePitch();

        Witch witch =
                (Witch) (Object) this;

        if (BabyWitches.isBaby(witch)) {
            return vanillaPitch * 1.50F;
        }

        return vanillaPitch;
    }


    /*
     * Direct support behavior.
     *
     * We deliberately run this outside the GoalSelector so
     * vanilla Witch combat AI cannot prevent the healing action
     * from being scheduled.
     */
    @Inject(
            method = "aiStep",
            at = @At("TAIL")
    )
    private void moblets$runSupportHealing(
            CallbackInfo ci
    ) {
        Witch witch =
                (Witch) (Object) this;

        if (witch.level().isClientSide()
                || !BabyWitches.isBaby(witch)) {

            return;
        }

        MobletTameState state =
                (MobletTameState) witch;

        if (!state.moblets$isTamed()) {
            return;
        }

        /*
         * Support Witch never maintains a combat target.
         *
         * The performRangedAttack injection above is the final
         * safety net, but clearing the target also prevents her
         * from chasing hostiles unnecessarily.
         */
        if (witch.getTarget() != null) {
            witch.setTarget(null);
            witch.getNavigation().stop();
        }

        /*
         * Stay mode is merchant mode:
         *
         *   wander
         *   trade
         *   no combat
         *   no support throwing
         */
        if (state.moblets$isOrderedToStay()) {
            return;
        }

        /*
         * Don't throw healing potions while someone is actively
         * using her merchant screen.
         */
        if (((MobletWitchMerchant) witch)
                .getTradingPlayer() != null) {

            return;
        }

        /*
         * Preserve normal Witch self-drinking behavior.
         */
        if (witch.isDrinkingPotion()) {
            return;
        }

        if (witch.tickCount
                < this.moblets$nextSupportHealTick) {

            return;
        }

        UUID ownerUuid =
                state.moblets$getOwnerUuid();

        if (ownerUuid == null) {
            return;
        }

        Player owner =
                witch.level()
                        .getPlayerByUUID(
                                ownerUuid
                        );

        if (owner == null
                || !owner.isAlive()) {

            return;
        }

        /*
         * Strictly BELOW half health.
         *
         * 10/20 HP does not trigger.
         * 9.5/20 HP does.
         */
        if (owner.getHealth()
                >= owner.getMaxHealth()
                * MOBLETS_SUPPORT_HEALTH_THRESHOLD) {

            return;
        }

        if (witch.distanceToSqr(owner)
                > MOBLETS_SUPPORT_RANGE_SQR) {

            return;
        }

        if (!witch.getSensing()
                .hasLineOfSight(owner)) {

            return;
        }

        if (!(witch.level()
                instanceof ServerLevel serverLevel)) {

            return;
        }

        ItemStack potionStack =
                PotionContents.createItemStack(
                        Items.SPLASH_POTION,
                        Potions.STRONG_HEALING
                );

        ThrownSplashPotion potion =
                new ThrownSplashPotion(
                        serverLevel,
                        witch,
                        potionStack
                );

        double dx =
                owner.getX()
                        - potion.getX();

        double dz =
                owner.getZ()
                        - potion.getZ();

        double horizontalDistance =
                Math.sqrt(
                        dx * dx
                                + dz * dz
                );

        /*
         * Aim slightly low so the splash breaks near the
         * player's body/feet rather than sailing overhead.
         */
        double dy =
                owner.getEyeY()
                        - 0.75D
                        - potion.getY()
                        + horizontalDistance
                        * 0.20D;

        witch.getLookControl()
                .setLookAt(
                        owner,
                        30.0F,
                        30.0F
                );

        Projectile.spawnProjectileUsingShoot(
                potion,
                serverLevel,
                potionStack,
                dx,
                dy,
                dz,
                0.75F,
                0.10F
        );

        this.moblets$nextSupportHealTick =
                witch.tickCount
                        + MOBLETS_SUPPORT_COOLDOWN_TICKS;
    }

    @Inject(
            method = "registerGoals",
            at = @At("TAIL")
    )
    private void moblets$addWitchCompanionGoals(
            CallbackInfo ci
    ) {
        Witch witch =
                (Witch) (Object) this;

        this.goalSelector.addGoal(
                0,
                new MobletWitchFleeGoal(
                        witch
                )
        );

        this.goalSelector.addGoal(
                1,
                new MobletWitchTradeAttentionGoal(
                        witch
                )
        );

        this.goalSelector.addGoal(
                1,
                new MobletWitchStayWanderGoal(
                        witch
                )
        );

        /*
         * Wild baby Witch curiosity/taming behavior.
         */
        this.goalSelector.addGoal(
                1,
                new MobletCuriosityGoal(
                        witch
                )
        );

        /*
         * Tamed Follow behavior.
         */
        this.goalSelector.addGoal(
                3,
                new MobletFollowOwnerGoal(
                        witch
                )
        );
    }

    /*
     * Tamed baby Witches are pure support companions.
     *
     * Absolutely no vanilla Witch ranged attack is allowed.
     * This prevents Harming, Poison, Slowness, Weakness, and
     * vanilla raider-support throws regardless of which AI goal
     * attempted to initiate the attack.
     */
    @Inject(
            method = "performRangedAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void moblets$disableTamedWitchOffense(
            LivingEntity target,
            float power,
            CallbackInfo ci
    ) {
        Witch witch =
                (Witch) (Object) this;

        if (BabyWitches.isBaby(witch)
                && ((MobletTameState) witch)
                        .moblets$isTamed()) {

            ci.cancel();
        }
    }

    @ModifyArg(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"
            ),
            index = 8
    )
    private float babyMobs$reduceBabyWitchAccuracy(
            float vanillaInaccuracy
    ) {
        Witch witch =
                (Witch) (Object) this;

        if (BabyWitches.isBaby(witch)) {
            return 40.0F;
        }

        return vanillaInaccuracy;
    }

    @ModifyArg(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"
            ),
            index = 2
    )
    private ItemStack babyMobs$weakenBabyWitchTimedPotions(
            ItemStack potionStack
    ) {
        Witch witch =
                (Witch) (Object) this;

        if (BabyWitches.isBaby(witch)) {
            potionStack.set(
                    DataComponents.POTION_DURATION_SCALE,
                    0.5F
            );
        }

        return potionStack;
    }
}

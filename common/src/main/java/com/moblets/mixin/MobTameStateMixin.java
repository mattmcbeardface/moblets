package com.moblets.mixin;

import java.util.UUID;

import net.minecraft.core.BlockPos;

import com.moblets.taming.MobletTameState;
import com.moblets.taming.MobletTaming;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobTameStateMixin
        implements MobletTameState {

    @Unique
    private static final String MOBLETS_OWNER_KEY =
            "MobletsOwner";

    @Unique
    private static final String MOBLETS_STAY_KEY =
            "MobletsStay";

    @Unique
    private static final String MOBLETS_STAY_ANCHOR_SET_KEY =
            "MobletsStayAnchorSet";

    @Unique
    private static final String MOBLETS_STAY_ANCHOR_X_KEY =
            "MobletsStayAnchorX";

    @Unique
    private static final String MOBLETS_STAY_ANCHOR_Y_KEY =
            "MobletsStayAnchorY";

    @Unique
    private static final String MOBLETS_STAY_ANCHOR_Z_KEY =
            "MobletsStayAnchorZ";

    @Unique
    private static final double MOBLETS_STAY_COMBAT_RADIUS =
            3.0D;

    @Unique
    private static final double MOBLETS_STAY_COMBAT_RADIUS_SQR =
            MOBLETS_STAY_COMBAT_RADIUS
                    * MOBLETS_STAY_COMBAT_RADIUS;

    @Unique
    private UUID moblets$ownerUuid;

    @Unique
    private UUID moblets$curiousPlayerUuid;

    @Unique
    private UUID moblets$consideringPlayerUuid;

    @Unique
    private boolean moblets$pendingTameSuccess;

    @Unique
    private int moblets$consideringTicks;

    @Unique
    private boolean moblets$orderedToStay;

    @Unique
    private BlockPos moblets$stayAnchor;

    @Override
    public boolean moblets$isTamed() {
        return moblets$ownerUuid != null;
    }

    @Override
    public UUID moblets$getOwnerUuid() {
        return moblets$ownerUuid;
    }

    @Override
    public void moblets$setOwnerUuid(
            UUID ownerUuid
    ) {
        this.moblets$ownerUuid = ownerUuid;
    }

    @Override
    public UUID moblets$getCuriousPlayerUuid() {
        return moblets$curiousPlayerUuid;
    }

    @Override
    public void moblets$setCuriousPlayerUuid(
            UUID playerUuid
    ) {
        this.moblets$curiousPlayerUuid =
                playerUuid;
    }

    @Override
    public boolean moblets$isConsideringTame() {
        return this.moblets$consideringPlayerUuid
                != null;
    }

    @Override
    public UUID moblets$getConsideringPlayerUuid() {
        return this.moblets$consideringPlayerUuid;
    }

    @Override
    public void moblets$beginConsideringTame(
            UUID playerUuid,
            boolean success,
            int ticks
    ) {
        this.moblets$consideringPlayerUuid =
                playerUuid;
        this.moblets$pendingTameSuccess =
                success;
        this.moblets$consideringTicks =
                ticks;
    }

    @Override
    public boolean moblets$getPendingTameSuccess() {
        return this.moblets$pendingTameSuccess;
    }

    @Override
    public int moblets$getConsideringTicks() {
        return this.moblets$consideringTicks;
    }

    @Override
    public void moblets$setConsideringTicks(
            int ticks
    ) {
        this.moblets$consideringTicks = ticks;
    }

    @Override
    public void moblets$clearConsideringTame() {
        this.moblets$consideringPlayerUuid = null;
        this.moblets$pendingTameSuccess = false;
        this.moblets$consideringTicks = 0;
    }

    @Override
    public boolean moblets$isOrderedToStay() {
        return this.moblets$orderedToStay;
    }

    @Override
    public void moblets$setOrderedToStay(
            boolean stay
    ) {
        this.moblets$orderedToStay = stay;
    }

    @Override
    public BlockPos moblets$getStayAnchor() {
        return this.moblets$stayAnchor;
    }

    @Override
    public void moblets$setStayAnchor(
            BlockPos anchor
    ) {
        this.moblets$stayAnchor =
                anchor == null
                        ? null
                        : anchor.immutable();
    }

    @Inject(
            method = "interact",
            at = @At("HEAD"),
            cancellable = true
    )
    private void moblets$handleTaming(
            Player player,
            InteractionHand hand,
            Vec3 hitPosition,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        Mob mob =
                (Mob) (Object) this;

        /*
         * Owner-only empty-hand interaction toggles the
         * companion between Follow and Stay.
         */
        if (hand == InteractionHand.MAIN_HAND
                && moblets$isOwnedBy(player)
                && player.getItemInHand(hand).isEmpty()) {

            if (!mob.level().isClientSide()) {
                this.moblets$orderedToStay =
                        !this.moblets$orderedToStay;

                if (this.moblets$orderedToStay) {
                    this.moblets$stayAnchor =
                            mob.blockPosition().immutable();
                } else {
                    this.moblets$stayAnchor = null;
                }

                mob.setTarget(null);
                mob.getNavigation().stop();

                player.sendOverlayMessage(
                        Component.literal(
                                this.moblets$orderedToStay
                                        ? "Moblet is staying."
                                        : "Moblet is following."
                        )
                );
            }

            cir.setReturnValue(
                    InteractionResult.SUCCESS
            );
            return;
        }

        InteractionResult result =
                MobletTaming.tryTame(
                        mob,
                        player,
                        hand
                );

        if (result != InteractionResult.PASS) {
            cir.setReturnValue(result);
        }
    }

    @Inject(
            method = "setTarget",
            at = @At("HEAD"),
            cancellable = true
    )
    private void moblets$protectOwner(
            LivingEntity target,
            CallbackInfo ci
    ) {
        /*
         * Tamed Moblets are friendly companions, not PvP weapons.
         * Once tamed, they must never acquire any player as an
         * attack target, including players other than their owner.
         */
        if (moblets$isTamed()
                && target instanceof Player) {
            ci.cancel();
        }
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void moblets$enforceStayCombatLeash(
            CallbackInfo ci
    ) {
        Mob mob =
                (Mob) (Object) this;

        if (!this.moblets$isTamed()
                || !this.moblets$orderedToStay
                || this.moblets$stayAnchor == null) {
            return;
        }

        LivingEntity target =
                mob.getTarget();

        if (target == null
                || !target.isAlive()
                || target instanceof Player) {
            return;
        }

        double anchorX =
                this.moblets$stayAnchor.getX() + 0.5D;

        double anchorY =
                this.moblets$stayAnchor.getY();

        double anchorZ =
                this.moblets$stayAnchor.getZ() + 0.5D;

        double distanceSqr =
                mob.distanceToSqr(
                        anchorX,
                        anchorY,
                        anchorZ
                );

        /*
         * During combat, vanilla Skeleton bow AI may strafe
         * and reposition normally inside the sentry radius.
         *
         * Once it crosses the three-block leash, redirect it
         * toward its assigned post instead of letting it chase.
         */
        if (distanceSqr
                > MOBLETS_STAY_COMBAT_RADIUS_SQR) {

            mob.getNavigation().moveTo(
                    anchorX,
                    anchorY,
                    anchorZ,
                    1.0D
            );
        }
    }

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("TAIL")
    )
    private void moblets$saveOwnership(
            ValueOutput output,
            CallbackInfo ci
    ) {
        if (this.moblets$ownerUuid != null) {
            output.putString(
                    MOBLETS_OWNER_KEY,
                    this.moblets$ownerUuid.toString()
            );

            output.putBoolean(
                    MOBLETS_STAY_KEY,
                    this.moblets$orderedToStay
            );

            if (this.moblets$stayAnchor != null) {
                output.putBoolean(
                        MOBLETS_STAY_ANCHOR_SET_KEY,
                        true
                );

                output.putInt(
                        MOBLETS_STAY_ANCHOR_X_KEY,
                        this.moblets$stayAnchor.getX()
                );

                output.putInt(
                        MOBLETS_STAY_ANCHOR_Y_KEY,
                        this.moblets$stayAnchor.getY()
                );

                output.putInt(
                        MOBLETS_STAY_ANCHOR_Z_KEY,
                        this.moblets$stayAnchor.getZ()
                );
            }
        }
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void moblets$loadOwnership(
            ValueInput input,
            CallbackInfo ci
    ) {
        String value =
                input.getStringOr(
                        MOBLETS_OWNER_KEY,
                        ""
                );

        if (value.isBlank()) {
            this.moblets$ownerUuid = null;
        } else {
            try {
                this.moblets$ownerUuid =
                        UUID.fromString(value);
            } catch (IllegalArgumentException ignored) {
                this.moblets$ownerUuid = null;
            }
        }

        this.moblets$orderedToStay =
                input.getBooleanOr(
                        MOBLETS_STAY_KEY,
                        false
                );

        if (this.moblets$orderedToStay
                && input.getBooleanOr(
                        MOBLETS_STAY_ANCHOR_SET_KEY,
                        false
                )) {

            this.moblets$stayAnchor =
                    new BlockPos(
                            input.getIntOr(
                                    MOBLETS_STAY_ANCHOR_X_KEY,
                                    0
                            ),
                            input.getIntOr(
                                    MOBLETS_STAY_ANCHOR_Y_KEY,
                                    0
                            ),
                            input.getIntOr(
                                    MOBLETS_STAY_ANCHOR_Z_KEY,
                                    0
                            )
                    );
        } else {
            this.moblets$stayAnchor = null;
        }

        /*
         * Curiosity and consideration are transient states.
         */
        this.moblets$curiousPlayerUuid = null;
        moblets$clearConsideringTame();
    }
}

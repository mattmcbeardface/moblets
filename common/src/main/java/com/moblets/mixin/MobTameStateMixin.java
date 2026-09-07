package com.moblets.mixin;

import java.util.UUID;

import com.moblets.taming.MobletTameState;
import com.moblets.taming.MobletTaming;

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
    private UUID moblets$ownerUuid;

    @Unique
    private UUID moblets$curiousPlayerUuid;

    @Unique
    private UUID moblets$consideringPlayerUuid;

    @Unique
    private boolean moblets$pendingTameSuccess;

    @Unique
    private int moblets$consideringTicks;

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
        InteractionResult result =
                MobletTaming.tryTame(
                        (Mob) (Object) this,
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
        if (target instanceof Player player
                && moblets$isOwnedBy(player)) {
            ci.cancel();
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

        /*
         * Curiosity and consideration are transient states.
         */
        this.moblets$curiousPlayerUuid = null;
        moblets$clearConsideringTame();
    }
}

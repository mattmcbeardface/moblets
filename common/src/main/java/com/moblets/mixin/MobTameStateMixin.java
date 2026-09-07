package com.moblets.mixin;

import java.util.UUID;

import net.minecraft.core.BlockPos;

import com.moblets.BabySkeletons;
import com.moblets.taming.MobletTameState;
import com.moblets.taming.MobletTaming;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    private static final double MOBLETS_STAY_ENGAGEMENT_RADIUS =
            16.0D;

    @Unique
    private static final double MOBLETS_STAY_ENGAGEMENT_RADIUS_SQR =
            MOBLETS_STAY_ENGAGEMENT_RADIUS
                    * MOBLETS_STAY_ENGAGEMENT_RADIUS;

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
         * Skeleton Moblet armor interaction.
         *
         * Ownership is server-authoritative and is not currently
         * synchronized to the client. Therefore the client must
         * recognize an armor-on-Moblet interaction without using
         * moblets$isOwnedBy(), otherwise it falls through to the
         * armor item's normal use action and equips the player.
         *
         * The server still performs the actual ownership check
         * before changing any equipment.
         */
        if (hand == InteractionHand.MAIN_HAND
                && mob instanceof AbstractSkeleton skeleton
                && BabySkeletons.isBaby(skeleton)) {

            ItemStack held =
                    player.getItemInHand(hand);

            if (!held.isEmpty()) {
                EquipmentSlot slot =
                        mob.getEquipmentSlotForItem(
                                held
                        );

                if (slot.isArmor()
                        && mob.isEquippableInSlot(
                                held,
                                slot)) {

                    /*
                     * Client:
                     *
                     * Consume the entity interaction as an ITEM
                     * interaction. This prevents Minecraft from
                     * subsequently invoking the armor's ordinary
                     * self-equip action on the player.
                     */
                    if (mob.level().isClientSide()) {
                        cir.setReturnValue(
                                InteractionResult.SUCCESS
                        );
                        return;
                    }

                    /*
                     * Server:
                     *
                     * Only the actual owner may change Moblet
                     * equipment.
                     */
                    if (!moblets$isOwnedBy(player)) {
                        cir.setReturnValue(
                                InteractionResult.CONSUME
                        );
                        return;
                    }

                    ItemStack previous =
                            mob.getItemBySlot(slot);

                    /*
                     * Preserve durability, enchantments, trims,
                     * custom components, etc.
                     */
                    ItemStack equipped =
                            held.copyWithCount(1);

                    mob.setItemSlot(
                            slot,
                            equipped
                    );

                    /*
                     * Player-supplied companion equipment should
                     * reliably drop if the Moblet dies.
                     */
                    mob.setGuaranteedDrop(slot);

                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }

                    /*
                     * Replacing an existing piece returns the old
                     * armor directly to the owner.
                     */
                    if (!previous.isEmpty()) {
                        ItemStack returned =
                                previous.copy();

                        if (!player.addItem(returned)) {
                            player.drop(
                                    returned,
                                    false
                            );
                        }
                    }

                    cir.setReturnValue(
                            InteractionResult.SUCCESS_SERVER
                    );
                    return;
                }
            }
        }

        /*
         * Owner-only sneak + empty-hand interaction removes all
         * equipped armor from a Skeleton Moblet.
         *
         * This is deliberately separate from the ordinary
         * empty-hand Follow/Stay interaction.
         */
        if (hand == InteractionHand.MAIN_HAND
                && moblets$isOwnedBy(player)
                && mob instanceof AbstractSkeleton
                && player.isShiftKeyDown()
                && player.getItemInHand(hand).isEmpty()) {

            if (!mob.level().isClientSide()) {
                boolean removedAny = false;

                EquipmentSlot[] armorSlots = {
                        EquipmentSlot.HEAD,
                        EquipmentSlot.CHEST,
                        EquipmentSlot.LEGS,
                        EquipmentSlot.FEET
                };

                for (EquipmentSlot slot : armorSlots) {
                    ItemStack equipped =
                            mob.getItemBySlot(slot);

                    if (equipped.isEmpty()) {
                        continue;
                    }

                    ItemStack returned =
                            equipped.copy();

                    mob.setItemSlot(
                            slot,
                            ItemStack.EMPTY
                    );

                    if (!player.addItem(returned)) {
                        player.drop(
                                returned,
                                false
                        );
                    }

                    removedAny = true;
                }

                player.sendOverlayMessage(
                        Component.literal(
                                removedAny
                                        ? "Moblet armor removed."
                                        : "Moblet has no armor."
                        )
                );
            }

            cir.setReturnValue(
                    InteractionResult.SUCCESS.withoutItem()
            );
            return;
        }

        /*
         * Owner-only empty-hand interaction toggles the
         * companion between Follow and Stay.
         */
        if (hand == InteractionHand.MAIN_HAND
                && moblets$isOwnedBy(player)
                && !player.isShiftKeyDown()
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
                    InteractionResult.SUCCESS.withoutItem()
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

        if (target == null) {
            return;
        }

        /*
         * Tamed Moblets never attack players.
         */
        if (!target.isAlive()
                || target instanceof Player) {

            mob.setTarget(null);
            mob.getNavigation().stop();
            return;
        }

        double anchorX =
                this.moblets$stayAnchor.getX() + 0.5D;

        double anchorZ =
                this.moblets$stayAnchor.getZ() + 0.5D;

        double dx =
                target.getX() - anchorX;

        double dz =
                target.getZ() - anchorZ;

        double horizontalDistanceSqr =
                dx * dx + dz * dz;

        /*
         * Vertical distance is intentionally ignored.
         *
         * A sentry on a wall or tower may engage something
         * below it, but will not pursue a target horizontally
         * outside the defended area.
         */
        if (horizontalDistanceSqr
                > MOBLETS_STAY_ENGAGEMENT_RADIUS_SQR) {

            mob.setTarget(null);
            mob.getNavigation().stop();
            return;
        }

    }

    /*
     * Tamed Moblets use a deliberately conservative fall
     * tolerance. This influences vanilla path generation in
     * Follow as well as Stay mode.
     */
    @Inject(
            method = "getMaxFallDistance",
            at = @At("HEAD"),
            cancellable = true
    )
    private void moblets$limitTamedFallDistance(
            CallbackInfoReturnable<Integer> cir
    ) {
        if (this.moblets$isTamed()) {
            cir.setReturnValue(2);
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

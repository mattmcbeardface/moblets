package com.moblets.mixin;

import java.util.UUID;

import net.minecraft.core.BlockPos;

import com.moblets.BabySkeletons;
import com.moblets.BabyCreepers;
import com.moblets.BabyPillagers;
import com.moblets.taming.MobletTameState;
import com.moblets.taming.MobletTaming;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
         * Owner healing / health inspection for Moblet companions.
         *
         * Normal right-click with a bone:
         *   heals 4 HP and consumes one bone in Survival.
         *
         * Sneak + right-click with a bone:
         *   reports health without healing or consuming.
         */
        if (hand == InteractionHand.MAIN_HAND
                && (
                        mob instanceof AbstractSkeleton skeleton
                                && BabySkeletons.isBaby(skeleton)
                                && player.getItemInHand(hand)
                                        .is(Items.BONE)
                        ||
                        mob instanceof Creeper creeper
                                && BabyCreepers.isBaby(creeper)
                                && player.getItemInHand(hand)
                                        .is(Items.GUNPOWDER)
                        ||
                        mob instanceof Pillager pillager
                                && BabyPillagers.isBaby(pillager)
                                && player.getItemInHand(hand)
                                        .is(Items.GOLD_NUGGET)
                )) {

            /*
             * Client recognizes the interaction immediately so
             * the held item cannot fall through into some other
             * use behavior. Ownership remains server-authoritative.
             */
            if (mob.level().isClientSide()) {
                cir.setReturnValue(
                        InteractionResult.SUCCESS
                );
                return;
            }

            if (!moblets$isOwnedBy(player)) {
                cir.setReturnValue(
                        InteractionResult.CONSUME
                );
                return;
            }

            float health =
                    mob.getHealth();

            float maxHealth =
                    mob.getMaxHealth();

            /*
             * Sneaking with a bone is the non-destructive health
             * inspection action.
             */
            if (player.isShiftKeyDown()) {
                player.sendOverlayMessage(
                        Component.literal(
                                "Moblet health: "
                                        + Math.round(health)
                                        + " / "
                                        + Math.round(maxHealth)
                                        + " HP"
                        )
                );

                cir.setReturnValue(
                        InteractionResult.SUCCESS_SERVER
                );
                return;
            }

            /*
             * Do not waste a bone when already at full health.
             */
            if (health >= maxHealth) {
                player.sendOverlayMessage(
                        Component.literal(
                                "Moblet health: "
                                        + Math.round(health)
                                        + " / "
                                        + Math.round(maxHealth)
                                        + " HP"
                        )
                );

                cir.setReturnValue(
                        InteractionResult.SUCCESS_SERVER
                );
                return;
            }

            float before =
                    health;

            mob.heal(4.0F);

            float after =
                    mob.getHealth();

            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }

            player.sendOverlayMessage(
                    Component.literal(
                            "Moblet healed: "
                                    + Math.round(before)
                                    + " -> "
                                    + Math.round(after)
                                    + " / "
                                    + Math.round(maxHealth)
                                    + " HP"
                    )
            );

            cir.setReturnValue(
                    InteractionResult.SUCCESS_SERVER
            );
            return;
        }

        /*
         * Baby Creeper helmet interaction.
         *
         * Creeper companions may wear HEAD equipment only.
         * Chestplates, leggings and boots deliberately fall
         * through to the ordinary Follow / Stay interaction.
         */
        if (hand == InteractionHand.MAIN_HAND
                && mob instanceof Creeper creeper
                && BabyCreepers.isBaby(creeper)) {

            ItemStack held =
                    player.getItemInHand(hand);

            if (!held.isEmpty()) {
                EquipmentSlot slot =
                        mob.getEquipmentSlotForItem(
                                held
                        );

                if (slot == EquipmentSlot.HEAD
                        && mob.isEquippableInSlot(
                                held,
                                EquipmentSlot.HEAD
                        )) {

                    /*
                     * Consume client-side so a helmet does not
                     * equip onto the player as well.
                     */
                    if (mob.level().isClientSide()) {
                        cir.setReturnValue(
                                InteractionResult.SUCCESS
                        );
                        return;
                    }

                    if (!moblets$isOwnedBy(player)) {
                        cir.setReturnValue(
                                InteractionResult.CONSUME
                        );
                        return;
                    }

                    ItemStack previous =
                            mob.getItemBySlot(
                                    EquipmentSlot.HEAD
                            );

                    ItemStack equipped =
                            held.copyWithCount(1);

                    mob.setItemSlot(
                            EquipmentSlot.HEAD,
                            equipped
                    );

                    mob.setGuaranteedDrop(
                            EquipmentSlot.HEAD
                    );

                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }

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

                    player.sendOverlayMessage(
                            Component.literal(
                                    "Moblet helmet equipped."
                            )
                    );

                    cir.setReturnValue(
                            InteractionResult.SUCCESS_SERVER
                    );
                    return;
                }
            }
        }

        /*
         * Shift + empty hand removes the Baby Creeper's helmet.
         */
        if (hand == InteractionHand.MAIN_HAND
                && mob instanceof Creeper creeper
                && BabyCreepers.isBaby(creeper)
                && player.isShiftKeyDown()
                && player.getItemInHand(hand).isEmpty()) {

            if (mob.level().isClientSide()) {
                cir.setReturnValue(
                        InteractionResult.SUCCESS.withoutItem()
                );
                return;
            }

            if (!moblets$isOwnedBy(player)) {
                cir.setReturnValue(
                        InteractionResult.CONSUME
                );
                return;
            }

            ItemStack equipped =
                    mob.getItemBySlot(
                            EquipmentSlot.HEAD
                    );

            if (!equipped.isEmpty()) {
                ItemStack returned =
                        equipped.copy();

                mob.setItemSlot(
                        EquipmentSlot.HEAD,
                        ItemStack.EMPTY
                );

                if (!player.addItem(returned)) {
                    player.drop(
                            returned,
                            false
                    );
                }

                player.sendOverlayMessage(
                        Component.literal(
                                "Moblet helmet removed."
                        )
                );
            } else {
                player.sendOverlayMessage(
                        Component.literal(
                                "Moblet has no helmet."
                        )
                );
            }

            cir.setReturnValue(
                    InteractionResult.SUCCESS_SERVER
            );
            return;
        }

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

                    player.sendOverlayMessage(
                            Component.literal(
                                    "Moblet armor equipped."
                            )
                    );

                    cir.setReturnValue(
                            InteractionResult.SUCCESS_SERVER
                    );
                    return;
                }
            }
        }

        /*
         * Owner-managed Skeleton bow equipment.
         *
         * Right-clicking a tamed baby Skeleton with a bow equips
         * that exact bow, preserving enchantments, durability and
         * other item components. The previous main-hand item is
         * returned to the owner.
         */
        if (hand == InteractionHand.MAIN_HAND
                && mob instanceof AbstractSkeleton skeleton
                && BabySkeletons.isBaby(skeleton)
                && BabySkeletons.isRangedFamily(skeleton)
                && player.getItemInHand(hand).getItem()
                        instanceof BowItem) {

            ItemStack held =
                    player.getItemInHand(hand);

            /*
             * Client:
             *
             * Consume this as the Moblet interaction so the bow's
             * normal player-use action does not also occur.
             */
            if (mob.level().isClientSide()) {
                cir.setReturnValue(
                        InteractionResult.SUCCESS
                );
                return;
            }

            /*
             * Server-authoritative ownership check.
             */
            if (!moblets$isOwnedBy(player)) {
                cir.setReturnValue(
                        InteractionResult.CONSUME
                );
                return;
            }

            ItemStack previous =
                    mob.getItemBySlot(
                            EquipmentSlot.MAINHAND
                    );

            ItemStack equipped =
                    held.copyWithCount(1);

            mob.setItemSlot(
                    EquipmentSlot.MAINHAND,
                    equipped
            );

            /*
             * Make player-supplied weapons reliably recoverable
             * if the Moblet dies.
             */
            mob.setGuaranteedDrop(
                    EquipmentSlot.MAINHAND
            );

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }

            /*
             * Return the previous weapon/item to the owner.
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

            /*
             * Refresh Skeleton combat goals immediately so the
             * newly equipped bow is recognized.
             */
            skeleton.reassessWeaponGoal();

            player.sendOverlayMessage(
                    Component.literal(
                            "Moblet bow equipped."
                    )
            );

            cir.setReturnValue(
                    InteractionResult.SUCCESS_SERVER
            );
            return;
        }

        /*
         * Owner-managed Pillager crossbow equipment.
         *
         * Right-clicking a tamed baby Pillager with a crossbow
         * replaces its current main-hand weapon. The complete
         * ItemStack is copied, preserving enchantments,
         * durability and other components.
         */
        if (hand == InteractionHand.MAIN_HAND
                && mob instanceof Pillager pillager
                && BabyPillagers.isBaby(pillager)
                && player.getItemInHand(hand).getItem()
                        instanceof CrossbowItem) {

            ItemStack held =
                    player.getItemInHand(hand);

            /*
             * Prevent the player's own crossbow use action from
             * starting on the client.
             */
            if (mob.level().isClientSide()) {
                cir.setReturnValue(
                        InteractionResult.SUCCESS
                );
                return;
            }

            /*
             * Only the owner may replace the weapon.
             */
            if (!moblets$isOwnedBy(player)) {
                cir.setReturnValue(
                        InteractionResult.CONSUME
                );
                return;
            }

            ItemStack previous =
                    mob.getItemBySlot(
                            EquipmentSlot.MAINHAND
                    );

            ItemStack equipped =
                    held.copyWithCount(1);

            mob.setItemSlot(
                    EquipmentSlot.MAINHAND,
                    equipped
            );

            /*
             * A player-supplied crossbow should be recoverable
             * if the Moblet later dies.
             */
            mob.setGuaranteedDrop(
                    EquipmentSlot.MAINHAND
            );

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }

            /*
             * Give the previous weapon back to the owner.
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

            player.sendOverlayMessage(
                    Component.literal(
                            "Moblet crossbow equipped."
                    )
            );

            cir.setReturnValue(
                    InteractionResult.SUCCESS_SERVER
            );
            return;
        }

        /*
         * Owner-managed Wither Skeleton sword equipment.
         *
         * The Wither Moblet is the melee specialist: swords are
         * handled as equipment, while bows and other items retain
         * the ordinary Follow / Stay interaction.
         */
        if (hand == InteractionHand.MAIN_HAND
                && mob instanceof WitherSkeleton witherSkeleton
                && BabySkeletons.isBaby(witherSkeleton)
                && player.getItemInHand(hand)
                        .is(ItemTags.SWORDS)) {

            ItemStack held =
                    player.getItemInHand(hand);

            /*
             * Consume the client interaction so the sword does
             * not perform its normal player-side item action.
             * Ownership remains server-authoritative.
             */
            if (mob.level().isClientSide()) {
                cir.setReturnValue(
                        InteractionResult.SUCCESS
                );
                return;
            }

            if (!moblets$isOwnedBy(player)) {
                cir.setReturnValue(
                        InteractionResult.CONSUME
                );
                return;
            }

            ItemStack previous =
                    mob.getItemBySlot(
                            EquipmentSlot.MAINHAND
                    );

            ItemStack equipped =
                    held.copyWithCount(1);

            mob.setItemSlot(
                    EquipmentSlot.MAINHAND,
                    equipped
            );

            mob.setGuaranteedDrop(
                    EquipmentSlot.MAINHAND
            );

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }

            /*
             * Return the previous weapon/item to the owner.
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

            /*
             * Refresh AbstractSkeleton's weapon goal immediately
             * so the newly equipped sword is used for melee.
             */
            witherSkeleton.reassessWeaponGoal();

            player.sendOverlayMessage(
                    Component.literal(
                            "Moblet sword equipped."
                    )
            );

            cir.setReturnValue(
                    InteractionResult.SUCCESS_SERVER
            );
            return;
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
                && mob instanceof AbstractSkeleton skeleton
                && BabySkeletons.isBaby(skeleton)
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
         * General owner command interaction.
         *
         * Any MAIN_HAND interaction that was not already handled
         * above as:
         *
         *   - Bone healing / health inspection
         *   - Armor equipping
         *   - Sneak + empty-hand armor removal
         *
         * becomes the universal Follow / Stay command.
         *
         * Client ownership is not currently synchronized, so the
         * client consumes the interaction for supported Moblets and
         * lets the server decide whether the player is actually
         * the owner.
         */
        if (hand == InteractionHand.MAIN_HAND
                && (
                        mob instanceof AbstractSkeleton skeleton
                                && BabySkeletons.isBaby(skeleton)
                        ||
                        mob instanceof Creeper creeper
                                && BabyCreepers.isBaby(creeper)
                        ||
                        mob instanceof Pillager pillager
                                && BabyPillagers.isBaby(pillager)
                )) {

            /*
             * Client prediction:
             *
             * Prevent the held item's normal use behavior from
             * also firing when the player clicks the Moblet.
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
             * Only the owner may issue Follow / Stay commands.
             * Non-owners fall through so normal taming logic can
             * still process wild Moblets.
             */
            if (moblets$isOwnedBy(player)) {
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

                cir.setReturnValue(
                        InteractionResult.SUCCESS_SERVER
                );
                return;
            }
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

    /*
     * Tamed Moblets no longer burn in direct sunlight.
     *
     * This hooks only Minecraft's daylight burn check. It does
     * not make the Moblet fire-immune, so lava, fire blocks,
     * flaming projectiles, etc. still behave normally.
     */
    @Inject(
            method = "isSunBurnTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void moblets$preventTamedSunBurn(
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this.moblets$isTamed()) {
            cir.setReturnValue(false);
        }
    }

    /*
     * Very slow passive recovery for tamed Moblets.
     *
     * One HP every 30 seconds while out of combat.
     * This is intentionally weak enough that active combat can
     * still be dangerous; owner-provided healing remains useful.
     */
    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void moblets$passiveTamedRegeneration(
            CallbackInfo ci
    ) {
        Mob mob =
                (Mob) (Object) this;

        if (mob.level().isClientSide()
                || !this.moblets$isTamed()
                || mob.getTarget() != null
                || mob.getHealth() >= mob.getMaxHealth()) {
            return;
        }

        if (mob.tickCount % 600 == 0) {
            mob.heal(1.0F);
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

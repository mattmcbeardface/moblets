package com.pagesofatlas.babymobs;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;

public final class BabyEndermanItemTheftGoal extends Goal {
    private static final double DETECTION_DISTANCE = 12.0D;
    private static final double DETECTION_DISTANCE_SQR =
            DETECTION_DISTANCE * DETECTION_DISTANCE;

    /*
     * Navigation may consider itself finished slightly before
     * the Moblet is physically centered over the dropped item.
     * Give the little thief enough reach to actually grab what
     * it just walked over to.
     */
    private static final double PICKUP_DISTANCE =
            2.5D;

    private static final double PICKUP_DISTANCE_SQR =
            PICKUP_DISTANCE * PICKUP_DISTANCE;

    private static final double APPROACH_SPEED =
            1.25D;

    private final EnderMan enderman;

    private ItemEntity item;

    public BabyEndermanItemTheftGoal(
            EnderMan enderman
    ) {
        this.enderman = enderman;

        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE,
                Goal.Flag.LOOK
        ));
    }

    @Override
    public boolean canUse() {
        if (!BabyEndermen.isBaby(this.enderman)) {
            return false;
        }

        if (!this.enderman
                .getItemBySlot(EquipmentSlot.MAINHAND)
                .isEmpty()) {
            return false;
        }

        /*
         * Check periodically, just like the fox item-search goal.
         */
        if (this.enderman.getRandom()
                .nextInt(reducedTickDelay(10)) != 0) {
            return false;
        }

        this.item = this.findNearestItem();

        return this.item != null;
    }

    @Override
    public boolean canContinueToUse() {
        return BabyEndermen.isBaby(this.enderman)
                && this.item != null
                && this.item.isAlive()
                && !this.item.hasPickUpDelay()
                && !this.item.getItem().isEmpty()
                && this.enderman
                        .getItemBySlot(
                                EquipmentSlot.MAINHAND
                        )
                        .isEmpty()
                && this.enderman.distanceToSqr(this.item)
                        <= DETECTION_DISTANCE_SQR;
    }

    @Override
    public void start() {
        if (this.item != null) {
            this.enderman.getNavigation().moveTo(
                    this.item,
                    APPROACH_SPEED
            );
        }
    }

    @Override
    public void stop() {
        this.enderman.getNavigation().stop();
        this.item = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.item == null
                || !this.item.isAlive()) {
            return;
        }

        this.enderman.getLookControl().setLookAt(
                this.item,
                30.0F,
                30.0F
        );

        if (this.enderman.distanceToSqr(this.item)
                <= PICKUP_DISTANCE_SQR) {

            this.stealItem();
            return;
        }

        this.enderman.getNavigation().moveTo(
                this.item,
                APPROACH_SPEED
        );
    }

    private void stealItem() {
        if (this.item == null) {
            return;
        }

        ItemStack groundStack =
                this.item.getItem();

        if (groundStack.isEmpty()) {
            return;
        }

        /*
         * Like a fox, steal one item rather than deleting
         * an entire dropped stack.
         */
        ItemStack stolen =
                groundStack.split(1);

        this.enderman.setItemSlot(
                EquipmentSlot.MAINHAND,
                stolen
        );

        /*
         * Make sure killing the thief gets the stolen item back.
         */
        this.enderman.setDropChance(
                EquipmentSlot.MAINHAND,
                2.0F
        );

        if (groundStack.isEmpty()) {
            this.item.discard();
        } else {
            this.item.setItem(groundStack);
        }

        this.enderman.getNavigation().stop();
    }

    private ItemEntity findNearestItem() {
        List<ItemEntity> items =
                this.enderman.level().getEntitiesOfClass(
                        ItemEntity.class,
                        this.enderman
                                .getBoundingBox()
                                .inflate(
                                        DETECTION_DISTANCE,
                                        6.0D,
                                        DETECTION_DISTANCE
                                ),
                        item ->
                                item.isAlive()
                                && !item.hasPickUpDelay()
                                && !item.getItem().isEmpty()
                );

        ItemEntity nearest = null;
        double nearestDistance =
                DETECTION_DISTANCE_SQR;

        for (ItemEntity candidate : items) {
            double distance =
                    this.enderman.distanceToSqr(
                            candidate
                    );

            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }

        return nearest;
    }
}

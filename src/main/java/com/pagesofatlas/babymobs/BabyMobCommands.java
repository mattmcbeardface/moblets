package com.pagesofatlas.babymobs;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.skeleton.Skeleton;

public final class BabyMobCommands {

    private BabyMobCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            dispatcher.register(
                    Commands.literal("babymobs")
                            .then(Commands.literal("summon")

                                    .then(Commands.literal("skeleton")
                                            .then(Commands.literal("baby")
                                                    .executes(context ->
                                                            summonSkeleton(context.getSource(), true)))
                                            .then(Commands.literal("adult")
                                                    .executes(context ->
                                                            summonSkeleton(context.getSource(), false)))
                                    )

                                    .then(Commands.literal("creeper")
                                            .then(Commands.literal("baby")
                                                    .executes(context ->
                                                            summonCreeper(context.getSource(), true)))
                                            .then(Commands.literal("adult")
                                                    .executes(context ->
                                                            summonCreeper(context.getSource(), false)))
                                    )

                                    .then(Commands.literal("enderman")
                                            .then(Commands.literal("baby")
                                                    .executes(context ->
                                                            summonEnderman(context.getSource(), true)))
                                            .then(Commands.literal("adult")
                                                    .executes(context ->
                                                            summonEnderman(context.getSource(), false)))
                                    )
                            )
            );
        });
    }

    private static BlockPos getSpawnPos(CommandSourceStack source) {
        return BlockPos.containing(
                source.getPosition().x,
                source.getPosition().y,
                source.getPosition().z
        );
    }

    private static int summonSkeleton(CommandSourceStack source, boolean baby) {
        ServerLevel level = source.getLevel();

        Skeleton skeleton = EntityTypes.SKELETON.spawn(
                level,
                getSpawnPos(source),
                EntitySpawnReason.COMMAND
        );

        if (skeleton == null) {
            return 0;
        }

        if (baby) {
            BabySkeletons.applyBaby(skeleton);
        }

        return 1;
    }

    private static int summonCreeper(CommandSourceStack source, boolean baby) {
        ServerLevel level = source.getLevel();

        Creeper creeper = EntityTypes.CREEPER.spawn(
                level,
                getSpawnPos(source),
                EntitySpawnReason.COMMAND
        );

        if (creeper == null) {
            return 0;
        }

        if (baby) {
            BabyCreepers.applyBaby(creeper);
        }

        return 1;
    }

    private static int summonEnderman(CommandSourceStack source, boolean baby) {
        ServerLevel level = source.getLevel();

        EnderMan enderman = EntityTypes.ENDERMAN.spawn(
                level,
                getSpawnPos(source),
                EntitySpawnReason.COMMAND
        );

        if (enderman == null) {
            return 0;
        }

        if (baby) {
            BabyEndermen.applyBaby(enderman);
        }

        return 1;
    }
}

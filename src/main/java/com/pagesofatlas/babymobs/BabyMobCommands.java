package com.pagesofatlas.babymobs;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
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
                            )
            );
        });
    }

    private static int summonSkeleton(CommandSourceStack source, boolean baby) {
        ServerLevel level = source.getLevel();

        BlockPos pos = BlockPos.containing(
                source.getPosition().x,
                source.getPosition().y,
                source.getPosition().z
        );

        Skeleton skeleton = EntityTypes.SKELETON.spawn(
                level,
                pos,
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
}

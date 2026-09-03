package com.pagesofatlas.babymobs;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;

public final class BabyMobCommands {

    private BabyMobCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
                dispatcher.register(
                        Commands.literal("babymobs")
                                .then(Commands.literal("summon")
                                        .then(Commands.literal("skeleton")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.SKELETON,
                                                                true,
                                                                BabySkeletons::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.SKELETON,
                                                                false,
                                                                BabySkeletons::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("creeper")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.CREEPER,
                                                                true,
                                                                BabyCreepers::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.CREEPER,
                                                                false,
                                                                BabyCreepers::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("enderman")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.ENDERMAN,
                                                                true,
                                                                BabyEndermen::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.ENDERMAN,
                                                                false,
                                                                BabyEndermen::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("witch")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.WITCH,
                                                                true,
                                                                BabyWitches::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.WITCH,
                                                                false,
                                                                BabyWitches::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("stray")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.STRAY,
                                                                true,
                                                                BabySkeletons::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.STRAY,
                                                                false,
                                                                BabySkeletons::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("bogged")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.BOGGED,
                                                                true,
                                                                BabySkeletons::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.BOGGED,
                                                                false,
                                                                BabySkeletons::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("parched")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.PARCHED,
                                                                true,
                                                                BabySkeletons::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.PARCHED,
                                                                false,
                                                                BabySkeletons::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("wither_skeleton")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.WITHER_SKELETON,
                                                                true,
                                                                BabySkeletons::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityTypes.WITHER_SKELETON,
                                                                false,
                                                                BabySkeletons::applyBaby
                                                        )))
                                        )
                                )
                )
        );
    }

    private static <T extends Mob> int summon(
            CommandSourceStack source,
            EntityType<T> type,
            boolean baby,
            Consumer<T> babyApplier
    ) {
        ServerLevel level = source.getLevel();

        T mob = type.spawn(
                level,
                getSpawnPos(source),
                EntitySpawnReason.COMMAND
        );

        if (mob == null) {
            return 0;
        }

        if (baby) {
            babyApplier.accept(mob);
        }

        return 1;
    }

    private static BlockPos getSpawnPos(CommandSourceStack source) {
        return BlockPos.containing(
                source.getPosition().x,
                source.getPosition().y,
                source.getPosition().z
        );
    }
}

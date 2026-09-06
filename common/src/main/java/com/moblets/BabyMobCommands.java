package com.moblets;

import java.util.function.Consumer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class BabyMobCommands {

    private BabyMobCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                        Commands.literal("moblets")
                                .then(Commands.literal("summon")
                                        .then(Commands.literal("skeleton")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.SKELETON,
                                                                true,
                                                                BabySkeletons::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.SKELETON,
                                                                false,
                                                                BabySkeletons::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("creeper")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.CREEPER,
                                                                true,
                                                                BabyCreepers::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.CREEPER,
                                                                false,
                                                                BabyCreepers::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("enderman")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.ENDERMAN,
                                                                true,
                                                                BabyEndermen::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.ENDERMAN,
                                                                false,
                                                                BabyEndermen::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("witch")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.WITCH,
                                                                true,
                                                                BabyWitches::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.WITCH,
                                                                false,
                                                                BabyWitches::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("stray")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.STRAY,
                                                                true,
                                                                BabySkeletons::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.STRAY,
                                                                false,
                                                                BabySkeletons::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("bogged")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.BOGGED,
                                                                true,
                                                                BabySkeletons::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.BOGGED,
                                                                false,
                                                                BabySkeletons::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("parched")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.PARCHED,
                                                                true,
                                                                BabySkeletons::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.PARCHED,
                                                                false,
                                                                BabySkeletons::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("wither_skeleton")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.WITHER_SKELETON,
                                                                true,
                                                                BabySkeletons::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.WITHER_SKELETON,
                                                                false,
                                                                BabySkeletons::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("pillager")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.PILLAGER,
                                                                true,
                                                                BabyPillagers::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.PILLAGER,
                                                                false,
                                                                BabyPillagers::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("snow_golem")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.SNOW_GOLEM,
                                                                true,
                                                                BabySnowGolems::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.SNOW_GOLEM,
                                                                false,
                                                                BabySnowGolems::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("iron_golem")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.IRON_GOLEM,
                                                                true,
                                                                BabyIronGolems::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.IRON_GOLEM,
                                                                false,
                                                                BabyIronGolems::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("wandering_trader")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.WANDERING_TRADER,
                                                                true,
                                                                BabyWanderingTraders::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.WANDERING_TRADER,
                                                                false,
                                                                BabyWanderingTraders::applyBaby
                                                        )))
                                        )
                                        .then(Commands.literal("camel_husk")
                                                .then(Commands.literal("baby")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.CAMEL_HUSK,
                                                                true,
                                                                BabyCamelHusks::applyBaby
                                                        )))
                                                .then(Commands.literal("adult")
                                                        .executes(context -> summon(
                                                                context.getSource(),
                                                                EntityType.CAMEL_HUSK,
                                                                false,
                                                                BabyCamelHusks::applyBaby
                                                        )))
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

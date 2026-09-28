package com.cubr.fargo_bosses.fargo_bosses.command;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import com.cubr.fargo_bosses.fargo_bosses.entity.AbominationEntity;
import com.cubr.fargo_bosses.fargo_bosses.entity.MutantEntity;
import com.cubr.fargo_bosses.fargo_bosses.world.BossDifficultyData;
import com.cubr.fargo_bosses.fargo_bosses.world.BossDifficultyData.Boss;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class FargoBossesCommand {
    private static final DynamicCommandExceptionType UNKNOWN_BOSS = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.fargo_bosses.difficulty.unknown_boss", name));

    private FargoBossesCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(FargoBosses.MODID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("difficulty")
                        .then(Commands.argument("boss_name", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        new String[]{"abomination", "mutant"}, builder))
                                .then(Commands.argument("difficulty", IntegerArgumentType.integer(-1, 2))
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                new String[]{"-1", "0", "1", "2"}, builder))
                                        .executes(context -> setDifficulty(context.getSource(),
                                                StringArgumentType.getString(context, "boss_name"),
                                                IntegerArgumentType.getInteger(context, "difficulty")))))));
    }

    private static int setDifficulty(CommandSourceStack source, String name, int difficulty) throws CommandSyntaxException {
        Boss boss = switch (name) {
            case "abomination" -> Boss.ABOMINATION;
            case "mutant" -> Boss.MUTANT;
            default -> throw UNKNOWN_BOSS.create(name);
        };
        BossDifficultyData.get(source.getServer()).setDifficulty(boss, difficulty);

        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (boss == Boss.ABOMINATION && entity instanceof AbominationEntity abomination) {
                    abomination.refreshGlobalBossDifficulty();
                }
                else if (boss == Boss.MUTANT && entity instanceof MutantEntity mutant) {
                    mutant.refreshGlobalBossDifficulty();
                }
            }
        }

        Component bossName = Component.translatable("entity.fargo_bosses." + boss.id);
        if (difficulty == -1) {
            source.sendSuccess(() -> Component.translatable("commands.fargo_bosses.difficulty.cleared", bossName), true);
        }
        else {
            source.sendSuccess(() -> Component.translatable("commands.fargo_bosses.difficulty.set", bossName, difficulty), true);
        }
        return Command.SINGLE_SUCCESS;
    }
}

package me.logwet.delorean.patch;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.logwet.delorean.DeLorean;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;

public class CommandManager {
    private static int getIntArgument(CommandContext<CommandSourceStack> context, String name) {
        return IntegerArgumentType.getInteger(context, name);
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        context.getSource()
                .sendSuccess(
                        new TextComponent(
                                "Slots: " + DeLorean.SLOTMANAGER.slotsData.getSlots()),
                        true);

        return 1;
    }

    private static int saveID(CommandContext<CommandSourceStack> context) {
        int index = getIntArgument(context, "index");
        index--;

        DeLorean.SLOTMANAGER.save(index);
        context.getSource().sendSuccess(new TextComponent("Saved slot " + (index + 1)), true);

        return 1;
    }

    private static int loadID(CommandContext<CommandSourceStack> context) {
        int index = getIntArgument(context, "index");
        index--;

        DeLorean.SLOTMANAGER.load(index);
        context.getSource().sendSuccess(new TextComponent("Loaded slot " + (index + 1)), true);

        return 1;
    }

    private static int saveLatest(CommandContext<CommandSourceStack> context) {
        DeLorean.SLOTMANAGER.save();
        context.getSource().sendSuccess(new TextComponent("Saved latest slot"), true);

        return 1;
    }

    private static int loadLatest(CommandContext<CommandSourceStack> context) {
        DeLorean.SLOTMANAGER.load();
        context.getSource().sendSuccess(new TextComponent("Loaded latest slot"), true);

        return 1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("savestate")
                        .executes(CommandManager::list)
                        .then(
                                literal("save")
                                        .executes(CommandManager::saveLatest)
                                        .then(
                                                argument("index", IntegerArgumentType.integer(1, 9))
                                                        .executes(CommandManager::saveID)))
                        .then(
                                literal("load")
                                        .executes(CommandManager::loadLatest)
                                        .then(
                                                argument("index", IntegerArgumentType.integer(1, 9))
                                                        .executes(CommandManager::loadID))));
    }
}

package me.logwet.delorean;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;

public class CommandManager {
    private static int getIntArgument(CommandContext<CommandSourceStack> context, String name) {
        return IntegerArgumentType.getInteger(context, name);
    }

    private static int disabled(CommandContext<CommandSourceStack> context) {
        context.getSource()
                .sendFailure(
                        new TextComponent("This functionality has been disabled by another mod."));
        return -1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        Map<Integer, String> slots;

        synchronized (DeLorean.SLOTMANAGER_LOCK) {
            slots = DeLorean.SLOTMANAGER.getSlotsData().getSlots();
        }

        context.getSource().sendSuccess(new TextComponent("Slots: " + slots), true);

        return 1;
    }

    private static int saveSlot(CommandContext<CommandSourceStack> context) {
        if (DeLorean.CONTROL_ENABLED) {
            int slot = getIntArgument(context, "slot");
            slot--;

            DeLorean.TRIGGER_SAVE.set(true);
            DeLorean.TRIGGER_SAVE_SLOT.set(slot);

            context.getSource().sendSuccess(new TextComponent("Saved slot " + (slot + 1)), true);

            return 1;
        } else {
            return disabled(context);
        }
    }

    private static int loadSlot(CommandContext<CommandSourceStack> context) {
        int slot = getIntArgument(context, "slot");
        slot--;

        if (DeLorean.CONTROL_ENABLED) {
            DeLorean.TRIGGER_LOAD.set(true);
            DeLorean.TRIGGER_LOAD_SLOT.set(slot);

            context.getSource().sendSuccess(new TextComponent("Loaded slot " + (slot + 1)), true);

            return 1;
        } else {
            return disabled(context);
        }
    }

    private static int saveLatest(CommandContext<CommandSourceStack> context) {
        if (DeLorean.CONTROL_ENABLED) {
            DeLorean.TRIGGER_SAVE.set(true);
            context.getSource().sendSuccess(new TextComponent("Saved latest slot"), true);

            return 1;
        } else {
            return disabled(context);
        }
    }

    private static int loadLatest(CommandContext<CommandSourceStack> context) {
        if (DeLorean.CONTROL_ENABLED) {
            DeLorean.TRIGGER_LOAD.set(true);
            context.getSource()
                    .sendSuccess(new TextComponent("Triggered load of latest slot"), true);

            return 1;
        } else {
            return disabled(context);
        }
    }

    private static int deleteSlot(CommandContext<CommandSourceStack> context) {
        int slot = getIntArgument(context, "slot");
        slot--;

        if (DeLorean.CONTROL_ENABLED) {
            DeLorean.TRIGGER_DELETE.set(true);
            DeLorean.TRIGGER_DELETE_SLOT.set(slot);

            context.getSource().sendSuccess(new TextComponent("Deleted slot " + (slot + 1)), true);

            return 1;
        } else {
            return disabled(context);
        }
    }

    private static int deleteAll(CommandContext<CommandSourceStack> context) {
        if (DeLorean.CONTROL_ENABLED) {
            DeLorean.TRIGGER_DELETE.set(true);
            context.getSource()
                    .sendSuccess(new TextComponent("Triggered delete of all slots"), true);

            return 1;
        } else {
            return disabled(context);
        }
    }

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        dispatcher.register(
                literal("delorean")
                        .executes(CommandManager::list)
                        .then(literal("list").executes(CommandManager::list))
                        .then(
                                literal("save")
                                        .executes(CommandManager::saveLatest)
                                        .then(
                                                argument("slot", IntegerArgumentType.integer(1, 9))
                                                        .executes(CommandManager::saveSlot)))
                        .then(
                                literal("load")
                                        .executes(CommandManager::loadLatest)
                                        .then(
                                                argument("slot", IntegerArgumentType.integer(1, 9))
                                                        .executes(CommandManager::loadSlot)))
                        .then(
                                literal("delete")
                                        .executes(CommandManager::deleteAll)
                                        .then(
                                                argument("slot", IntegerArgumentType.integer(1, 9))
                                                        .executes(CommandManager::deleteSlot))));
    }
}

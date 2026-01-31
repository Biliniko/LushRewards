package org.lushplugins.lushrewards.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.libraries.chatcolor.parsers.ParserTypes;
import org.lushplugins.lushlib.utils.DisplayItemStack;

import java.util.List;

public class GuiCommandButton {
    private final DisplayItemStack displayItem;
    private final List<String> commands;

    public GuiCommandButton(DisplayItemStack displayItem, List<String> commands) {
        this.displayItem = displayItem != null ? displayItem : DisplayItemStack.empty();
        this.commands = commands != null ? commands : List.of();
    }

    public DisplayItemStack getDisplayItem() {
        return displayItem;
    }

    public boolean hasCommands() {
        return !commands.isEmpty();
    }

    public void execute(Player player) {
        if (player == null || commands.isEmpty()) {
            return;
        }

        for (String commandRaw : commands) {
            if (commandRaw == null || commandRaw.isBlank()) {
                continue;
            }

            String command = commandRaw.replace("{player}", player.getName());
            CommandTarget target = CommandTarget.from(command);
            command = target.stripPrefix(command);
            command = ChatColorHandler.translate(command, player, ParserTypes.placeholder());

            if (command.isBlank()) {
                continue;
            }

            if (target == CommandTarget.CONSOLE) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            } else {
                Bukkit.dispatchCommand(player, command);
            }
        }
    }

    private enum CommandTarget {
        CONSOLE,
        PLAYER;

        private static final String CONSOLE_PREFIX = "console:";
        private static final String PLAYER_PREFIX = "player:";

        public static CommandTarget from(String command) {
            if (command == null) {
                return PLAYER;
            }

            String trimmed = command.trim();
            if (startsWithIgnoreCase(trimmed, CONSOLE_PREFIX)) {
                return CONSOLE;
            }
            if (startsWithIgnoreCase(trimmed, PLAYER_PREFIX)) {
                return PLAYER;
            }
            return PLAYER;
        }

        public String stripPrefix(String command) {
            if (command == null) {
                return "";
            }

            String trimmed = command.trim();
            if (startsWithIgnoreCase(trimmed, CONSOLE_PREFIX)) {
                return trimmed.substring(CONSOLE_PREFIX.length()).trim();
            }
            if (startsWithIgnoreCase(trimmed, PLAYER_PREFIX)) {
                return trimmed.substring(PLAYER_PREFIX.length()).trim();
            }
            return command;
        }

        private static boolean startsWithIgnoreCase(String value, String prefix) {
            if (value.length() < prefix.length()) {
                return false;
            }
            return value.regionMatches(true, 0, prefix, 0, prefix.length());
        }
    }
}

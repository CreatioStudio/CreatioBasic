package vip.creatio.basic.cmd;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

public enum SenderType {

    /** CommandSender that implements Player */
    PLAYER,

    /** CommandSender that implements ConsoleCommandSender */
    CONSOLE,

    /** CommandSender that implements BlockCommandSender */
    COMMAND_BLOCK,

    /** CommandSender that implements CommandMinecart */
    COMMAND_MINECART,

    /** All the entity except player and cmd minecart */
    ENTITY;

    public static SenderType of(CommandSender sender) {
        if (sender instanceof Player)
            return PLAYER;
        if (sender instanceof ConsoleCommandSender)
            return CONSOLE;
        if (sender instanceof BlockCommandSender)
            return COMMAND_BLOCK;
        if (sender instanceof CommandMinecart)
            return COMMAND_MINECART;

        return ENTITY;
    }

}

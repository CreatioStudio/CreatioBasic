package vip.creatio.basic.tools.cmd;

import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface Command {

    boolean run(CommandSender source, String input)
}

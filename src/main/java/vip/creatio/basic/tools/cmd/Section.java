package vip.creatio.basic.tools.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.CommandListenerWrapper;
import org.bukkit.command.CommandSender;

import java.util.function.Predicate;

public abstract class Section {

    protected final ArgumentBuilder<CommandListenerWrapper, ? extends ArgumentBuilder<CommandListenerWrapper, ?>> builder;
    protected Command<CommandSender> command;
    protected Predicate<CommandSender> requirement;
    protected CommandNode<CommandSender> target;

    protected Section(ArgumentBuilder<CommandListenerWrapper, ? extends ArgumentBuilder<CommandListenerWrapper, ?>> builder) {
        this.builder = builder;
    }

    public Command<CommandSender> getCommand() {
        return command;
    }

    public Section executes(Command<CommandSender> command) {
        builder.executes(c -> command.run(new))
    }

    public Predicate<CommandSender> getRequirement() {
        return requirement;
    }

    public CommandNode<CommandSender> getRedirect() {
        return target;
    }

    public boolean isFork() {
        return builder.isFork();
    }
}

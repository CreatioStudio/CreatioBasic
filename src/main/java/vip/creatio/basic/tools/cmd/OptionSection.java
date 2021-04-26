package vip.creatio.basic.tools.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.CommandListenerWrapper;
import org.bukkit.command.CommandSender;

import java.util.function.Predicate;

public class OptionSection extends LiteralArgumentBuilder<CommandListenerWrapper> {

    protected final LiteralArgumentBuilder<CommandListenerWrapper> builder;

    protected Command<CommandSender> command;

    protected Predicate<CommandSender> requirement =

    protected OptionSection(String option) {
        super(option);
    }

    public static OptionSection of(String option) {
        return new OptionSection(option);
    }

    @Override
    public Command<CommandSender> getCommand() {
        return command;
    }
}

package vip.creatio.basic.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.command.CommandSender;

import java.util.function.Predicate;

public class LiteralArgument extends Argument {

    protected LiteralArgument(String option) {
        super(LiteralArgumentBuilder.literal(option));
    }

    public static LiteralArgument of(String option) {
        return new LiteralArgument(option);
    }

    public String getLiteral() {
        return ((LiteralArgumentBuilder<?>) builder).getLiteral();
    }

    @Override
    public LiteralArgument then(Argument argument) {
        super.then(argument);
        return this;
    }

    @Override
    public LiteralArgument then(CommandNode<?> argument) {
        super.then(argument);
        return this;
    }

    @Override
    public LiteralArgument executes(CommandAction command) {
        super.executes(command);
        return this;
    }

    @Override
    public LiteralArgument requires(Predicate<CommandSender> requirement) {
        super.requires(requirement);
        return this;
    }

    @Override
    public LiteralArgument redirect(CommandNode<?> target) {
        super.redirect(target);
        return this;
    }

    @Override
    public LiteralCommandNode<?> build() {
        return (LiteralCommandNode<?>) super.build();
    }
}

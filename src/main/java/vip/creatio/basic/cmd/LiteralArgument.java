package vip.creatio.basic.cmd;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class LiteralArgument extends Argument {

    protected final String literal;

    protected LiteralArgument(String literal) {
        this.literal = literal;
    }

    public static LiteralArgument of(String option) {
        return new LiteralArgument(option);
    }

    public String getLiteral() {
        return literal;
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
    public LiteralArgument executes(CommandAction.Nil command) {
        return executes((CommandAction) command);
    }

    @Override
    public LiteralArgument requires(@NotNull Predicate<CommandSender> requirement) {
        super.requires(requirement);
        return this;
    }

    @Override
    public LiteralArgument redirect(CommandNode<?> target) {
        super.redirect(target);
        return this;
    }

    @Override
    public LiteralArgument redirect(CommandNode<?> target, SingleRedirectSource redirectTo) {
        super.redirect(target, redirectTo);
        return this;
    }

    @Override
    public LiteralArgument fork(CommandNode<?> target, RedirectSource redirectTo) {
        super.fork(target, redirectTo);
        return this;
    }

    @Override
    public LiteralArgument forward(CommandNode<?> target, RedirectSource redirectTo, boolean fork) {
        super.forward(target, redirectTo, fork);
        return this;
    }

    @Override
    public LiteralArgument fallbacks(@NotNull FallbackAction fallback) {
        super.fallbacks(fallback);
        return this;
    }

    @Override
    public LiteralArgument requiresSenderType(@NotNull Predicate<SenderType> requirement) {
        super.requiresSenderType(requirement);
        return this;
    }

    @Override
    public LiteralArgument requiresSenderType(SenderType... types) {
        super.requiresSenderType(types);
        return this;
    }

    @Override
    public LiteralArgument restricted(boolean restricted) {
        super.restricted(restricted);
        return this;
    }

    @Override
    public LiteralCommandNode<?> internalBuild(Inheritable parent) {
        if (parent != null) {
            inheritable.inheritFrom(parent);
        }
        ExLiteralCommandNode result = new ExLiteralCommandNode(literal, command, inheritable, target, redirectSource,
                forks, restricted);

        addNodes(result);

        return result;
    }

    @Override
    public LiteralCommandNode<?> build() {
        return (LiteralCommandNode<?>) super.build();
    }
}

package vip.creatio.basic.cmd;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class Argument {

    protected final RootCommandNode arguments = new RootCommandNode<>();
    protected CommandAction command;
    protected Predicate<CommandSender> requirement = s -> true;
    protected CommandNode<?> target;
    protected RedirectSource redirectSource;
    protected boolean forks;

    protected FallbackAction[] fallback = new FallbackAction[]{DefaultFallbackAction.DEFAULT};

    public CommandAction getCommand() {
        return command;
    }

    public Argument then(Argument argument) {
        if (target != null) {
            throw new IllegalStateException("Cannot add children to a redirected node");
        } else {
            arguments.addChild(argument.internalBuild(fallback));
            return this;
        }
    }

    public Argument then(CommandNode<?> argument) {
        if (this.target != null) {
            throw new IllegalStateException("Cannot add children to a redirected node");
        } else {
            this.arguments.addChild(argument);
            return this;
        }
    }

    public static LiteralArgument of(String option) {
        return LiteralArgument.of(option);
    }

    public static <T> RequiredArgument<T> arg(String name, ArgumentType<T> type) {
        return RequiredArgument.arg(name, type);
    }

    public Collection<CommandNode<?>> getArguments() {
        return arguments.getChildren();
    }

    public Argument executes(CommandAction command) {
        this.command = command;
        return this;
    }

    public Argument executes(NilCommandAction command) {
        return executes((CommandAction) command);
    }

    public Predicate<CommandSender> getRequirement() {
        return requirement;
    }

    public Argument fallbacks(FallbackAction fallback) {
        this.fallback[0] = fallback;
        return this;
    }

    public FallbackAction getFallback() {
        return fallback[0];
    }

    public Argument requires(@NotNull Predicate<CommandSender> requirement) {
        this.requirement = requirement;
        return this;
    }

    public CommandNode<?> getRedirect() {
        return target;
    }

    public RedirectSource getRedirectTarget() {
        return redirectSource;
    }

    public Argument redirect(CommandNode<?> target) {
        return this.forward(target, null, false);
    }

    public Argument redirect(CommandNode<?> target, SingleRedirectSource redirectTo) {
        return this.forward(target, redirectTo == null ? null : (o) -> Collections.singleton(redirectTo.apply(o)), false);
    }

    public Argument fork(CommandNode<?> target, RedirectSource redirectTo) {
        return forward(target, redirectTo, true);
    }

    public Argument forward(CommandNode<?> target, RedirectSource redirectTo, boolean fork) {
        if (!this.arguments.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot forward a node with children");
        } else {
            this.target = target;
            this.redirectSource = redirectTo;
            this.forks = fork;
            return this;
        }
    }

    protected abstract CommandNode<?> internalBuild(FallbackAction[] fallback);
    public CommandNode<?> build() {
        return internalBuild(new FallbackAction[]{DefaultFallbackAction.DEFAULT});
    }

    protected final void addNodes(CommandNode<?> n) {
        for (CommandNode node : getArguments()) {
            n.addChild(node);
        }
    }

}

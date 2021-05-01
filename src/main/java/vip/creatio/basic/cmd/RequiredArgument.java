package vip.creatio.basic.cmd;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class RequiredArgument<T> extends Argument {

    protected final String name;
    protected final ArgumentType<T> argumentType;
    protected SuggestionProvider suggestions;

    protected RequiredArgument(String name, ArgumentType<T> type) {
        this.argumentType = type;
        this.name = name;
    }

    public static <T> RequiredArgument<T> arg(String name, ArgumentType<T> type) {
        return new RequiredArgument<>(name, type);
    }

    public RequiredArgument<T> suggests(SuggestionProvider suggestions) {
        this.suggestions = suggestions;
        return this;
    }

    public SuggestionProvider getCustomSuggestions() {
        return suggestions;
    }

    public ArgumentType<T> getType() {
        return argumentType;
    }

    public String getName() {
        return name;
    }

    @Override
    public RequiredArgument<T> then(Argument argument) {
        super.then(argument);
        return this;
    }

    @Override
    public RequiredArgument<T> then(CommandNode<?> argument) {
        super.then(argument);
        return this;
    }

    @Override
    public RequiredArgument<T> executes(CommandAction command) {
        super.executes(command);
        return this;
    }

    @Override
    public RequiredArgument<T> executes(NilCommandAction command) {
        return executes((CommandAction) command);
    }

    @Override
    public RequiredArgument<T> requires(@NotNull Predicate<CommandSender> requirement) {
        super.requires(requirement);
        return this;
    }

    @Override
    public RequiredArgument<T> redirect(CommandNode<?> target) {
        super.redirect(target);
        return this;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected ArgumentCommandNode<?, T> internalBuild(FallbackAction[] fallback) {
        if (this.fallback[0] != DefaultFallbackAction.DEFAULT) fallback = this.fallback;
        ExArgumentCommandNode<T> result = new ExArgumentCommandNode<>(name, argumentType, command, requirement, target,
                redirectSource, forks, suggestions, fallback);

        addNodes(result);

        return result;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public ArgumentCommandNode<?, T> build() {
        return (ArgumentCommandNode<?, T>) super.build();
    }
}

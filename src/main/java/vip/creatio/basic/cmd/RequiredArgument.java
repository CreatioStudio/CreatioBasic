package vip.creatio.basic.cmd;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
    public RequiredArgument<T> executes(CommandAction.Nil command) {
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
    public RequiredArgument<T> requiresSenderType(@NotNull Predicate<SenderType> requirement) {
        super.requiresSenderType(requirement);
        return this;
    }

    @Override
    public RequiredArgument<T> requiresSenderType(SenderType... types) {
        super.requiresSenderType(types);
        return this;
    }

    @Override
    public RequiredArgument<T> restricted(boolean restricted) {
        super.restricted(restricted);
        return this;
    }

    @Override
    public RequiredArgument<T> fallbacks(@NotNull FallbackAction fallback) {
        super.fallbacks(fallback);
        return this;
    }

    @Override
    public RequiredArgument<T> fallbacksFailure(@NotNull SyntaxConsumer<Context> action) {
        super.fallbacksFailure(action);
        return this;
    }

    @Override
    public RequiredArgument<T> fallbacksInvalidInput(@NotNull BiConsumer<Context, CommandSyntaxException> action) {
        super.fallbacksInvalidInput(action);
        return this;
    }

    @Override
    public RequiredArgument<T> fallbacksException(@NotNull SyntaxBiConsumer<Context, Throwable> action) {
        super.fallbacksException(action);
        return this;
    }

    @Override
    public RequiredArgument<T> fallbacksNoPermission(@NotNull Consumer<CommandSender> action) {
        super.fallbacksNoPermission(action);
        return this;
    }

    @Override
    public RequiredArgument<T> fallbacksInvalidSender(@NotNull Consumer<CommandSender> action) {
        super.fallbacksInvalidSender(action);
        return this;
    }

    @Override
    protected ArgumentCommandNode<?, T> internalBuild(InheritedData parent) {
        if (parent != null) {
            inherit.inheritFrom(parent);
        }
        ExArgumentCommandNode<T> result = new ExArgumentCommandNode<>(name, argumentType, command, inherit, target,
                redirectSource, forks, suggestions, restricted);

        addNodes(result);
        return result;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public ArgumentCommandNode<?, T> build() {
        return (ArgumentCommandNode<?, T>) super.build();
    }
}

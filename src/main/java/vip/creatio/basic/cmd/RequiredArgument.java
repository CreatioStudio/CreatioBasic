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
    protected ArgumentCommandNode<?, T> internalBuild(Inheritable parent) {
        if (parent != null) {
            inheritable.inheritFrom(parent);
        }
        ExArgumentCommandNode<T> result = new ExArgumentCommandNode<>(name, argumentType, command, inheritable, target,
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

package vip.creatio.basic.cmd;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.CommandListenerWrapper;
import org.bukkit.command.CommandSender;

import java.util.function.Predicate;

public class RequiredArgument<T> extends Argument {

    protected SuggestionProvider suggestionProvider;
    protected ArgumentType<T> argumentType;

    protected RequiredArgument(String name, ArgumentType<T> type) {
        super(RequiredArgumentBuilder.argument(name, ArgumentTypes.unwrap(type)));
        this.argumentType = type;
    }

    public static <T> RequiredArgument<T> arg(String name, ArgumentType<T> type) {
        return new RequiredArgument<>(name, type);
    }

    @SuppressWarnings("unchecked")
    public RequiredArgument<T> suggests(SuggestionProvider suggestions) {
        suggestionProvider = suggestions;
        ((RequiredArgumentBuilder<CommandListenerWrapper, ?>) builder).suggests((c, s) ->
                suggestions.getSuggestions(new Content(c, defaultErrMsg), s));
        return this;
    }

    public SuggestionProvider getSuggestionProvider() {
        return suggestionProvider;
    }

    public ArgumentType<T> getType() {
        return argumentType;
    }

    public String getName() {
        return ((RequiredArgumentBuilder<?, ?>) builder).getName();
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
    public RequiredArgument<T> requires(Predicate<CommandSender> requirement) {
        super.requires(requirement);
        return this;
    }

    @Override
    public RequiredArgument<T> redirect(CommandNode<?> target) {
        super.redirect(target);
        return this;
    }

    @Override
    public ArgumentCommandNode<?, ?> build() {
        return (ArgumentCommandNode<?, ?>) super.build();
    }
}

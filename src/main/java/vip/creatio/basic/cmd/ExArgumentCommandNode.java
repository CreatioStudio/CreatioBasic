package vip.creatio.basic.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.CommandListenerWrapper;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.util.NMS;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class ExArgumentCommandNode<T> extends ArgumentCommandNode<CommandListenerWrapper, T> implements ExCommandNode {

    private static final Var<Command<CommandListenerWrapper>> COMMAND =
            Reflection.field(CommandNode.class, "command");

    private final CommandAction command;
    private final RedirectSource redirectSource;
    private final Argument.InheritedData inherit;
    private final ArgumentType<T> type;
    private final boolean restricted;
    private final boolean customArgumentType;

    @SuppressWarnings("unchecked")
    public ExArgumentCommandNode(@NotNull String name,
                                 @NotNull ArgumentType<T> type,
                                 @Nullable CommandAction command,
                                 @NotNull Argument.InheritedData inherit,
                                 @Nullable CommandNode<?> redirect,
                                 @Nullable RedirectSource modifier,
                                 boolean forks,
                                 @Nullable SuggestionProvider suggestions,
                                 boolean restricted) {
        super(name,
                (ArgumentType<T>) ArgumentTypes.unwrap(type),
                null,
                restricted ? w -> inherit.getRequirement().test(NMS.toBukkit(w)) : w -> true,
                (CommandNode<CommandListenerWrapper>) redirect,
                modifier == null ? null : c -> modifier.apply(new Context(c)).stream().map(NMS::toNms).collect(Collectors.toList()),
                forks,
                suggestions == null
                        ? (type instanceof ExternArgumentType ? (com.mojang.brigadier.suggestion.SuggestionProvider<CommandListenerWrapper>) type : null)
                        : (c, b) -> suggestions.getSuggestions(new Context(c), b));
        if (command != null) COMMAND.set(this, ExCommandNode.super::executeAction);
        this.command = command;
        this.inherit = inherit;
        this.redirectSource = modifier;
        this.type = type;
        this.customArgumentType = type instanceof ExternArgumentType && !(type instanceof ArgumentTypes.WrappedArgumentType);
        this.restricted = restricted;
    }

    @Override
    public CompletableFuture<Suggestions>
    listSuggestions(CommandContext<CommandListenerWrapper> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        if (accessiblyTestSilent(NMS.toBukkit(context.getSource()))) {
            return super.listSuggestions(context, builder);
        }
        return builder.buildFuture();
    }

    @Override
    public void parse(StringReader reader, CommandContextBuilder<CommandListenerWrapper> contextBuilder) throws CommandSyntaxException {
        if (customArgumentType) {
            try {
                int start = reader.getCursor();
                T result = this.type.parse(reader);
                ParsedArgument<CommandListenerWrapper, T> parsed = new ParsedArgument<>(start, reader.getCursor(), result);
                contextBuilder.withArgument(getName(), parsed);
                contextBuilder.withNode(this, parsed.getRange());

                // To make sure all exceptions are CommandSyntaxException
            } catch (CommandSyntaxException e) {
                throw e;
            } catch (Throwable t) {
                String errMsg = "Exception while parsing Brigadier command!";
                System.err.println(errMsg);
                t.printStackTrace();
                throw new CommandSyntaxException(new CommandExceptionType(){}, Component.of(errMsg));
            }
        } else {
            super.parse(reader, contextBuilder);
        }
    }

    @Override
    public FallbackAction getFallback() {
        return inherit.fallback;
    }

    @Override
    public CommandAction getCommandAction() {
        return command;
    }

    public RedirectSource getRedirectSource() {
        return redirectSource;
    }

    public Predicate<CommandSender> getSenderPredicate() {
        return inherit.getRequirement();
    }

    public Predicate<SenderType> getRequiredSenderType() {
        return inherit.getSenderType();
    }

    @Override
    public CommandNode<?> getNode() {
        return this;
    }

    @Override
    public boolean isRestricted() {
        return restricted;
    }

    @Override
    public Argument.InheritedData getInheritable() {
        return inherit;
    }
}

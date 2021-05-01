package vip.creatio.basic.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
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
import vip.creatio.basic.util.NMS;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class ExArgumentCommandNode<T> extends ArgumentCommandNode<CommandListenerWrapper, T> implements ExCommandNode {

    private static final Var<Command<CommandListenerWrapper>> COMMAND =
            Reflection.field(CommandNode.class, "command");

    private final CommandAction command;
    private final FallbackAction[] fallback;

    @SuppressWarnings("unchecked")
    public ExArgumentCommandNode(@NotNull String name,
                                 @NotNull ArgumentType<T> type,
                                 @Nullable CommandAction command,
                                 @NotNull Predicate<CommandSender> requirement,
                                 @Nullable CommandNode<?> redirect,
                                 @Nullable RedirectSource modifier,
                                 boolean forks,
                                 @Nullable SuggestionProvider suggestions,
                                 @NotNull FallbackAction[] fallback) {
        super(name, (ArgumentType<T>) ArgumentTypes.unwrap(type), null, w -> requirement.test(NMS.toBukkit(w)),
                (CommandNode<CommandListenerWrapper>) redirect,
                modifier == null ? null : c -> modifier.apply(new Context(c)).stream().map(NMS::toNms).collect(Collectors.toList()),
                forks, suggestions == null ? null : (c, b) -> suggestions.getSuggestions(new Context(c), b));
        if (command != null) COMMAND.set(this, this::executeAction);
        this.command = command;
        this.fallback = fallback;
    }

    private int executeAction(CommandContext<CommandListenerWrapper> c) throws CommandSyntaxException {
        Context context = new Context(c);
        try {
            if (command != null && command.run(context)) {
                return 1;
            } else {
                fallback[0].failed(context);
            }
        } catch (CommandSyntaxException e) {
            fallback[0].failed(context);
        } catch (Throwable t) {
            fallback[0].exception(context, t);
        }

        return 0;
    }

    @Override
    public CompletableFuture<Suggestions>
    listSuggestions(CommandContext<CommandListenerWrapper> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        if (canUse(context.getSource())) {
            return super.listSuggestions(context, builder);
        }
        return builder.buildFuture();
    }

    // CraftBukkit affiliates
    @Override
    public synchronized boolean canUse(CommandListenerWrapper source) {
        //TODO: weird logic
        if (!super.canUse(source)) {
            fallback[0].noPermission(NMS.toBukkit(source));
            return false;
        }
        return true;
    }

    @Override
    public FallbackAction getFallback() {
        return fallback[0];
    }
}

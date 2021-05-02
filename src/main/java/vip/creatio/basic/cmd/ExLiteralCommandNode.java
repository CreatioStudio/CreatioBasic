package vip.creatio.basic.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
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

final class ExLiteralCommandNode extends LiteralCommandNode<CommandListenerWrapper> implements ExCommandNode {

    private static final Var<Command<CommandListenerWrapper>> COMMAND =
            Reflection.field(CommandNode.class, "command");

    private final CommandAction command;
    private final RedirectSource redirectSource;
    private final Argument.Inheritable inheritable;
    private final boolean restricted;

    @SuppressWarnings("unchecked")
    ExLiteralCommandNode(@NotNull String literal,
                         @Nullable CommandAction command,
                         @NotNull Argument.Inheritable inheritable,
                         @Nullable CommandNode<?> redirect,
                         @Nullable RedirectSource modifier,
                         boolean forks,
                         boolean restricted) {
        super(literal, null, restricted ? w -> inheritable.required[0].test(NMS.toBukkit(w)) : w -> true,
                (CommandNode<CommandListenerWrapper>) redirect,
                modifier == null ? null : c -> modifier.apply(new Context(c)).stream().map(NMS::toNms).collect(Collectors.toList()),
                forks);
        if (command != null) COMMAND.set(this, ExCommandNode.super::executeAction);
        this.command = command;
        this.inheritable = inheritable;
        this.redirectSource = modifier;
        this.restricted = restricted;
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext<CommandListenerWrapper> context, SuggestionsBuilder builder) {
        if (accessiblyTestSilent(NMS.toBukkit(context.getSource()))) {
            return super.listSuggestions(context, builder);
        }
        return builder.buildFuture();
    }

    @Override
    public FallbackAction getFallback() {
        return inheritable.fallback[0];
    }

    public CommandAction getCommandAction() {
        return command;
    }

    public RedirectSource getRedirectSource() {
        return redirectSource;
    }

    public Predicate<CommandSender> getSenderPredicate() {
        return inheritable.required[0];
    }

    public Predicate<SenderType> getRequiredSenderType() {
        return inheritable.reqType[0];
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
    public Argument.Inheritable getInheritable() {
        return inheritable;
    }
}

package vip.creatio.basic.cmd;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class Argument {

    protected final RootCommandNode arguments = new RootCommandNode<>();
    protected CommandAction command;
    protected Inheritable inheritable = getInheritable();
    protected CommandNode<?> target;
    protected RedirectSource redirectSource;
    protected boolean forks;
    protected boolean restricted;

    public CommandAction getCommand() {
        return command;
    }

    public Argument then(Argument argument) {
        if (target != null) {
            throw new IllegalStateException("Cannot add children to a redirected node");
        } else {
            arguments.addChild(argument.internalBuild(inheritable));
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

    /** The command to execute in this node */
    public Argument executes(CommandAction command) {
        this.command = command;
        return this;
    }

    /** The command to execute in this node */
    public Argument executes(CommandAction.Nil command) {
        return executes((CommandAction) command);
    }

    /** Condition for command sender execute this node */
    public Argument requires(@NotNull Predicate<CommandSender> requirement) {
        inheritable.setRequired(requirement);
        return this;
    }

    public Predicate<CommandSender> getRequirement() {
        return inheritable.required[0];
    }

    /** A fallback processor, to handle something like exception and failure */
    public Argument fallbacks(@NotNull FallbackAction fallback) {
        inheritable.setFallback(fallback);
        return this;
    }

    public FallbackAction getFallback() {
        return inheritable.fallback[0];
    }

    public Predicate<SenderType> getRequiredSenderType() {
        return inheritable.reqType[0];
    }

    /** Limit command sender's type, like player or console */
    public Argument requiresSenderType(@NotNull Predicate<SenderType> requirement) {
        inheritable.setSenderType(requirement);
        return this;
    }

    /** Limit command sender's type, like player or console */
    public Argument requiresSenderType(SenderType... types) {
        inheritable.setSenderType(t -> {
            for (SenderType type : types) {
                if (type == t) return true;
            }
            return false;});
        return this;
    }

    public boolean isRestricted() {
        return restricted;
    }

    /** Command sender will not be able to see this node if requirement test is not pass */
    public Argument restricted(boolean restricted) {
        this.restricted = restricted;
        AsyncPlayerChatEvent
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

    protected abstract CommandNode<?> internalBuild(Inheritable inheritable);

    public CommandNode<?> build() {
        return internalBuild(null);
    }

    protected final void addNodes(CommandNode<?> n) {
        for (CommandNode node : getArguments()) {
            n.addChild(node);
        }
    }

    protected static Inheritable getInheritable() {
        return new Inheritable(new FallbackAction[]{Inheritable.DEFAULT_ACTION}, new Predicate[]{Inheritable.DEFAULT_SENDER_TYPE}, new Predicate[]{Inheritable.DEFAULT_REQUIREMENT});
    }

    static class Inheritable {

        InheritableFallbackAction fallback;
        Predicate<SenderType>[] reqType;
        Predicate<CommandSender>[] required;

        Inheritable(FallbackAction[] fallback,
                    Predicate<SenderType>[] reqType,
                    Predicate<CommandSender>[] required) {
            this.fallback = new InheritableFallbackAction(fallback[0]);
            this.reqType = reqType;
            this.required = required;
        }

        public Inheritable(InheritableFallbackAction fallback,
                           Predicate<SenderType>[] reqType,
                           Predicate<CommandSender>[] required) {
            this.fallback = fallback;
            this.reqType = reqType;
            this.required = required;
        }

        Inheritable copy() {
            return new Inheritable(fallback, reqType, required);
        }

        void setFallback(FallbackAction action) {
            fallback = new InheritableFallbackAction(action);
        }

        void setSenderType(Predicate<SenderType> predicate) {
            reqType[0] = predicate;
        }

        void setRequired(Predicate<CommandSender> predicate) {
            required[0] = predicate;
        }

        void inheritFallback(Inheritable inh) {
            this.fallback = inh.fallback;
        }

        void inheritSenderType(Inheritable inh) {
            this.reqType = inh.reqType;
        }

        void inheritRequired(Inheritable inh) {
            this.required = inh.required;
        }

        void inheritFrom(Inheritable parent) {
            if (fallback[0] == DEFAULT_ACTION) inheritFallback(parent);

            if (reqType[0] == DEFAULT_SENDER_TYPE) inheritSenderType(parent);
            else {
                if (parent.reqType[0] != DEFAULT_SENDER_TYPE) {
                    reqType[0] = reqType[0].and(parent.reqType[0]);
                }
            }
            if (required[0] == DEFAULT_REQUIREMENT) inheritRequired(parent);
            else {
                if (parent.required[0] != DEFAULT_REQUIREMENT) {
                    required[0] = required[0].and(parent.required[0]);
                }
            }
        }
    }

    static class InheritableFallbackAction implements FallbackAction {
        //TODO: replace with an advanced inherit system
        private static final FallbackAction DEFAULT_ACTION = FallbackAction.DEFAULT;
        private static final SyntaxBiConsumer<Context, CommandSyntaxException> DEFAULT_FALLBACK_INVALID_INPUT = DEFAULT_ACTION::invalidInput;
        private static final SyntaxConsumer<Context> DEFAULT_FALLBACK_FAILURE = DEFAULT_ACTION::failure;
        private static final SyntaxBiConsumer<Context, Throwable> DEFAULT_FALLBACK_EXCEPTION = DEFAULT_ACTION::exception;
        private static final SyntaxConsumer<CommandSender> DEFAULT_FALLBACK_NO_PERM = DEFAULT_ACTION::noPermission;
        private static final SyntaxConsumer<CommandSender> DEFAULT_FALLBACK_INVALID_SENDER = DEFAULT_ACTION::invalidSender;

        FallbackAction[] source;
        SyntaxBiConsumer<Context, CommandSyntaxException>[] invalidInput;
        SyntaxConsumer<Context>[] failure;
        SyntaxBiConsumer<Context, Throwable>[] exception;
        SyntaxConsumer<CommandSender>[] noPermission;
        SyntaxConsumer<CommandSender>[] invalidSender;

        public InheritableFallbackAction() {
            this.invalidInput = new SyntaxBiConsumer[]{DEFAULT_FALLBACK_INVALID_INPUT};
            this.failure = new SyntaxConsumer[]{DEFAULT_FALLBACK_FAILURE};
            this.exception = new SyntaxBiConsumer[]{DEFAULT_FALLBACK_EXCEPTION};
            this.noPermission = new SyntaxConsumer[]{DEFAULT_FALLBACK_NO_PERM};
            this.invalidSender = new SyntaxConsumer[]{DEFAULT_FALLBACK_INVALID_SENDER};
        }

        public InheritableFallbackAction(FallbackAction source) {
            this.invalidInput = new SyntaxBiConsumer[]{(SyntaxBiConsumer<Context, CommandSyntaxException>) source::invalidInput};
            this.failure = new SyntaxConsumer[]{(SyntaxConsumer<Context>) source::failure};
            this.exception = new SyntaxBiConsumer[]{(SyntaxBiConsumer<Context, Throwable>) source::exception};
            this.noPermission = new SyntaxConsumer[]{(SyntaxConsumer<CommandSender>) source::noPermission};
            this.invalidSender = new SyntaxConsumer[]{(SyntaxConsumer<CommandSender>) source::invalidSender};
        }

        @Override
        public void failure(Context context) throws CommandSyntaxException {
            this.failure[0].accept(context);
        }

        @Override
        public void exception(Context context, Throwable exception) throws CommandSyntaxException {
            this.exception[0].accept(context, exception);
        }

        @Override
        public void invalidInput(Context context, CommandSyntaxException e) {
            this.invalidInput[0].acceptNoExcept(context, e);
        }

        @Override
        public void noPermission(CommandSender sender) {
            this.noPermission[0].acceptNoExcept(sender);
        }

        @Override
        public void invalidSender(CommandSender sender) {
            this.invalidSender[0].acceptNoExcept(sender);
        }
    }

    @FunctionalInterface
    interface SyntaxConsumer<T> {
        void accept(T a) throws CommandSyntaxException;

        default void acceptNoExcept(T a) {
            try {
                accept(a);
            } catch (CommandSyntaxException ignored) {}
        }
    }

    @FunctionalInterface
    interface SyntaxBiConsumer<T, U> {
        void accept(T a, U b) throws CommandSyntaxException;

        default void acceptNoExcept(T a, U b) {
            try {
                accept(a, b);
            } catch (CommandSyntaxException ignored) {}
        }
    }
}

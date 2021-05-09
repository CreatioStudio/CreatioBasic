package vip.creatio.basic.cmd;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import vip.creatio.common.util.Inheritable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class Argument {

    public static final Predicate<SenderType> DEFAULT_SENDER_TYPE = s -> true;
    public static final Predicate<CommandSender> DEFAULT_REQUIREMENT = s -> true;

    protected final RootCommandNode arguments = new RootCommandNode<>();
    protected CommandAction command;
    protected InheritedData inherit = new InheritedData();
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
            arguments.addChild(argument.internalBuild(inherit));
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
        inherit.required.set(requirement);
        return this;
    }

    public Predicate<CommandSender> getRequirement() {
        return inherit.required.get();
    }

    /** A fallback processor, to handle something like exception and failure */
    public Argument fallbacks(@NotNull FallbackAction fallback) {
        inherit.setFallback(fallback);
        return this;
    }

    /** Split part of Fallback Action */
    public Argument fallbacksFailure(@NotNull SyntaxConsumer<Context> action) {
        inherit.fallback.failure.set(action);
        return this;
    }

    /** Split part of Fallback Action */
    public Argument fallbacksInvalidInput(@NotNull BiConsumer<Context, CommandSyntaxException> action) {
        inherit.fallback.invalidInput.set(action);
        return this;
    }

    /** Split part of Fallback Action */
    public Argument fallbacksException(@NotNull SyntaxBiConsumer<Context, Throwable> action) {
        inherit.fallback.exception.set(action);
        return this;
    }

    /** Split part of Fallback Action */
    public Argument fallbacksNoPermission(@NotNull Consumer<CommandSender> action) {
        inherit.fallback.noPermission.set(action);
        return this;
    }

    /** Split part of Fallback Action */
    public Argument fallbacksInvalidSender(@NotNull Consumer<CommandSender> action) {
        inherit.fallback.invalidSender.set(action);
        return this;
    }

    public FallbackAction getFallback() {
        return inherit.fallback;
    }

    public Predicate<SenderType> getRequiredSenderType() {
        return inherit.reqType.get();
    }

    /** Limit command sender's type, like player or console */
    public Argument requiresSenderType(@NotNull Predicate<SenderType> requirement) {
        inherit.reqType.set(requirement);
        return this;
    }

    /** Limit command sender's type, like player or console */
    public Argument requiresSenderType(SenderType... types) {
        inherit.reqType.set(t -> {
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

    protected abstract CommandNode<?> internalBuild(InheritedData inherit);

    public CommandNode<?> build() {
        return internalBuild(null);
    }

    protected final void addNodes(CommandNode<?> n) {
        for (CommandNode node : getArguments()) {
            n.addChild(node);
        }
    }

    static class InheritedData {

        InheritableFallbackAction fallback;
        Inheritable<Predicate<SenderType>> reqType;
        Inheritable<Predicate<CommandSender>> required;

        InheritedData() {
            this.fallback = new InheritableFallbackAction();
            this.reqType = new Inheritable<>(DEFAULT_SENDER_TYPE);
            this.required = new Inheritable<>(DEFAULT_REQUIREMENT);
        }

        void setFallback(FallbackAction action) {
            fallback = new InheritableFallbackAction(action);
        }

        void inheritFrom(InheritedData parent) {
            fallback.inheritFrom(parent.fallback);
            reqType.inherit(parent.reqType);
            required.inherit(parent.required);
        }

        Predicate<SenderType> getSenderType() {
            return reqType.get();
        }

        Predicate<CommandSender> getRequirement() {
            return required.get();
        }

        InheritableFallbackAction getFallback() {
            return fallback;
        }
    }

    static class InheritableFallbackAction implements FallbackAction {

        Inheritable<BiConsumer<Context, CommandSyntaxException>> invalidInput;
        Inheritable<SyntaxConsumer<Context>> failure;
        Inheritable<SyntaxBiConsumer<Context, Throwable>> exception;
        Inheritable<Consumer<CommandSender>> noPermission;
        Inheritable<Consumer<CommandSender>> invalidSender;

        InheritableFallbackAction() {
            this.invalidInput = new Inheritable<>(DEFAULT_INVALID_INPUT);
            this.failure = new Inheritable<>(DEFAULT_FAILURE);
            this.exception = new Inheritable<>(DEFAULT_EXCEPTION);
            this.noPermission = new Inheritable<>(DEFAULT_NO_PERM);
            this.invalidSender = new Inheritable<>(DEFAULT_INVALID_SENDER);
        }

        InheritableFallbackAction(FallbackAction source) {
            this.invalidInput = new Inheritable<>(source::invalidInput);
            this.failure = new Inheritable<>(source::failure);
            this.exception = new Inheritable<>(source::exception);
            this.noPermission = new Inheritable<>(source::noPermission);
            this.invalidSender = new Inheritable<>(source::invalidSender);
        }

        void inheritFrom(InheritableFallbackAction action) {
            invalidInput.inherit(action.invalidInput);
            failure.inherit(action.failure);
            exception.inherit(action.exception);
            noPermission.inherit(action.noPermission);
            invalidSender.inherit(action.invalidSender);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InheritableFallbackAction) {
                InheritableFallbackAction act = (InheritableFallbackAction) obj;
                return     (invalidInput.equals(act.invalidInput))
                        && (failure.equals(act.failure))
                        && (exception.equals(act.exception))
                        && (invalidSender.equals(act.invalidSender))
                        && (noPermission.equals(act.noPermission));
            }
            return false;
        }

        @Override
        public void failure(Context context) throws CommandSyntaxException {
            this.failure.get().accept(context);
        }

        @Override
        public void exception(Context context, Throwable exception) throws CommandSyntaxException {
            this.exception.get().accept(context, exception);
        }

        @Override
        public void invalidInput(Context context, CommandSyntaxException e) {
            this.invalidInput.get().accept(context, e);
        }

        @Override
        public void noPermission(CommandSender sender) {
            this.noPermission.get().accept(sender);
        }

        @Override
        public void invalidSender(CommandSender sender) {
            this.invalidSender.get().accept(sender);
        }
    }

    @FunctionalInterface
    public interface SyntaxConsumer<T> {
        void accept(T a) throws CommandSyntaxException;
    }

    @FunctionalInterface
    public interface SyntaxBiConsumer<T, U> {
        void accept(T a, U b) throws CommandSyntaxException;
    }
}

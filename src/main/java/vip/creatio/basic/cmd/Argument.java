package vip.creatio.basic.cmd;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.CommandListenerWrapper;
import org.bukkit.command.CommandSender;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.util.NMS;

import java.util.Collection;
import java.util.function.Predicate;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class Argument {

    protected static final Message defaultErrMsg = Component.of("Failed to execute command.");

    protected final ArgumentBuilder<CommandListenerWrapper, ? extends ArgumentBuilder<CommandListenerWrapper, ?>> builder;
    protected CommandAction command;
    protected Predicate<CommandSender> requirement;

    protected Argument(ArgumentBuilder<CommandListenerWrapper, ? extends ArgumentBuilder<CommandListenerWrapper, ?>> builder) {
        this.builder = builder;
    }

    public CommandAction getCommand() {
        return command;
    }

    public Argument then(Argument argument) {
        builder.then(argument.builder);
        return this;
    }


    public Argument then(CommandNode<?> argument) {
        builder.then((CommandNode<CommandListenerWrapper>) argument);
        return this;
    }

    public static LiteralArgument of(String option) {
        return LiteralArgument.of(option);
    }

    public static <T> RequiredArgument<T> arg(String name, ArgumentType<T> type) {
        return RequiredArgument.arg(name, type);
    }

    public Collection<CommandNode<?>> getArguments() {
        return (Collection) builder.getArguments();
    }

    public Argument executes(CommandAction command) {
        builder.executes(c -> {
            Content content = new Content(c, defaultErrMsg);
            try {
                if (command.run(content)) {
                    return 1;
                } else {
                    throw new CommandSyntaxException(new CommandExceptionType(){}, content.getErrMessage());
                }
            } catch (CommandSyntaxException e) {
                throw e;
            } catch (Throwable t) {
                String errMsg = "Unhandled exception executing Brigadier command '" + c.getRootNode().getName() + "'!";
                System.err.println(errMsg);
                throw new CommandSyntaxException(new CommandExceptionType(){}, Component.of(errMsg));
            }
        });
        return this;
    }

    public Predicate<CommandSender> getRequirement() {
        return requirement;
    }

    public Argument requires(Predicate<CommandSender> requirement) {
        builder.requires(w -> requirement.test(NMS.toBukkit(w)));
        return this;
    }

    public CommandNode<?> getRedirect() {
        return builder.getRedirect();
    }

    public Argument redirect(CommandNode<?> target) {
        builder.redirect((CommandNode<CommandListenerWrapper>) target);
        return this;
    }

    public CommandNode<?> build() {
        return builder.build();
    }
}

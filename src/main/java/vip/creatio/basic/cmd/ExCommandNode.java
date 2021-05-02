package vip.creatio.basic.cmd;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.CommandListenerWrapper;
import org.bukkit.command.CommandSender;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.util.NMS;

import java.util.function.Predicate;

interface ExCommandNode {

    CommandSyntaxException NO_MESSAGE = new CommandSyntaxException(new CommandExceptionType(){}, Component.create());

    FallbackAction getFallback();

    CommandAction getCommandAction();

    RedirectSource getRedirectSource();

    Predicate<CommandSender> getSenderPredicate();

    Predicate<SenderType> getRequiredSenderType();

    Argument.Inheritable getInheritable();

    boolean isRestricted();

    /** returns self */
    CommandNode<?> getNode();

    default int executeAction(CommandContext<CommandListenerWrapper> c) throws CommandSyntaxException {
        if (accessiblyTest(NMS.toBukkit(c.getSource()))) {
            Context context = new Context(c);
            try {
                if (getCommandAction() != null && getCommandAction().run(context)) {
                    return 1;
                } else {
                    getFallback().failure(context);
                }
            } catch (CommandSyntaxException e) {
                getFallback().failure(context);
            } catch (Throwable t) {
                // Exception fallback
                getFallback().exception(context, t);
            }
        } else {
            throw NO_MESSAGE;
        }
        return 0;
    }

    default boolean accessiblyTest(CommandSender sender) {
        if (!getSenderPredicate().test(sender)) {
            // No permission fallback
            getFallback().noPermission(sender);
            return false;
        }
        if (!getRequiredSenderType().test(SenderType.of(sender))) {
            // Invalid Sender fallback
            getFallback().invalidSender(sender);
            return false;
        }
        return true;
    }

    default boolean accessiblyTestSilent(CommandSender sender) {
        return getRequiredSenderType().test(SenderType.of(sender)) && getSenderPredicate().test(sender);
    }
}

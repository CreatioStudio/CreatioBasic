package vip.creatio.basic.cmd;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import vip.creatio.basic.chat.ChatFormat;
import vip.creatio.basic.chat.ClickEvent;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.util.NMS;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface FallbackAction {

    Default DEFAULT = new Default();
    Consumer<CommandSender> DEFAULT_INVALID_SENDER = DEFAULT::invalidSender;
    Consumer<CommandSender> DEFAULT_NO_PERM = DEFAULT::noPermission;
    Argument.SyntaxBiConsumer<Context, Throwable> DEFAULT_EXCEPTION = DEFAULT::exception;
    Argument.SyntaxConsumer<Context> DEFAULT_FAILURE = DEFAULT::failure;
    BiConsumer<Context, CommandSyntaxException> DEFAULT_INVALID_INPUT = DEFAULT::invalidInput;

    CommandSyntaxException NO_MESSAGE = new CommandSyntaxException(new CommandExceptionType(){}, Component.create());

    /**
     * Called when CommandAction returns false,
     * Throw a CommandSyntaxException to report command execution failed.
     * Execution failure mainly affects chain command block
     *
     * If no exception is thrown, Minecraft will believe this command execution is succeed.
     */
    void failure(Context context) throws CommandSyntaxException;

    /**
     * Called when command throws an exception
     * Throw a CommandSyntaxException to report command execution failed.
     * Execution failure mainly affects chain command block
     *
     * If no exception is thrown, Minecraft will believe this command execution is succeed.
     */
    void exception(Context context, Throwable exception) throws CommandSyntaxException;

    /**
     * Called when the command input didn't pass parsing step
     *
     * Command execution will always fail if this method is called
     */
    void invalidInput(Context context, CommandSyntaxException e);

    /**
     * Called when command node permission test failed
     *
     * Command execution will always fail if this method is called
     */
    void noPermission(CommandSender sender);

    /**
     * Called when command node executor test failed
     *
     * Command execution will always fail if this method is called
     */
    void invalidSender(CommandSender sender);



    /**
     * Send a failure message to command sender.
     * For example, the bottom message box in command block
     */
    default void sendFailureMessage(CommandSender sender, Message message) {
        NMS.toNms(sender).sendFailureMessage(Component.wrap(message).unwrap());
    }

    /**
     * Send a failure message to command sender.
     * For example, the bottom message box in command block
     */
    default void sendFailureMessage(CommandSender sender, String message) {
        sendFailureMessage(sender, Component.of(message));
    }

    /** Efficient ways to create exception with failure message */
    default CommandSyntaxException reportFailure(Message message) {
        return new CommandSyntaxException(new CommandExceptionType(){}, Component.wrap(message));
    }

    /** Efficient ways to create exception with failure message */
    default CommandSyntaxException reportFailure(String message) {
        return reportFailure(Component.of(message));
    }



    /**
     * Default implementation of FallbackAction, can be inherit.
     * This implementation reacts like a vanilla command
     */
    class Default implements FallbackAction {

        @Override
        public void invalidInput(Context context, CommandSyntaxException e) {
            sendFailureMessage(context.getSender(), Component.wrap(e.getRawMessage()));
            if (e.getInput() != null && e.getCursor() >= 0) {
                int maxLen = Math.min(context.getInput().length(), e.getCursor());
                Component comp = Component.create().withColor(ChatFormat.GRAY).withClickEvent(ClickEvent.suggestCmd(context.getLabel()));

                if (maxLen > 10) {
                    comp.append("...");
                }

                comp.append(context.getInput().substring(Math.max(0, maxLen - 10), maxLen));
                if (maxLen < context.getInput().length()) {
                    Component redPart = Component.of(context.getInput().substring(maxLen)).withUnderline(true).withColor(ChatFormat.RED);

                    comp.append(redPart);
                }

                comp.append(Component.translate("command.context.here").withItalic(true).withColor(ChatFormat.GOLD));
                sendFailureMessage(context.getSender(), comp);
            }
        }

        @Override
        public void failure(Context context) throws CommandSyntaxException {
            throw reportFailure("Failed to execute this command");
        }

        @Override
        public void exception(Context context, Throwable exception) throws CommandSyntaxException {
            String errMsg = "Unhandled exception executing Brigadier command '" + context.getLabel() + "'!";
            System.err.println(errMsg);
            exception.printStackTrace();
            throw reportFailure(errMsg);
        }

        @Override
        public void noPermission(CommandSender sender) {
            sender.sendMessage("You don't have permission to execute this command!");
        }

        @Override
        public void invalidSender(CommandSender sender) {
            if (sender instanceof Player) {
                sendFailureMessage(sender, "This command cannot be execute by player!");
            } else if (sender instanceof ConsoleCommandSender) {
                sendFailureMessage(sender, "This command cannot be execute by console!");
            }
        }
    }
}

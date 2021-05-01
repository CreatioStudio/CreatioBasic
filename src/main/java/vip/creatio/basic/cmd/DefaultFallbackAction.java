package vip.creatio.basic.cmd;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.*;
import org.bukkit.command.CommandSender;
import vip.creatio.basic.chat.ChatFormat;
import vip.creatio.basic.chat.ClickEvent;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.util.NMS;

public class DefaultFallbackAction implements FallbackAction {

    public static final Message DEFAULT_MSG_FAILED = Component.of("Failed to execute this command");
    public static final DefaultFallbackAction DEFAULT = new DefaultFallbackAction();

    @Override
    public void invalidInput(Context context, CommandSyntaxException e) {
        CommandListenerWrapper wrapper = NMS.toNms(context.getSource());
        wrapper.sendFailureMessage(Component.wrap(e.getRawMessage()).unwrap());
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
            wrapper.sendFailureMessage(comp.unwrap());
        }
    }

    @Override
    public void failed(Context context) throws CommandSyntaxException {
        throw new CommandSyntaxException(new CommandExceptionType() {
        }, DEFAULT_MSG_FAILED);
    }

    @Override
    public void exception(Context context, Throwable exception) throws CommandSyntaxException {
        String errMsg = "Unhandled exception executing Brigadier command '" + context.getRootNode().getName() + "'!";
        System.err.println(errMsg);
        throw new CommandSyntaxException(new CommandExceptionType() {
        }, Component.of(errMsg));
    }

    @Override
    public void noPermission(CommandSender sender) {
        sender.sendMessage("You don't have permission to execute this command!");
    }
}

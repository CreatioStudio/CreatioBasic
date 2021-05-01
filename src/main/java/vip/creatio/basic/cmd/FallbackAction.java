package vip.creatio.basic.cmd;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.command.CommandSender;

public interface FallbackAction {

    void invalidInput(Context context, CommandSyntaxException e) throws CommandSyntaxException;

    void failed(Context context) throws CommandSyntaxException;

    void exception(Context context, Throwable exception) throws CommandSyntaxException;

    void noPermission(CommandSender sender);

}

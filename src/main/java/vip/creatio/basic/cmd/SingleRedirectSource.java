package vip.creatio.basic.cmd;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface SingleRedirectSource {

    CommandSender apply(Context context) throws CommandSyntaxException;

}

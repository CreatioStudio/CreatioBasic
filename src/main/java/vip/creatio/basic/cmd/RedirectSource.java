package vip.creatio.basic.cmd;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.command.CommandSender;

import java.util.Collection;

@FunctionalInterface
public interface RedirectSource {

    Collection<CommandSender> apply(Context context) throws CommandSyntaxException;

}

package vip.creatio.basic.cmd;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface CommandAction {

    boolean run(Context context) throws CommandSyntaxException;

}

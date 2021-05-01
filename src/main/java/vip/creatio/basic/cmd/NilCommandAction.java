package vip.creatio.basic.cmd;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

/**
 * A special CommandAction that will always return true.
 */
@FunctionalInterface
public interface NilCommandAction extends CommandAction {

    void accept(Context context) throws CommandSyntaxException;

    @Override
    default boolean run(Context context) throws CommandSyntaxException {
        accept(context);
        return true;
    }
}

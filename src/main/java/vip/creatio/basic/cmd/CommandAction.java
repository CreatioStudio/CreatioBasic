package vip.creatio.basic.cmd;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface CommandAction {

    boolean run(Context context) throws CommandSyntaxException;

    /**
     * A special CommandAction that will always return true.
     */
    @FunctionalInterface
    interface Nil extends CommandAction {

        void accept(Context context) throws CommandSyntaxException;

        @Override
        default boolean run(Context context) throws CommandSyntaxException {
            accept(context);
            return true;
        }
    }
}

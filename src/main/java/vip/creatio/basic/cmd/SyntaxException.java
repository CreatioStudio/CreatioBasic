package vip.creatio.basic.cmd;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

/**
 * A custom CommandSyntaxException for ExternArgumentTypes
 */
public class SyntaxException extends CommandSyntaxException {

    private final Object[] args;

    public SyntaxException(CommandExceptionType type, Message message, String input, int cursor, Object... args) {
        super(type, message, input, cursor);
        this.args = args;
    }

    public Object[] getArguments() {
        return args;
    }

    public int getArgumentCount() {
        return args.length;
    }

}

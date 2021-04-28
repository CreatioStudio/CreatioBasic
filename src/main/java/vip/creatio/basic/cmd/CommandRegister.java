package vip.creatio.basic.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public interface CommandRegister {

    CommandDispatcher<?> getCommandDispatcher();

    Command register(@NotNull LiteralCommandNode<?> node,
                            @NotNull String description);

    default Command register(@NotNull LiteralArgument arg,
                             @NotNull String description) {
        return register(arg.build(), description);
    }

    Command register(@NotNull LiteralCommandNode<?> node,
                     @NotNull String description,
                     @NotNull List<String> aliases);

    default Command register(@NotNull LiteralArgument arg,
                             @NotNull String description,
                             @NotNull List<String> aliases) {
        return register(arg.build(), description, aliases);
    }

    Command register(@NotNull LiteralCommandNode<?> node,
                     @NotNull String description,
                     @NotNull List<String> aliases,
                     @NotNull Predicate<? super Player> permissionTest);

    default Command register(@NotNull LiteralArgument arg,
                     @NotNull String description,
                     @NotNull List<String> aliases,
                     @NotNull Predicate<? super Player> permissionTest) {
        return register(arg.build(), description, aliases, permissionTest);
    }

}

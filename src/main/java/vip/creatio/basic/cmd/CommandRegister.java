package vip.creatio.basic.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public interface CommandRegister {

    CommandDispatcher<?> getCommandDispatcher();

    SimpleCommandMap getCommandMap();

    void register(@NotNull LiteralCommandNode<?> node,
                            @NotNull String description);

    default void register(@NotNull LiteralArgument arg,
                             @NotNull String description) {
        register(arg.build(), description);
    }

    void register(@NotNull LiteralCommandNode<?> node,
                     @NotNull String description,
                     @NotNull List<String> aliases);

    default void register(@NotNull LiteralArgument arg,
                             @NotNull String description,
                             @NotNull List<String> aliases) {
        register(arg.build(), description, aliases);
    }

    void register(@NotNull LiteralCommandNode<?> node,
                     @NotNull String description,
                     @NotNull List<String> aliases,
                     @NotNull Predicate<? super CommandSender> permissionTest);

    default void register(@NotNull LiteralArgument arg,
                     @NotNull String description,
                     @NotNull List<String> aliases,
                     @NotNull Predicate<? super CommandSender> permissionTest) {
        register(arg.build(), description, aliases, permissionTest);
    }

    void unregister(@NotNull String name);

}

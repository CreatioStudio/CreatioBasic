package vip.creatio.basic.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface CommandRegister {

    CommandDispatcher<?> getCommandDispatcher();

    SimpleCommandMap getCommandMap();

    void register(@NotNull LiteralCommandNode<?> node,
                  @NotNull String description);

    void register(@NotNull LiteralCommandNode<?> node,
                  @NotNull String description,
                  @NotNull List<String> aliases);

    void register(@NotNull LiteralCommandNode<?> node,
                  @NotNull String description,
                  @NotNull List<String> aliases,
                  @Nullable HelpTopicFactory<BrigadierCommand> helpTopic);

    default void register(@NotNull LiteralArgument arg,
                          @NotNull String description) {
        register(arg.build(), description);
    }

    default void register(@NotNull LiteralArgument arg,
                          @NotNull String description,
                          @NotNull List<String> aliases) {
        register(arg.build(), description, aliases);
    }

    default void register(@NotNull LiteralArgument arg,
                          @NotNull String description,
                          @NotNull List<String> aliases,
                          @Nullable HelpTopicFactory<BrigadierCommand> helpTopic) {
        register(arg.build(), description, aliases, helpTopic);
    }

    void unregister(@NotNull String name);

    /** Can be get through command name and aliases */
    BrigadierCommand getCommand(String name);

    List<BrigadierCommand> getCommands();

    Map<String, String[]> getAliases();
}

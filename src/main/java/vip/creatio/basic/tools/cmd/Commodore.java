package vip.creatio.basic.tools.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.server.CommandListenerWrapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.BukkitCommandWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import vip.creatio.basic.util.BukkitUtil;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Commodore {

    // ArgumentCommandNode#customSuggestions field
    private static final Var<SuggestionProvider<?>> CUSTOM_SUGGESTIONS = Reflection.field(ArgumentCommandNode.class, "customSuggestions");

    // CommandDispatcher#CommandDispatcher field
    private static final Var<CommandDispatcher<Object>> BRIGADIER_DISPATCHER = Reflection.field(net.minecraft.server.CommandDispatcher.class, 0);

    // CommandNode#children, CommandNode#literals, CommandNode#arguments fields
    private static final Var<Map<String, CommandNode<?>>> CHILDREN_FIELD = Reflection.field(CommandNode.class, "children");
    private static final Var<Map<String, LiteralCommandNode<?>>> LITERALS_FIELD = Reflection.field(CommandNode.class, "literals");
    private static final Var<Map<String, ArgumentCommandNode<?, ?>>> ARGUMENTS_FIELD = Reflection.field(CommandNode.class, "arguments");

    private final Plugin plugin;
    private final List<LiteralCommandNode<?>> registeredNodes = new ArrayList<>();

    public Commodore(Plugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(new ServerReloadListener(), this.plugin);
    }

    public CommandDispatcher<?> getDispatcher() {
        net.minecraft.server.CommandDispatcher dispatcher = BukkitUtil.getServer().getCommandDispatcher();
        return BRIGADIER_DISPATCHER.get(dispatcher);
    }

    public CommandSender getBukkitSender(CommandListenerWrapper wrapper) {
        Objects.requireNonNull(wrapper, "commandWrapperListener");
        return wrapper.getBukkitSender();
    }

    public List<LiteralCommandNode<?>> getRegisteredNodes() {
        return Collections.unmodifiableList(this.registeredNodes);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void register(LiteralCommandNode<?> node) {
        Objects.requireNonNull(node, "node");

        CommandDispatcher dispatcher = getDispatcher();
        RootCommandNode root = dispatcher.getRoot();

        removeChild(root, node.getName());
        root.addChild(node);
        this.registeredNodes.add(node);
    }

    @SuppressWarnings("unchecked")
    public void register(Command command, LiteralCommandNode<?> node, Predicate<? super Player> permissionTest) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(permissionTest, "permissionTest");

        try {
            SuggestionProvider<?> wrapper = (SuggestionProvider<?>) new BukkitCommandWrapper((CraftServer) this.plugin.getServer(), command);
            setCustomSuggestionProvider(node, wrapper);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Collection<String> aliases = Commodore.getAliases(command);
        if (!aliases.contains(node.getLiteral())) {
            node = renameLiteralNode(node, command.getName());
        }

        for (String alias : aliases) {
            if (node.getLiteral().equals(alias)) {
                register(node);
            } else {
                register(LiteralArgumentBuilder.literal(alias).redirect((LiteralCommandNode<Object>)node).build());
            }
        }

        this.plugin.getServer().getPluginManager().registerEvents(new CommandDataSendListener(command, permissionTest), this.plugin);
    }

    public void register(Command command, LiteralCommandNode<?> node) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(node, "node");

        register(command, node, command::testPermissionSilent);
    }

    @SuppressWarnings("rawtypes")
    private static void removeChild(RootCommandNode root, String name) {
        Map<String, ?> children;
        children = CHILDREN_FIELD.get(root);
        children.remove(name);

        children = LITERALS_FIELD.get(root);
        children.remove(name);

        children = ARGUMENTS_FIELD.get(root);
        children.remove(name);
    }

    private static void setCustomSuggestionProvider(CommandNode<?> node, SuggestionProvider<?> suggestionProvider) {
        if (node instanceof ArgumentCommandNode) {
            ArgumentCommandNode<?, ?> argumentNode = (ArgumentCommandNode<?, ?>) node;
            CUSTOM_SUGGESTIONS.set(argumentNode, suggestionProvider);
        }

        // apply recursively to child nodes
        for (CommandNode<?> child : node.getChildren()) {
            setCustomSuggestionProvider(child, suggestionProvider);
        }
    }

    private static <S> LiteralCommandNode<S> renameLiteralNode(LiteralCommandNode<S> node, String newLiteral) {
        LiteralCommandNode<S> clone = new LiteralCommandNode<>(newLiteral, node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());
        for (CommandNode<S> child : node.getChildren()) {
            clone.addChild(child);
        }
        return clone;
    }

    /**
     * Listens for server (re)loads, and re-adds all registered nodes to the dispatcher.
     */
    private final class ServerReloadListener implements Listener {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @EventHandler
        public void onLoad(ServerLoadEvent e) {
            CommandDispatcher dispatcher = getDispatcher();
            RootCommandNode root = dispatcher.getRoot();

            for (LiteralCommandNode<?> node : Commodore.this.registeredNodes) {
                removeChild(root, node.getName());
                root.addChild(node);
            }
        }
    }

    /**
     * Removes minecraft namespaced argument data, & data for players without permission to view the
     * corresponding commands.
     */
    private static final class CommandDataSendListener implements Listener {
        private final Set<String> aliases;
        private final Set<String> minecraftPrefixedAliases;
        private final Predicate<? super Player> permissionTest;

        CommandDataSendListener(Command pluginCommand, Predicate<? super Player> permissionTest) {
            this.aliases = new HashSet<>(Commodore.getAliases(pluginCommand));
            this.minecraftPrefixedAliases = this.aliases.stream().map(alias -> "minecraft:" + alias).collect(Collectors.toSet());
            this.permissionTest = permissionTest;
        }

        @EventHandler
        public void onCommandSend(PlayerCommandSendEvent e) {
            // always remove 'minecraft:' prefixed aliases added by craftbukkit.
            // this happens because bukkit thinks our injected commands are vanilla commands.
            e.getCommands().removeAll(this.minecraftPrefixedAliases);

            // remove the actual aliases if the player doesn't pass the permission test
            if (!this.permissionTest.test(e.getPlayer())) {
                e.getCommands().removeAll(this.aliases);
            }
        }
    }

    /**
     * Gets all of the aliases known for the given command.
     *
     * <p>This will include the main label, as well as defined aliases, and
     * aliases including the fallback prefix added by Bukkit.</p>
     *
     * @param command the command
     * @return the aliases
     */
    static Collection<String> getAliases(Command command) {
        Objects.requireNonNull(command, "command");

        Stream<String> aliasesStream = Stream.concat(
                Stream.of(command.getLabel()),
                command.getAliases().stream()
        );

        if (command instanceof PluginCommand) {
            String fallbackPrefix = ((PluginCommand) command).getPlugin().getName().toLowerCase().trim();
            aliasesStream = aliasesStream.flatMap(alias -> Stream.of(
                    alias,
                    fallbackPrefix + ":" + alias
            ));
        }

        return aliasesStream.distinct().collect(Collectors.toList());
    }

}

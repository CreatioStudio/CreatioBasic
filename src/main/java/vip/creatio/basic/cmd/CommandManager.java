package vip.creatio.basic.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.server.CommandListenerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import vip.creatio.basic.util.BukkitUtil;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CommandManager implements CommandRegister {

    // com.mojang.brigadier.CommandDispatcher#CommandDispatcher
    private static final Var<CommandDispatcher<?>> BRIGADIER_DISPATCHER =
            Reflection.field(net.minecraft.server.CommandDispatcher.class, 0);

    // CommandNode#children, CommandNode#literals, CommandNode#arguments
    private static final Var<Map<String, CommandNode<?>>> CHILDREN_FIELD =
            Reflection.field(CommandNode.class, "children");
    private static final Var<Map<String, LiteralCommandNode<?>>> LITERALS_FIELD =
            Reflection.field(CommandNode.class, "literals");
    private static final Var<Map<String, ArgumentCommandNode<?, ?>>> ARGUMENTS_FIELD =
            Reflection.field(CommandNode.class, "arguments");

    private static final Var<Map<String, Command>> KNOWN_COMMANDS =
            Reflection.field(SimpleCommandMap.class, "knownCommands");

    private final Plugin plugin;
    private final String prefix;
    private final Map<LiteralCommandNode<?>, Predicate<? super Player>> registeredCommand = new HashMap<>();
    private final Map<String, LiteralCommandNode<?>> registeredCommandString = new HashMap<>();
    private CommandDispatcher<?> cache;

    public CommandManager(Plugin plugin) {
        this(plugin, plugin.getName());
    }

    public CommandManager(Plugin plugin, String prefix) {
        this.plugin = plugin;
        this.prefix = prefix;
        Bukkit.getPluginManager().registerEvent(PlayerCommandSendEvent.class,
                new Listener(){}, EventPriority.MONITOR, (l, e) -> {
                    if (PlayerCommandSendEvent.class.isAssignableFrom(e.getClass()))
                        this.onCommandSend((PlayerCommandSendEvent) e);
                }, plugin);
        Bukkit.getPluginManager().registerEvent(PluginDisableEvent.class,
                new Listener() {}, EventPriority.NORMAL, (l, e) -> {
                    if (PluginDisableEvent.class.isAssignableFrom(e.getClass()))
                        this.onPluginDisable((PluginDisableEvent) e);
                }, plugin);
    }

    public CommandDispatcher<?> getCommandDispatcher() {
        if (cache == null) {
            net.minecraft.server.CommandDispatcher dispatcher = getNMSDispatcher();
            cache = BRIGADIER_DISPATCHER.get(dispatcher);
        }

        return cache;
    }

    private net.minecraft.server.CommandDispatcher getNMSDispatcher() {
        return BukkitUtil.getServer().getCommandDispatcher();
    }

    private RootCommandNode<?> getRootNode() {
        return getCommandDispatcher().getRoot();
    }

    private void clearRoot(RootCommandNode<?> root, String name) {
        CHILDREN_FIELD.get(root).remove(name);
        LITERALS_FIELD.get(root).remove(name);
        ARGUMENTS_FIELD.get(root).remove(name);
    }

    private void clearConflict(SimpleCommandMap map, String name) {
        Command conflict = map.getCommand(name);
        String lableName = prefix + ':' + name;
        String fallback = "minecraft:" + name;
        if (conflict != null && (conflict.getLabel().equals(lableName)
                || conflict.getLabel().equals(fallback))) {
            conflict.unregister(map);
            Map<String, ?> knownCommand = KNOWN_COMMANDS.get(map);

            knownCommand.remove(name);
            knownCommand.remove(lableName);
            knownCommand.remove(fallback);
        }
    }

    private SimpleCommandMap getCommandMap() {
        return BukkitUtil.getCommandMap();
    }

    public Command register(@NotNull LiteralCommandNode<?> node,
                            @NotNull String description) {
        return register(node, description, new ArrayList<>());
    }

    public Command register(@NotNull LiteralCommandNode<?> node,
                            @NotNull String description,
                            @NotNull List<String> aliases) {
        return register(node, description, aliases, p -> true);
    }

    public Command register(@NotNull LiteralCommandNode<?> node,
                            @NotNull String description,
                            @NotNull List<String> aliases,
                            @NotNull Predicate<? super Player> permissionTest) {

        clearConflict(getCommandMap(), node.getName());

        Command wrapper = new VanillaCommandWrapper(getNMSDispatcher(),
                (CommandNode<CommandListenerWrapper>) node);
        wrapper.setDescription(description);
        wrapper.setAliases(aliases);
        wrapper.setPermission(null);

        getCommandMap().register(prefix, wrapper);
        RootCommandNode<?> rootNode = getRootNode();
        clearRoot(rootNode, node.getName());

        rootNode.addChild((LiteralCommandNode) node);
        registeredCommand.put(node, permissionTest);
        registeredCommandString.put(node.getName(), node);
        registeredCommandString.put(prefix + ':' + node.getName(), node);

        return wrapper;
    }

    private void onCommandSend(PlayerCommandSendEvent event) {
        event.getCommands().removeIf(next ->
                !registeredCommand.get(registeredCommandString.get(next)).test(event.getPlayer()));
    }

    private void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() == plugin) {
            RootCommandNode<?> root = getRootNode();
            for (String name : registeredCommandString.keySet()) {
                clearRoot(root, name);
            }
        }
    }

    public SuggestionProvider suggestText(String... text) {
        return (c, b) -> {
            for (String s : text) {
                b.suggest(s);
            }
            return b.buildFuture();
        };
    }

    public SuggestionProvider suggestPlayer(Collection<Player> plr) {
        return (c, b) -> {
            for (Player p : plr) {
                b.suggest(p.getName());
            }
            return b.buildFuture();
        };
    }
}

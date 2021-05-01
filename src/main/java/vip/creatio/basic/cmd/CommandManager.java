package vip.creatio.basic.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import vip.creatio.basic.annotation.Task;
import vip.creatio.basic.annotation.TaskType;
import vip.creatio.basic.util.BukkitUtil;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Predicate;

/**
 * Brigadier implementation of CommandRegister instance, the required LiteralCommandNode can be get
 * through Argument::build, or vanilla Brigadier's ArgumentBuilder::build,
 */
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

    // Weak cache, in case some of the CommandManager are intermediate
    private static final List<WeakReference<CommandManager>> MGR_LIST = new ArrayList<>();
    private static CommandDispatcher<?> cache;

    private final Plugin plugin;
    private final String prefix;
    private final Map<String, BrigadierCommand> registeredCommand = new HashMap<>();
    private final Map<String, BrigadierCommand> aliases = new HashMap<>();
    private boolean initialized;

    public CommandManager(Plugin plugin) {
        this(plugin, plugin.getName());
    }

    public CommandManager(Plugin plugin, String prefix) {
        this.plugin = plugin;
        this.prefix = prefix;
        MGR_LIST.add(new WeakReference<>(this));
    }

    public CommandDispatcher<?> getCommandDispatcher() {
        return getCommandDispatcher0();
    }

    private static CommandDispatcher<?> getCommandDispatcher0() {
        if (cache == null) {
            net.minecraft.server.CommandDispatcher dispatcher = getNMSDispatcher();
            return BRIGADIER_DISPATCHER.get(dispatcher);
        }

        return cache;
    }

    private static net.minecraft.server.CommandDispatcher getNMSDispatcher() {
        return BukkitUtil.getServer().getCommandDispatcher();
    }

    private static RootCommandNode<?> getRootNode() {
        return getCommandDispatcher0().getRoot();
    }

    public void unregister(@NotNull String name) {
        BrigadierCommand cmd = registeredCommand.remove(name);
        if (cmd != null) {
            clearMap(cmd.getCommandMap(), prefix, name);
            clearRoot(getRootNode(), name);
            for (String alias : cmd.getAliases()) {
                clearMap(cmd.getCommandMap(), prefix, alias);
                clearRoot(getRootNode(), alias);
                aliases.remove(alias);
            }
            syncCommand();
        }
    }

    private void clearMap(CommandMap map, String prefix, String name) {
        Map<String, ?> knownCommand = KNOWN_COMMANDS.get(map);
        Command conflict = map.getCommand(name);
        clearMap0(map, knownCommand, conflict, name);

        conflict = map.getCommand(plugin.getName() + ':' + name);
        clearMap0(map, knownCommand, conflict, name);

        conflict = map.getCommand(prefix + ':' + name);
        clearMap0(map, knownCommand, conflict, name);
    }

    private static void clearMap0(CommandMap map, Map<String, ?> knownCommand, Command conflict, String name) {
        if (conflict != null) {
            conflict.unregister(map);
            knownCommand.remove(name);
        }
    }

    private static void clearRoot(RootCommandNode<?> root, String name) {
        CHILDREN_FIELD.get(root).remove(name);
        LITERALS_FIELD.get(root).remove(name);
        ARGUMENTS_FIELD.get(root).remove(name);
    }

    public static void syncCommand() {
        RootCommandNode<?> root = getRootNode();

        for (Map.Entry<String, Command> entry : KNOWN_COMMANDS.get(getCommandMap0()).entrySet()) {
            String label = entry.getKey();
            Command command = entry.getValue();

            if (command instanceof BrigadierCommand) {
                LiteralCommandNode<?> node = ((BrigadierCommand) command).getCommand();
                if (!node.getLiteral().equals(label)) {
                    LiteralCommandNode<?> alias = node instanceof ExLiteralCommandNode
                            ? new ExLiteralCommandNode(
                                    label,
                                    ((ExLiteralCommandNode) node).getCommandAction(),
                                    ((ExLiteralCommandNode) node).getPredicate(),
                                    node.getRedirect(),
                                    ((ExLiteralCommandNode) node).getRedirectSource(),
                                    node.isFork(),
                                    new FallbackAction[]{((ExLiteralCommandNode) node).getFallback()})
                            : new LiteralCommandNode(label, node.getCommand(), node.getRequirement(), node.getRedirect(),
                                    node.getRedirectModifier(), node.isFork());

                    for (CommandNode<?> child : node.getChildren()) {
                        alias.addChild((CommandNode) child);
                    }
                    node = alias;
                    if (root.getChild(label) != null) clearRoot(root, label);
                }

                root.addChild((CommandNode) node);
            }
        }

        BukkitUtil.syncCommand(getRootNode());
    }

    public SimpleCommandMap getCommandMap() {
        return getCommandMap0();
    }

    private static SimpleCommandMap getCommandMap0() {
        return BukkitUtil.getCommandMap();
    }

    public void register(@NotNull LiteralCommandNode<?> node,
                         @NotNull String description) {
        register(node, description, new ArrayList<>());
    }

    public void register(@NotNull LiteralCommandNode<?> node,
                         @NotNull String description,
                         @NotNull List<String> aliases) {
        register(node, description, aliases, p -> true);
    }

    public void register(@NotNull LiteralCommandNode<?> node,
                         @NotNull String description,
                         @NotNull List<String> aliases,
                         @NotNull Predicate<? super CommandSender> permissionTest) {
        register(node, description, aliases, permissionTest, true);
    }

    public void register(@NotNull LiteralCommandNode<?> node,
                         @NotNull String description,
                         @NotNull List<String> aliases,
                         @NotNull Predicate<? super CommandSender> permissionTest,
                         boolean showInHelp) {
        BrigadierCommand command = new BrigadierCommand(this,
                (LiteralCommandNode) node, description, aliases, permissionTest, showInHelp);

        registeredCommand.put(node.getName(), command);
        for (String alias : aliases) {
            this.aliases.put(alias, command);
        }

        // If server already initialized then we can directly register this command
        if (initialized) {
            CommandMap map = getCommandMap0();
            internalRegister(map, command);
            syncCommand();
        }
    }

    @Task(TaskType.POST_WORLD)
    static void onPostWorld() {
        cache = getCommandDispatcher0();
        for (WeakReference<CommandManager> ref : MGR_LIST) {
            CommandManager mgr = ref.get();
            if (mgr != null) {
                mgr.initCommand();
            }
        }
        syncCommand();
    }

    @Task(TaskType.ON_UNLOAD)
    static void onUnLoad() {
        for (WeakReference<CommandManager> ref : MGR_LIST) {
            CommandManager mgr = ref.get();
            if (mgr != null) {
                mgr.onDisable();
            }
        }
        BukkitUtil.syncCommand(getRootNode());
    }

    private void initCommand() {
        // Initialization lock
        if (initialized) return;
        initialized = true;

        CommandMap map = getCommandMap0();
        for (BrigadierCommand cmd : registeredCommand.values()) {
            internalRegister(map, cmd);
        }
    }

    private void internalRegister(CommandMap map, BrigadierCommand cmd) {
        clearMap(map, prefix, cmd.getName());
        clearRoot(getRootNode(), cmd.getName());
        getRootNode().addChild((LiteralCommandNode) cmd.getCommand());

        map.register(prefix, cmd);
        cmd.onRegister();
    }

    private void onDisable() {
        RootCommandNode<?> root = getRootNode();

        Set<String> aliases = this.aliases.keySet();
        for (Map.Entry<String, Command> entry : KNOWN_COMMANDS.get(getCommandMap0()).entrySet()) {
            String label = entry.getKey();
            Command command = entry.getValue();

            if (command instanceof BrigadierCommand) {
                if (aliases.contains(label))
                    clearRoot(root, label);
            }
        }
        this.registeredCommand.clear();
        this.aliases.clear();
    }
}

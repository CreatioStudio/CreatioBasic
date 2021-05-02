package vip.creatio.basic.cmd;

import com.mojang.brigadier.context.*;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.CommandListenerWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import vip.creatio.basic.util.BukkitUtil;
import vip.creatio.common.util.ReflectUtil;

import java.util.List;
import java.util.Map;

public final class Context {

    private static final Var<Map<String, ParsedArgument<CommandListenerWrapper, ?>>> ARGUMENTS =
            Reflection.field(CommandContext.class, "arguments");

    private final CommandSender source;
    private final String input;
    private final Map<String, ParsedArgument<CommandListenerWrapper, ?>> arguments;
    private final CommandNode<?> rootNode;
    private final List<ParsedCommandNode<CommandListenerWrapper>> nodes;
    private final StringRange range;
    private final boolean forks;

    @SuppressWarnings("unchecked")
    Context(CommandContext<?> wrapper) {
        CommandContext<CommandListenerWrapper> raw = (CommandContext<CommandListenerWrapper>) wrapper;
        this.source = raw.getSource().getBukkitSender();
        this.input = raw.getInput();
        this.arguments = ARGUMENTS.get(raw);
        this.rootNode = raw.getRootNode();
        this.nodes = raw.getNodes();
        this.range = raw.getRange();
        this.forks = raw.isForked();
    }

    Context(CommandSender source,
            String input,
            Map<String, ParsedArgument<CommandListenerWrapper, ?>> arguments,
            CommandNode<?> rootNode,
            List<ParsedCommandNode<CommandListenerWrapper>> nodes,
            StringRange range,
            boolean forks) {
        this.source = source;
        this.input = input;
        this.arguments = arguments;
        this.rootNode = rootNode;
        this.nodes = nodes;
        this.range = range;
        this.forks = forks;
    }

    @SuppressWarnings("unchecked")
    public <V> V getArgument(String name, Class<V> clazz) {
        ParsedArgument<?, ?> argument = this.arguments.get(name);
        if (argument == null) {
            throw new IllegalArgumentException("No such argument '" + name + "' exists on this command");
        } else {
            Object result = argument.getResult();
            clazz = (Class<V>) ArgumentTypes.unwrap(clazz);
            if (ReflectUtil.toWrapper(clazz).isAssignableFrom(result.getClass())) {
                return (V) ArgumentTypes.wrap(result);
            } else {
                throw new IllegalArgumentException("Argument '" + name + "' is defined as " + result.getClass().getSimpleName() + ", not " + clazz);
            }
        }
    }

    public StringRange getRange(String name) {
        for (ParsedCommandNode<?> arg : nodes) {
            if (arg.getNode().getName().equals(name)) return arg.getRange();
        }
        return null;
    }

    public String getInput() {
        return input;
    }

    public CommandSender getSource() {
        return source;
    }

    public Location getLocation() {
        return BukkitUtil.getLocation(source);
    }

    public World getWorld() {
        return BukkitUtil.getWorld(source);
    }

    public CommandNode<?> getRootNode() {
        return rootNode;
    }

    public StringRange getLastNodeRange() {
        return nodes.get(nodes.size() - 1).getRange();
    }

    public StringRange getRange() {
        return range;
    }

    public boolean isForked() {
        return forks;
    }

    public String getLabel() {
        return getRootNode().getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Context)) {
            return false;
        } else {
            Context that = (Context) o;
            if (!this.arguments.equals(that.arguments)) {
                return false;
            } else if (!this.rootNode.equals(that.rootNode)) {
                return false;
            } else if (this.nodes.size() == that.nodes.size() && this.nodes.equals(that.nodes)) {
                return this.source.equals(that.source);
            } else {
                return false;
            }
        }
    }

    @Override
    public int hashCode() {
        int result = this.source.hashCode();
        result = 31 * result + this.arguments.hashCode();
        result = 31 * result + this.rootNode.hashCode();
        result = 31 * result + this.nodes.hashCode();
        return result;
    }
}

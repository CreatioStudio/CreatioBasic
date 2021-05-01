package vip.creatio.basic.cmd;

import com.google.common.base.Joiner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.CommandListenerWrapper;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.util.NMS;

import java.util.*;
import java.util.function.Predicate;

/**
 * A modified version of VanillaCommandWrapper, supports custom invalid input handler
 *
 * Instance of this class has to be registered through CommandManager, otherwise it
 * would behave like a normal BukkitCommand, which doesn't have custom argument color.
 */
public class BrigadierCommand extends BukkitCommand {

    private static final Var<CommandMap> COMMAND_MAP = Reflection.field(Command.class, "commandMap");

    private final LiteralCommandNode<CommandListenerWrapper> command;
    private final Predicate<? super CommandSender> permissionTest;
    private final CommandRegister register;
    private final boolean showInHelp;

    public BrigadierCommand(CommandRegister register,
                            LiteralCommandNode<CommandListenerWrapper> command,
                            String description,
                            List<String> aliases,
                            Predicate<? super CommandSender> permissionTest,
                            boolean showInHelp) {
        super(command.getName(), description, command.getUsageText(), aliases);
        this.command = command;
        this.permissionTest = permissionTest;
        this.register = register;
        this.showInHelp = showInHelp;
    }

    // Called when CommandManager register the command
    protected void onRegister() {

    }

    // Since server's CommandDispatcher changes when server finished plugin loading, which is the time
    // most plugins register their commands, the dispatcher have to require dynamically and should not
    // be cached.
    @SuppressWarnings("unchecked")
    private CommandDispatcher<CommandListenerWrapper> getDispatcher() {
        return (CommandDispatcher<CommandListenerWrapper>) register.getCommandDispatcher();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
        if (!testPermission(sender) || !permissionTest.test(sender)) return true;

        CommandListenerWrapper nms = NMS.toNms(sender);
        args[0] = commandLabel;
        String cmd = connect(args, getName());

        StringReader reader = new StringReader(cmd);

        if (reader.canRead() && reader.peek() == '/') {
            reader.skip();
        }

        ParseResults<CommandListenerWrapper> parsed = getDispatcher().parse(reader, nms);

        try {
            // Dispatch actual command
            getDispatcher().execute(parsed);
        } catch (CommandSyntaxException e) {
            try {
                // Invalid input processor
                List<ParsedCommandNode<CommandListenerWrapper>> nodes = parsed.getContext().getNodes();
                ParsedCommandNode<CommandListenerWrapper> last = nodes.get(nodes.size() - 1);

                // If node is a ExLiteralCommandNode, which supports custom fallback, then use it's fallback
                // else we use Minecraft's default fallback instead.
                (last.getNode() instanceof ExCommandNode
                        ? ((ExCommandNode) last.getNode()).getFallback()
                        : DefaultFallbackAction.DEFAULT).invalidInput(new Context(parsed.getContext().build(cmd)), e);
            } catch (CommandSyntaxException ee) {
                // Final fallback
                nms.sendFailureMessage(Component.wrap(e.getRawMessage()).unwrap());
            }
            return true;
        } catch (Exception e) {
            System.err.println("Exception while executing command " + commandLabel);
            e.printStackTrace();
        }
        return true;
    }

    // Normally this completer wouldn't be used by a Brigadier command, this one is a fallback solution
    // in case this instance got accidentally registered into CommandMap directly.
    @Override public @NotNull List<String>
    tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args, Location location)
    throws IllegalArgumentException {
        Objects.requireNonNull(sender, "Sender cannot be null");
        Objects.requireNonNull(args, "Arguments cannot be null");
        Objects.requireNonNull(alias, "Alias cannot be null");

        CommandListenerWrapper nms = NMS.toNms(sender);
        ParseResults<CommandListenerWrapper> parsed = getDispatcher().parse(connect(args, getName()), nms);

        List<String> results = new ArrayList<>();
        getDispatcher().getCompletionSuggestions(parsed).thenAccept((suggestions) -> {
            suggestions.getList().forEach((s) -> results.add(s.getText()));
        });

        return results;
    }

    private String connect(String[] args, String name) {
        return name + ((args.length > 0) ? " " + Joiner.on(' ').join(args) : "");
    }

    public CommandRegister getRegister() {
        return register;
    }

    public LiteralCommandNode<?> getCommand() {
        return command;
    }

    public CommandMap getCommandMap() {
        return COMMAND_MAP.get(this);
    }
}
package vip.creatio.basic.cmd;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.CommandListenerWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.tools.Wrapper;
import vip.creatio.basic.util.BukkitUtil;

public final class Content implements Wrapper<CommandContext<CommandListenerWrapper>> {

    private final CommandContext<CommandListenerWrapper> raw;

    private Message errMsg;

    Content(CommandContext<CommandListenerWrapper> wrapper, Message errMsg) {
        this.raw = wrapper;
        this.errMsg = errMsg;
    }

    public Message getErrMessage() {
        return errMsg;
    }

    public void setErrMessage(Message msg) {
        this.errMsg = msg;
    }

    public void setErrMessage(String str) {
        this.errMsg = Component.of(str);
    }

    @SuppressWarnings("unchecked")
    public <V> V getArgument(String name, Class<V> clazz) {
        Object obj = raw.getArgument(name, ArgumentTypes.unwrap(clazz));
        return (V) ArgumentTypes.wrap(obj);
    }

    public String getInput() {
        return raw.getInput();
    }

    public CommandSender getSource() {
        return raw.getSource().getBukkitSender();
    }

    public Location getLocation() {
        return BukkitUtil.getLocation(raw.getSource());
    }

    public World getWorld() {
        return BukkitUtil.getWorld(raw.getSource());
    }

    @Override
    public CommandContext<CommandListenerWrapper> unwrap() {
        return raw;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Class<? extends CommandContext<CommandListenerWrapper>> wrappedClass() {
        return (Class) CommandContext.class;
    }
}

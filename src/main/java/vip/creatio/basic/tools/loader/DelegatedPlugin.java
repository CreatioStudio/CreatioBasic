package vip.creatio.basic.tools.loader;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vip.creatio.basic.annotation.ListenerProcessor;
import vip.creatio.basic.annotation.TaskProcessor;
import vip.creatio.basic.cmd.CommandManager;
import vip.creatio.basic.cmd.CommandRegister;
import vip.creatio.basic.packet.ChannelPacketListener;
import vip.creatio.basic.packet.PacketListener;
import vip.creatio.basic.tools.*;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * A delegated plugin class that contains basic plugin services
 */
public abstract class DelegatedPlugin implements PluginInterface {

    protected final AbstractBootstrap bootstrap;

    // Set it to FormatMsgManager in initNmsLoader if you want
    // yml message support.
    /** Custom message sending service */
    protected MsgSender msgSender;

    /** Packet listening service */
    protected PacketListener packetListener;

    /** Task annotation processing service */
    protected GlobalTaskExecutor taskManager;

    /** Listener annotation processing service */
    protected ListenerRegister listenerManager;

    /** Enhanced command registry service */
    protected CommandRegister commandRegister;


    protected DelegatedPlugin(AbstractBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public final void load() {
        onLoad();
        taskManager = new TaskManager(bootstrap);
        listenerManager = new ListenerManager(bootstrap);
        msgSender = new MsgManager();
        packetListener = new ChannelPacketListener(bootstrap.getName() + "_listener");
    }

    protected void onLoad() {}

    @Override
    public final void enable() {
        commandRegister = new CommandManager(bootstrap);
        Bukkit.getPluginManager().registerEvent(ServerLoadEvent.class, new Listener(){},
                EventPriority.NORMAL, (l, e) -> {
            if (ServerLoadEvent.class.isAssignableFrom(e.getClass())) {
                taskManager.onPostWorld();
                onPostWorld();
            }
        }, bootstrap);
        onEnable();
        PacketListener.registerListener(bootstrap, packetListener);
        bootstrap.loader.addProcessor(new ListenerProcessor(listenerManager));
        bootstrap.loader.addProcessor(new TaskProcessor(taskManager));
        bootstrap.loader.processAnnotations();

        taskManager.start();
        taskManager.onLoad();
    }

    protected void onEnable() {}

    @Override
    public final void disable() {
        onDisable();
        taskManager.onUnload();
        PacketListener.unregisterListeners(bootstrap);
    }

    protected void onDisable() {}

    /** Called when server finished loading */
    protected void onPostWorld() {}

    public MsgSender getMsgSender() {
        return msgSender;
    }

    public PacketListener getPacketListener() {
        return packetListener;
    }

    public GlobalTaskExecutor getTaskManager() {
        return taskManager;
    }

    public ListenerRegister getListenerManager() {
        return listenerManager;
    }

    public CommandRegister getCommandRegister() {
        return commandRegister;
    }

    public final File getDataFolder() {
        return bootstrap.getDataFolder();
    }

    public final File getFile() {
        return bootstrap.getFile();
    }

    public final Server getServer() {
        return bootstrap.getServer();
    }

    public void saveResource(@NotNull String resourcePath, boolean replace) {
        bootstrap.saveResource(resourcePath, replace);
    }

    public @Nullable InputStream getResource(@NotNull String filename) {
        return bootstrap.getResource(filename);
    }

    public Logger getLogger() {
        return bootstrap.getLogger();
    }

}

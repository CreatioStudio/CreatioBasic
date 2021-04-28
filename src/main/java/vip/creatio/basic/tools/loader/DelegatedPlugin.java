package vip.creatio.basic.tools.loader;

import vip.creatio.basic.annotation.processor.ListenerProcessor;
import vip.creatio.basic.annotation.processor.TaskProcessor;
import vip.creatio.basic.cmd.CommandManager;
import vip.creatio.basic.cmd.CommandRegister;
import vip.creatio.basic.packet.ChannelPacketListener;
import vip.creatio.basic.packet.PacketListener;
import vip.creatio.basic.tools.*;

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
        commandRegister = new CommandManager(bootstrap);
    }

    protected void onLoad() {}

    @Override
    public final void enable() {
        onEnable();
        PacketListener.registerListener(bootstrap, packetListener);
        bootstrap.loader.addAnnotationProcessor(new ListenerProcessor(listenerManager));
        bootstrap.loader.addAnnotationProcessor(new TaskProcessor(taskManager));
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
}

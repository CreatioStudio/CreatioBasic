package vip.creatio.basic;

import vip.creatio.basic.internal.CLibBasicBootstrap;
import vip.creatio.basic.packet.PacketManager;
import vip.creatio.basic.tools.MsgManager;
import vip.creatio.basic.tools.MsgSender;
import vip.creatio.basic.cmd.TabCompleteHandler;
import vip.creatio.basic.tools.loader.AbstractBootstrap;
import vip.creatio.basic.tools.loader.DelegatedPlugin;

public final class CreatioBasic extends DelegatedPlugin {

    private static CreatioBasic     instance;

    private PacketManager           packetManager;

    protected CreatioBasic(AbstractBootstrap bootstrap) {
        super(bootstrap);
        instance = this;
        this.packetManager = new PacketManager();
    }

    @Override
    public void onEnable() {
        msgSender = new MsgManager("&6&l[&aClib&2Basic&6&l]");

        TabCompleteHandler.test();
    }

    @Override
    public void onDisable() { }

    public static void intern(String msg) {
        instance.msgSender.intern(msg);
    }

    public static void log(String msg) {
        instance.msgSender.log(msg);
    }

    public static MsgSender getSender() {
        return instance.msgSender;
    }

    public PacketManager getPacketManager() {
        return packetManager;
    }

    public static CreatioBasic getInstance() {
        return instance;
    }

    public CLibBasicBootstrap getBootstrap() {
        return (CLibBasicBootstrap) bootstrap;
    }

}

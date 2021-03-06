package vip.creatio.basic.util;

import io.netty.channel.Channel;
import net.minecraft.server.PlayerConnection;
import vip.creatio.basic.packet.Packet;
import org.bukkit.entity.Player;

public final class PlayerUtil {

    private PlayerUtil() {}

    public static void sendPacket(Player plr, Packet<?> packet) {
        getConnection(plr).sendPacket(packet.unwrap());
    }

    public static PlayerConnection getConnection(Player plr) {
        return NMS.toNms(plr).playerConnection;
    }

    public static Channel getChannel(Player plr) {
        return getConnection(plr).networkManager.channel;
    }

}

package vip.creatio.basic.packet.out;

import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.server.ICompletionProvider;
import net.minecraft.server.PacketPlayOutCommands;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import vip.creatio.basic.packet.Packet;

public class CommandsPacket extends Packet<PacketPlayOutCommands> {

    private static final Var<RootCommandNode<?>> ROOT_NODE = Reflection.field(PacketPlayOutCommands.class, 0);

    private final RootCommandNode<?> rootNode;

    CommandsPacket(PacketPlayOutCommands nms) {
        super(nms);
        this.rootNode = ROOT_NODE.get(nms);
    }

    @SuppressWarnings("unchecked")
    public CommandsPacket(RootCommandNode<?> root) {
        super(new PacketPlayOutCommands((RootCommandNode<ICompletionProvider>) root));
        this.rootNode = root;
    }

    public RootCommandNode<?> getRootNode() {
        return rootNode;
    }
}

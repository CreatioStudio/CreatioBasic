package vip.creatio.basic.packet.out;

import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.server.PacketPlayOutTabComplete;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import vip.creatio.basic.packet.Packet;

public class TabCompletePacket extends Packet<PacketPlayOutTabComplete> {

    private static final Var<Integer> TID = Reflection.field(PacketPlayOutTabComplete.class, 0);
    private static final Var<Suggestions> SUG = Reflection.field(PacketPlayOutTabComplete.class, 1);

    private int transactionId;
    private Suggestions suggestions;

    TabCompletePacket(PacketPlayOutTabComplete nms) {
        super(nms);
        this.transactionId = TID.getInt(nms);
        this.suggestions = SUG.get(nms);
    }

    public TabCompletePacket(int transactionId, Suggestions sug) {
        super(new PacketPlayOutTabComplete(transactionId, sug));
        this.transactionId = transactionId;
        this.suggestions = sug;
    }

    public int getTransactionID() {
        return transactionId;
    }

    public Suggestions getSuggestions() {
        return suggestions;
    }

    public void setTransactionID(int transactionId) {
        TID.setInt(original, transactionId);
        this.transactionId = transactionId;
    }

    public void setSuggestions(Suggestions suggestions) {
        SUG.set(original, suggestions);
        this.suggestions = suggestions;
    }
}

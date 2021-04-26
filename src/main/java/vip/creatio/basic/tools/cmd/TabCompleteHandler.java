package vip.creatio.basic.tools.cmd;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.server.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vip.creatio.basic.annotation.Listener;
import vip.creatio.basic.packet.in.OnTabCompletePacket;
import vip.creatio.basic.packet.out.TabCompletePacket;
import vip.creatio.basic.CLibBasic;

import java.util.concurrent.CompletableFuture;

public class TabCompleteHandler {

    public static void test() {
        LiteralArgumentBuilder<CommandListenerWrapper> lab = LiteralArgumentBuilder.<CommandListenerWrapper>literal("fuckershit")
                .then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("set")
                    .then(LiteralArgumentBuilder.literal("day"))
                    .then(LiteralArgumentBuilder.literal("noon"))
                    .then(LiteralArgumentBuilder.literal("night"))
                    .then(LiteralArgumentBuilder.literal("midnight"))
                    .then(RequiredArgumentBuilder.argument("time", IntegerArgumentType.integer()).suggests(c -> {
                        return CompletableFuture.completedFuture(44);
                    })))
                .then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("add")
                        .then(RequiredArgumentBuilder.argument("time", IntegerArgumentType.integer())))
                .then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("query")
                        .then(LiteralArgumentBuilder.literal("daytime"))
                        .then(LiteralArgumentBuilder.literal("gametime"))
                        .then(LiteralArgumentBuilder.literal("day")));

        Commodore comm = new Commodore(CLibBasic.getInstance().getBootstrap());
        comm.register(Bukkit.getPluginCommand("fuckershit"), lab.build());
        System.out.println(comm.getRegisteredNodes());
    }

    @Listener
    static boolean onTabComplete(OnTabCompletePacket pk, Player p) {
//        String cmd = pk.getContent().trim();
//        StringRange sr = new StringRange(cmd.lastIndexOf(' '), cmd.length());
//        Suggestions sug = new Suggestions(sr, Arrays.asList(new Suggestion(sr, "Msg1")));
//        new TabCompletePacket(pk.getTransactionID(), )
        return true;
    }

    @Listener
    static void sendTabComplete(TabCompletePacket pk, Player p) {
    }

}

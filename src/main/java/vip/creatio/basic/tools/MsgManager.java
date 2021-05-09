package vip.creatio.basic.tools;

import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import vip.creatio.basic.packet.out.ChatPacket;
import vip.creatio.basic.packet.out.SetTitlePacket;
import vip.creatio.basic.chat.ChatColor;
import vip.creatio.basic.chat.Component;

import java.util.logging.Level;
import java.util.regex.Pattern;

public class MsgManager implements MsgSender {

    private String hex = "\\{#([0-9a-fA-F])}";
    private Pattern hexPattern = Pattern.compile(hex);

    public MsgManager() {}

    public MsgManager(@NotNull String hexPattern) {
        setHexPattern(hexPattern);
    }

    public final void setHexPattern(@NotNull String hexPattern) {
        this.hex = hexPattern;
        this.hexPattern = Pattern.compile(this.hex);
    }

    @Override
    public void send(@NotNull Player player, Component... components) {
        for (Component c : components) {
            new ChatPacket(replaceChars(c)).send(player);
        }
    }

    @Override
    public void send(@NotNull BlockCommandSender sender, String... msg) {
        for (String s : msg) {
            sender.sendMessage(replaceChars(s));
        }
    }

    @Override
    public void send(@NotNull ConsoleCommandSender sender, String... msg) {
        for (String str : msg) {
            log(str);
        }
    }

    @Override
    public void sendBar(@NotNull Player player, @NotNull Component components) {
        for (Component c : components) {
            new SetTitlePacket(SetTitlePacket.Action.ACTIONBAR, replaceChars(c)).send(player);
        }
    }

    @Override
    public void sendTitle(@NotNull Player player, @NotNull Component components) {
        for (Component c : components) {
            new SetTitlePacket(SetTitlePacket.Action.TITLE, replaceChars(c)).send(player);
        }
    }

    @Override
    public void sendSubTitle(@NotNull Player player, @NotNull Component components) {
        for (Component c : components) {
            new SetTitlePacket(SetTitlePacket.Action.SUBTITLE, replaceChars(c)).send(player);
        }
    }

    @Override
    public void log(Level lvl, String msg) {
        Bukkit.getLogger().log(lvl, replaceChars(msg));
    }

    public String replaceColors(String msg) {
        return hexPattern
                .matcher(msg)
                .replaceAll(r -> ChatColor.hexToColorCode(r.group(1)))
                .replace('&', '§')
                .replace("\\§", "&");
    }

    public String replaceChars(String msg) {
        return replaceColors(msg);
    }

    public Component replaceChars(Component comp) {
        return comp
                .replaceAll(hex, m -> m.replaceAll(r -> ChatColor.hexToColorCode(r.group(1))))
                .replace('&', '§')
                .replace("\\§", "&");
    }

    @Override
    public void debug(String msg) {
        log(Level.CONFIG, msg);
    }

    @Override
    public void intern(String msg) {
        log(Level.WARNING, msg);
    }
}

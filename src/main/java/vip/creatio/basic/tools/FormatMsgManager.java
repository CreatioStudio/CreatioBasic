package vip.creatio.basic.tools;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vip.creatio.basic.chat.Component;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatMsgManager extends MsgManager {

    protected final Pattern vars;
    protected final Map<String, String> replaceMap = new HashMap<>();
    protected final Map<Pattern, String> replaceMapRegex = new HashMap<>();
    protected Configuration language;

    public FormatMsgManager(Configuration language, String varsPattern, String hexPattern) {
        super(hexPattern);
        this.language = language;
        this.vars = Pattern.compile(varsPattern);
    }

    public FormatMsgManager(Configuration language, String varsPattern) {
        super();
        this.language = language;
        this.vars = Pattern.compile(varsPattern);
    }

    public FormatMsgManager(Configuration language) {
        this.language = language;
        this.vars = Pattern.compile("%([0-9]{1,2})%");
    }

    private String[] getString(String path) {
        if (language.isList(path)) {
            List<String> list = language.getStringList(path);
            return list.toArray(new String[0]);
        } else {
            Object obj = language.get(path);
            if (obj == null) return new String[0];
            return new String[]{obj.toString()};
        }
    }

    public void setLanguage(Configuration lang) {
        this.language = lang;
    }

    public void addReplacer(String from, String to) {
        addReplacer(from, to, false);
    }

    public void addReplacer(String from, String to, boolean isRegex) {
        if (isRegex)
            replaceMapRegex.put(Pattern.compile(from), to);
        else
            replaceMap.put(from, to);
    }

    public void addReplacerToPath(String from, String path) {
        addReplacerToPath(from, path, false);
    }

    public void addReplacerToPath(String from, String path, boolean isRegex) {
        if (isRegex)
            replaceMapRegex.put(Pattern.compile(from), getSingle(path));
        else
            replaceMap.put(from, getSingle(path));
    }

    public void sendStatic(@NotNull Level lvl, @NotNull String path, String... vars) {
        for (String s : fromPath(path, vars)) {
            log(lvl, s);
        }
    }

    public void sendStatic(@NotNull String path, String... vars) {
        sendStatic(Level.INFO, path, vars);
    }

    public void sendStatic(@NotNull ConsoleCommandSender sender, String path, String... vars) {
        send(sender, fromPath(path, vars));
    }

    public void sendStatic(@NotNull BlockCommandSender sender, String path, String... vars) {
        send(sender, fromPath(path, vars));
    }

    public void sendStatic(@NotNull CommandSender sender, String path, String... vars) {
        send(sender, fromPath(path, vars));
    }

    public void sendStatic(@NotNull Player player, String path, String... vars) {
        send(player, getList(path, vars));
    }

    public void sendStaticTitle(@NotNull Player player, String path, String... vars) {
        sendTitle(player, fromPath(path, vars)[0]);
    }

    public void sendStaticSubTitle(@NotNull Player player, String path, String... vars) {
        sendSubTitle(player, fromPath(path, vars)[0]);
    }

    public void sendStaticBar(@NotNull Player player, String path, String... vars) {
        sendBar(player, fromPath(path, vars)[0]);
    }


    // sendStatic with custom prefix

    public void sendStatic(@NotNull Map<String, String> customMap, @NotNull Level lvl, @NotNull String path, String... vars) {
        for (String s : fromPath(path, vars)) {
            Bukkit.getLogger().log(lvl, replaceChars(customMap, s));
        }
    }

    public void sendStatic(@NotNull Map<String, String> customMap, @NotNull String path, String... vars) {
        sendStatic(customMap, Level.INFO, path, vars);
    }

    public void sendStatic(@NotNull Map<String, String> customMap, @NotNull ConsoleCommandSender sender, String path, String... vars) {
        sendStatic(customMap, Level.INFO, path, vars);
    }

    public void sendStatic(@NotNull Map<String, String> customMap, @NotNull BlockCommandSender sender, String path, String... vars) {
        for (String s : fromPath(path, vars)) {
            sender.sendMessage(replaceChars(customMap, s));
        }
    }

    public void sendStatic(@NotNull Map<String, String> customMap, @NotNull CommandSender sender, String path, String... vars) {
        if (sender instanceof Player) {
            sendStatic(customMap, (Player) sender, path, vars);
        } else if (sender instanceof ConsoleCommandSender) {
            sendStatic(customMap, (ConsoleCommandSender) sender, path, vars);
        } else if (sender instanceof BlockCommandSender) {
            sendStatic(customMap, (BlockCommandSender) sender, path, vars);
        }
    }

    public void sendStatic(@NotNull Map<String, String> customMap, @NotNull Player player, String path, String... vars) {
        send(player, getList(customMap, path, vars));
    }


    public String[] getList(@NotNull String path, String... vars) {
        return getList(null, path, vars);
    }

    public String[] getList(@Nullable Map<String, String> customMap, @NotNull String path, String... vars) {
        String[] msg = fromPath(path, vars);
        for (int i = 0; i < msg.length; i++) {
            msg[i] = replaceChars(customMap, msg[i]);
        }
        return msg;
    }

    public String getSingle(@NotNull String path, String... vars) {
        return getSingle(null, path, vars);
    }

    public String getSingle(@Nullable Map<String, String> customMap, @NotNull String path, String... vars) {
        String[] msg = fromPath(path, vars);
        if (msg.length > 0) {
            return replaceChars(customMap, msg[0]);
        } else {
            throw new IndexOutOfBoundsException("Empty array list!");
        }
    }

    private String[] fromPath(@NotNull String path, String... vars) {
        String[] msg = getString(path);
        for (int i = 0; i < msg.length; i++) {
            msg[i] = replaceVars(msg[i], vars);
        }
        return msg;
    }

    public String replaceChars(String msg) {
        return replaceChars(null, null, msg);
    }

    public String replaceChars(@Nullable Map<String, String> customMap, String msg) {
        return replaceChars(customMap, null, msg);
    }

    public String replaceChars(@Nullable Map<String, String> customMap, @Nullable Map<Pattern, String> customRegexMap, String msg) {
        Set<String> replaced = new HashSet<>();
        if (customMap != null) for (Map.Entry<String, String> entry : customMap.entrySet()) {
            msg = msg.replace(entry.getKey(), entry.getValue());
            replaced.add(entry.getKey());
        }
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            if (replaced.contains(entry.getKey())) continue;
            msg = msg.replace(entry.getKey(), entry.getValue());
            replaced.add(entry.getKey());
        }

        if (customRegexMap != null) for (Map.Entry<Pattern, String> entry : customRegexMap.entrySet()) {
            msg = entry.getKey().matcher(msg).replaceAll(entry.getValue());
        }
        for (Map.Entry<Pattern, String> entry : replaceMapRegex.entrySet()) {
            msg = entry.getKey().matcher(msg).replaceAll(entry.getValue());
        }

        return super.replaceChars(msg);
    }

    @Override
    public Component replaceChars(Component comp) {
        return replaceChars(null, null, comp);
    }

    public Component replaceChars(@Nullable Map<String, String> customMap, Component comp) {
        return replaceChars(customMap, null, comp);
    }

    public Component replaceChars(@Nullable Map<String, String> customMap, @Nullable Map<Pattern, String> customRegexMap, Component comp) {
        Set<String> replaced = new HashSet<>();
        if (customMap != null) {
            for (Map.Entry<String, String> entry : customMap.entrySet()) {
                comp = comp.replace(entry.getKey(), entry.getValue());
                replaced.add(entry.getKey());
            }
        }
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            if (replaced.contains(entry.getKey())) continue;
            comp = comp.replace(entry.getKey(), entry.getValue());
            replaced.add(entry.getKey());
        }

        if (customRegexMap != null) for (Map.Entry<Pattern, String> entry : customRegexMap.entrySet()) {
            comp = comp.replaceAll(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Pattern, String> entry : replaceMapRegex.entrySet()) {
            comp = comp.replaceAll(entry.getKey(), entry.getValue());
        }

        return super.replaceChars(comp);
    }

    protected String replaceVars(@NotNull String msg, String... vars) {
        Matcher mt = this.vars.matcher(msg);
        while (mt.find()) {
            int i = Integer.parseInt(mt.group(1));
            if (vars.length > i)
                msg = msg.replace(mt.group(0), vars[i]);
        }
        return msg;
    }
}

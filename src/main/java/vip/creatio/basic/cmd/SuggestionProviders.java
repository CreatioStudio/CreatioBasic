package vip.creatio.basic.cmd;

import org.bukkit.entity.Player;
import vip.creatio.common.util.IndexPair;
import vip.creatio.common.util.StringUtil;

import java.util.*;
import java.util.function.Supplier;

public final class SuggestionProviders {

    public static SuggestionProvider of(String... text) {
        List<String> l = Arrays.asList(text);
        return of(() -> l);
    }

    public static SuggestionProvider of(Supplier<Collection<String>> supplier) {
        return (c, b) -> {
            String remaining = b.getRemaining();
            for (String s : supplier.get()) {
                if (s.startsWith(remaining)) b.suggest(s);
            }
            return b.buildFuture();
        };
    }

    public static SuggestionProvider ofMultiple(String... text) {
        List<String> l = Arrays.asList(text);
        return ofMultiple(() -> l);
    }

    public static SuggestionProvider ofMultiple(Supplier<Collection<String>> supplier) {
        return (c, b) -> {
            boolean newLine = b.getRemaining().length() == 0 || b.getRemaining().charAt(b.getRemaining().length() - 1) == ' ';
            List<String> list = new ArrayList<>(supplier.get());
            IndexPair<String>[] split = StringUtil.indexSplit(b.getRemaining());
            if (split.length > 0) {
                IndexPair<String> last = split[split.length - 1];
                if (newLine) {
                    b = b.createOffset(b.getStart() + b.getRemaining().length());
                } else {
                    b = b.createOffset(b.getStart() + last.getIndex());
                    list.removeIf(s -> !s.startsWith(last.getValue()));
                }
            }
            for (String s : list) {
                b.suggest(s);
            }
            return b.buildFuture();
        };
    }

    public static SuggestionProvider ofMultipleNoRepeat(String... text) {
        List<String> l = Arrays.asList(text);
        return ofMultipleNoRepeat(() -> l);
    }

    public static SuggestionProvider ofMultipleNoRepeat(Supplier<Collection<String>> supplier) {
        return (c, b) -> {
            boolean newLine = b.getRemaining().length() == 0 || b.getRemaining().charAt(b.getRemaining().length() - 1) == ' ';
            List<String> list = new ArrayList<>(supplier.get());
            IndexPair<String>[] split = StringUtil.indexSplit(b.getRemaining());
            for (int i = 0, j = newLine ? split.length : split.length - 1; i < j; i++) {
                list.remove(split[i].getValue());
            }
            if (split.length > 0) {
                IndexPair<String> last = split[split.length - 1];
                if (newLine) {
                    b = b.createOffset(b.getStart() + b.getRemaining().length());
                } else {
                    b = b.createOffset(b.getStart() + last.getIndex());
                    list.removeIf(s -> !s.startsWith(last.getValue()));
                }
            }
            for (String s : list) {
                b.suggest(s);
            }
            return b.buildFuture();
        };
    }

    /** Suggests a list of player's name */
    public static SuggestionProvider players(Collection<Player> plr) {
        return (c, b) -> {
            String remaining = b.getRemaining();
            for (Player p : plr) {
                if (p.getName().startsWith(remaining)) b.suggest(p.getName());
            }
            return b.buildFuture();
        };
    }

    private static final SuggestionProvider NIL = (c, b) -> b.buildFuture();

    /** Suggests a null list */
    public static SuggestionProvider nil() {
        return NIL;
    }

}

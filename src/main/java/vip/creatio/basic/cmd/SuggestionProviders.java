package vip.creatio.basic.cmd;

import org.bukkit.entity.Player;

import java.util.Collection;

public final class SuggestionProviders {

    public static SuggestionProvider of(String... text) {
        return (c, b) -> {
            for (String s : text) {
                b.suggest(s);
            }
            return b.buildFuture();
        };
    }

    /** Suggests a list of player's name */
    public static SuggestionProvider players(Collection<Player> plr) {
        return (c, b) -> {
            for (Player p : plr) {
                b.suggest(p.getName());
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

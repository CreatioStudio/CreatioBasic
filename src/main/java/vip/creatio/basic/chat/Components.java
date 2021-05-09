package vip.creatio.basic.chat;

import org.jetbrains.annotations.Nullable;
import vip.creatio.common.util.Mth;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class Components {

    public static Component craftButton(@Nullable String prevCommand, @Nullable String sufCommand) {
        Component comp = Component.create();
        if (prevCommand == null) comp.append(Component.of("  §7<<< "));
        else comp.append(Component.of("  §2§l<<< ").withClickEvent(ClickEvent.runCmd(prevCommand)));
        comp.append(Component.of("§3|"));
        if (sufCommand == null) comp.append(Component.of(" §7>>>"));
        else comp.append(Component.of(" §2§l>>>").withClickEvent(ClickEvent.runCmd(sufCommand)));
        return comp;
    }

    public interface Craftable {
        Component[] craft();
    }

    public static final class List<T> implements Craftable {

        public final Collection<T> src;
        public BiFunction<Integer /* page */ , Boolean /* last */ , Component> titleFormat;
        public BiFunction<Integer /* index */ , T, Component> listFormat;
        public BiFunction<Integer /* page */ , Boolean /* last */ , Component> bottomFormat;
        public Supplier<Component> nullFormat;
        public BiFunction<Integer /* entered */ , Integer /* max */ , Component> outOfBoundFormat;
        public int pageSize = 15;
        public int page = 1;

        public List(Collection<T> source) {
            this.src = source;
        }

        @Override
        public Component[] craft() {
            if (src.size() == 0) return new Component[]{nullFormat.get()};

            int page = this.page - 1;

            int i = pageSize * page;

            if (page < 0) throw new RuntimeException("Page cannot be negative!");

            if (i >= src.size()) return new Component[]{outOfBoundFormat.apply(this.page, Mth.ceilDiv(src.size(), pageSize))};

            Component[] text = new Component[Math.min((src.size() - i), pageSize) + 2];

            int set = 0;

            for (T values : src) {
                set++;
                if (set < i + 1) continue;
                text[set - i] = listFormat.apply(set, values);
                if (set >= i + pageSize) break;
            }

            set -= i;

            text[0] = titleFormat.apply(page, set < 15);
            text[set + 1] = bottomFormat.apply(page, set < 15);
            return text;
        }
    }
    
}

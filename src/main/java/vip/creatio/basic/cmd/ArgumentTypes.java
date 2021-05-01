package vip.creatio.basic.cmd;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.*;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import vip.creatio.basic.math.Vec2;
import vip.creatio.basic.util.BukkitUtil;
import vip.creatio.basic.util.NMS;

import java.util.*;
import java.util.function.Function;

public final class ArgumentTypes {

    public static IntegerArgumentType ofInt() {
        return IntegerArgumentType.integer();
    }

    public static IntegerArgumentType ofInt(int min) {
        return IntegerArgumentType.integer(min);
    }

    public static IntegerArgumentType ofInt(int min, int max) {
        return IntegerArgumentType.integer(min, max);
    }

    public static BoolArgumentType ofBool() {
        return BoolArgumentType.bool();
    }

    public static DoubleArgumentType ofDouble() {
        return DoubleArgumentType.doubleArg();
    }

    public static DoubleArgumentType ofDouble(double min) {
        return DoubleArgumentType.doubleArg(min);
    }

    public static DoubleArgumentType ofDouble(double min, double max) {
        return DoubleArgumentType.doubleArg(min, max);
    }

    public static FloatArgumentType ofFloat() {
        return FloatArgumentType.floatArg();
    }

    public static FloatArgumentType ofFloat(float min) {
        return FloatArgumentType.floatArg(min);
    }

    public static FloatArgumentType ofFloat(float min, float max) {
        return FloatArgumentType.floatArg(min, max);
    }

    public static LongArgumentType ofLong() {
        return LongArgumentType.longArg();
    }

    public static LongArgumentType ofLong(long min) {
        return LongArgumentType.longArg(min);
    }

    public static LongArgumentType ofLong(long min, long max) {
        return LongArgumentType.longArg(min, max);
    }

    /** Single word, etc xxx, xxx_xxx */
    public static StringArgumentType ofWord() {
        return StringArgumentType.word();
    }

    /** String with quote, etc "xxx", "xxx xxx" */
    public static StringArgumentType ofString() {
        return StringArgumentType.string();
    }

    /** String that contains all arguments afterwards */
    public static StringArgumentType ofGreedyString() {
        return StringArgumentType.greedyString();
    }


    private static final Set<Class<?>> WHITELIST = new HashSet<>();
    @SuppressWarnings("rawtypes")
    private static final Map<Class<?>, Function> WRAPPERS = new HashMap<>();
    private static final Map<Class<?>, Class<?>> RAW_TYPES = new HashMap<>();
    static {
        WRAPPERS.put(net.minecraft.server.EntitySelector.class, (Function<net.minecraft.server.EntitySelector, ?>) EntitySelector::new);
        RAW_TYPES.put(EntitySelector.class, net.minecraft.server.EntitySelector.class);

        WRAPPERS.put(IVectorPosition.class, (Function<IVectorPosition, ?>) Coords::new);
        RAW_TYPES.put(Coords.class, IVectorPosition.class);
    }

    public static ArgumentType<EntitySelector> ofEntity() {
        return wrapArgument(ArgumentEntity.a /* entity */ ());
    }

    public static ArgumentType<EntitySelector> ofEntities() {
        return wrapArgument(ArgumentEntity.multipleEntities());
    }

    public static ArgumentType<EntitySelector> ofPlayer() {
        return wrapArgument(ArgumentEntity.c /* player */ ());
    }

    public static ArgumentType<EntitySelector> ofPlayers() {
        return wrapArgument(ArgumentEntity.d /* players */ ());
    }




    public static ArgumentType<Coords> ofRotation() {
        return wrapArgument(new ArgumentRotation());
    }

    public static ArgumentType<Coords> ofVec2(boolean centerCorrect) {
        return wrapArgument(new ArgumentVec2(centerCorrect));
    }

    public static ArgumentType<Coords> ofVec2() {
        return ofVec2(true);
    }

    public static ArgumentType<Coords> ofVec3(boolean centerCorrect) {
        return wrapArgument(new ArgumentVec3(centerCorrect));
    }

    public static ArgumentType<Coords> ofVec3() {
        return ofVec3(true);
    }

    public static class Coords {

        private final IVectorPosition pos;

        private Coords(IVectorPosition pos) {
            this.pos = pos;
        }

        public Vector getPosition(CommandSender sender) {
            return NMS.toBukkit(pos.a /* getPosition */ (NMS.toNms(sender)));
        }

        public Vec2 getRotation(CommandSender sender) {
            return NMS.toWrapper(pos.b /* getRotation */ (NMS.toNms(sender)));
        }

        public Location getLocation(CommandSender sender) {
            Vector vec = getPosition(sender);
            Vec2 rot = getRotation(sender);
            return new Location(BukkitUtil.getWorld(sender),
                    vec.getX(), vec.getY(), vec.getZ(), rot.x, rot.y);
        }

        public boolean isXRelative() {
            return pos.a /* isXRelative */ ();
        }

        public boolean isYRelative() {
            return pos.b /* isYRelative */ ();
        }

        public boolean isZRelative() {
            return pos.c /* isZRelative */ ();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Coords) {
                return pos.equals(((Coords) obj).pos);
            }
            return false;
        }
    }





    public static ArgumentType<Integer> ofTime() {
        return new ArgumentTime();
    }

    public static ArgumentType<UUID> ofUUID() {
        return new ArgumentUUID();
    }




    private static <S, T> ArgumentType<S> wrapArgument(@NotNull ArgumentType<T> raw) {
        return new WrappedArgumentType<>(raw);
    }

    private static class WrappedArgumentType<S, T> implements ArgumentType<S> {

        private final ArgumentType<T> raw;

        private WrappedArgumentType(@NotNull ArgumentType<T> raw) {
            this.raw = raw;
        }

        @Override
        public S parse(StringReader stringReader) throws CommandSyntaxException {
            return wrap(raw.parse(stringReader));
        }

        @SuppressWarnings("unchecked")
        private S wrap(T raw) {
            return (S) WRAPPERS.get(raw.getClass()).apply(raw);
        }
    }

    static ArgumentType<?> unwrap(ArgumentType<?> type) {
        if (type instanceof WrappedArgumentType) {
            return ((WrappedArgumentType<?, ?>) type).raw;
        }
        return type;
    }

    static Class<?> unwrap(Class<?> wrapped) {
        return RAW_TYPES.getOrDefault(wrapped, wrapped);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static Object wrap(Object obj) {
        Class<?> objClass = obj.getClass();
        Function<Object, Object> func = WRAPPERS.get(objClass);
        if (func != null) {
            return func.apply(obj);
        } else {
            if (!WHITELIST.contains(objClass)) {
                for (Map.Entry<Class<?>, Function> entry : WRAPPERS.entrySet()) {
                    if (entry.getKey().isAssignableFrom(objClass)) {
                        WRAPPERS.put(objClass, entry.getValue());
                        return entry.getValue().apply(obj);
                    }
                }
                WHITELIST.add(objClass);
            }
        }
        return obj;
    }
}

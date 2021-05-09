package vip.creatio.basic.cmd;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.*;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.math.Vec2;
import vip.creatio.basic.nbt.CompoundTag;
import vip.creatio.basic.nbt.NBTTag;
import vip.creatio.basic.util.BukkitUtil;
import vip.creatio.basic.util.NMS;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    public static ArgumentType<List<String>> ofMultipleString() {
        return ofMultiple(ofWord());
    }




    /** EntitySelectors, in order to see tab complete suggestions players need permission "minecraft.command.selector" */
    public static ArgumentType<EntitySelector> ofEntity() {
        return wrapArgument(ArgumentEntity.a /* entity */ (), EntitySelector::new);
    }

    public static ArgumentType<EntitySelector> ofEntities() {
        return wrapArgument(ArgumentEntity.multipleEntities(), EntitySelector::new);
    }

    public static ArgumentType<EntitySelector> ofPlayer() {
        return wrapArgument(ArgumentEntity.c /* player */ (), EntitySelector::new);
    }

    public static ArgumentType<EntitySelector> ofPlayers() {
        return wrapArgument(ArgumentEntity.d /* players */ (), EntitySelector::new);
    }




    public static ArgumentType<Coords> ofRotation() {
        return wrapArgument(new ArgumentRotation(), Coords::new);
    }

    public static ArgumentType<Coords> ofVec2(boolean centerCorrect) {
        return wrapArgument(new ArgumentVec2(centerCorrect), Coords::new);
    }

    public static ArgumentType<Coords> ofVec2i() {
        return wrapArgument(new ArgumentVec2I(), Coords::new);
    }

    public static ArgumentType<Coords> ofVec2() {
        return ofVec2(true);
    }

    public static ArgumentType<Coords> ofVec3(boolean centerCorrect) {
        return wrapArgument(new ArgumentVec3(centerCorrect), Coords::new);
    }

    public static ArgumentType<Coords> ofVec3() {
        return ofVec3(true);
    }

    public static final class Coords {

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

    public static ArgumentType<Component> ofComponent() {
        return wrapArgument(ArgumentChatComponent.a /* create */ (), Component::wrap);
    }

    public static ArgumentType<CompoundTag> ofCompoundTag() {
        return wrapArgument(ArgumentNBTTag.a /* create */ (), CompoundTag::new);
    }



    public static ArgumentType<NBTTag> ofNBTTag() {
        return wrapArgument(ArgumentNBTBase.a /* create */ (), NBTTag::wrap);
    }

    public static ArgumentType<NBTPath> ofNBTPath() {
        return wrapArgument(ArgumentNBTKey.a /* create */ (), NBTPath::new);
    }

    public static final class NBTPath {

        private static final Var<String> SOURCE_STRING = Reflection.field(ArgumentNBTKey.h.class, 0);

        private final ArgumentNBTKey.h path;

        private NBTPath(ArgumentNBTKey.h path) {
            this.path = path;
        }

        public List<NBTTag> get(NBTTag root) throws CommandSyntaxException {
            return path.a /* get */ (root.unwrap())
                    .stream()
                    .map((Function<? super NBTBase, NBTTag>) NBTTag::wrap)
                    .collect(Collectors.toList());
        }

        public int countMatching(NBTTag tag) {
            return path.b /* countMatching */ (tag.unwrap());
        }

        public List<NBTTag> getOrCreate(NBTTag root, Supplier<NBTTag> def) throws CommandSyntaxException {
            return path.a /* getOrCreate */ (root.unwrap(), () -> def.get().unwrap())
                    .stream()
                    .map((Function<? super NBTBase, NBTTag>) NBTTag::wrap)
                    .collect(Collectors.toList());
        }

        public int set(NBTTag root, Supplier<NBTTag> setTo) throws CommandSyntaxException {
            return path.b /* set */ (root.unwrap(), () -> setTo.get().unwrap());
        }

        public int remove(NBTTag root) {
            return path.c /* remove */ (root.unwrap());
        }

        public String getString() {
            return SOURCE_STRING.get(path);
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }


    // Custom argument types
    public static ArgumentType<File[]> ofFiles(File parent, FileFilter filter, boolean includeDir) {
        return new FileArgumentType(parent, filter, includeDir);
    }

    public static ArgumentType<File[]> ofFiles(File parent, FileFilter filter) {
        return ofFiles(parent, filter, false);
    }

    public static ArgumentType<File[]> ofFiles(File parent) {
        return new FileArgumentType(parent);
    }

    public static ArgumentType<File[]> ofFile(File parent, FileFilter filter) {
        return new FileArgumentType(parent, filter, false, true);
    }

    public static ArgumentType<File[]> ofFile(File parent) {
        return ofFile(parent, f -> true);
    }

    public static ArgumentType<File[]> ofClassFiles(File root, FileFilter filter) {
        return new ClassPathArgumentType(root, ".class", filter);
    }

    public static ArgumentType<File[]> ofClassFiles(File root) {
        return new ClassPathArgumentType(root);
    }

    public static ArgumentType<File[]> ofJavaFiles(File root, FileFilter filter) {
        return new ClassPathArgumentType(root, ".java", filter);
    }

    public static ArgumentType<File[]> ofJavaFiles(File root) {
        return new ClassPathArgumentType(root, ".java");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ArgumentType<Class<?>[]> ofClasses(Collection<Class<?>> range, boolean selectMultiple, boolean getAssociated) {
        return (ArgumentType) new ClassArgumentType(range, selectMultiple, getAssociated);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ArgumentType<Class<?>[]> ofClasses(Collection<Class<?>> range) {
        return (ArgumentType) new ClassArgumentType(range, true, true);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ArgumentType<Class<?>[]> ofClass(Collection<Class<?>> range) {
        return (ArgumentType) new ClassArgumentType(range, false, false);
    }


    public static <T> ArgumentType<List<T>> ofMultiple(ArgumentType<T> type, int min, int max) {
        return new MultiArgumentType<>(type, min, max);
    }

    public static <T> ArgumentType<List<T>> ofMultiple(ArgumentType<T> type) {
        return new MultiArgumentType<>(type, 1, -1);
    }



    private static <S, T> ArgumentType<S> wrapArgument(@NotNull ArgumentType<T> raw, Function<T, S> convertor) {
        return new WrappedArgumentType<>(raw, convertor);
    }

    @SuppressWarnings("unchecked")
    static final class WrappedArgumentType<S, T> extends ExternArgumentType<S> {

        private final Function<T, S> convertor;

        private WrappedArgumentType(@NotNull ArgumentType<T> raw, Function<T, S> convertor) {
            super(raw);
            this.convertor = convertor;
        }

        @Override
        public S parse(StringReader stringReader) throws CommandSyntaxException {
            return wrap(((ArgumentType<T>) raw).parse(stringReader));
        }

        private S wrap(T raw) {
            return convertor.apply(raw);
        }
    }

    @SuppressWarnings("rawtypes")
    private static final Map<Class<?>, Function> WRAPPERS = new HashMap<>();
    private static final Map<Class<?>, Class<?>> RAW_TYPES = new HashMap<>();
    static {
        WRAPPERS.put(EntitySelector.class, (Function<net.minecraft.server.EntitySelector, ?>) EntitySelector::new);
        RAW_TYPES.put(EntitySelector.class, net.minecraft.server.EntitySelector.class);

        WRAPPERS.put(Coords.class, (Function<IVectorPosition, ?>) Coords::new);
        RAW_TYPES.put(Coords.class, IVectorPosition.class);

        WRAPPERS.put(Component.class, (Function<IChatBaseComponent, ?>) Component::wrap);
        RAW_TYPES.put(Component.class, IChatBaseComponent.class);

        WRAPPERS.put(CompoundTag.class, (Function<NBTTagCompound, ?>) CompoundTag::new);
        RAW_TYPES.put(CompoundTag.class, NBTTagCompound.class);

        WRAPPERS.put(NBTTag.class, (Function<NBTBase, ?>) NBTTag::wrap);
        RAW_TYPES.put(NBTTag.class, NBTBase.class);

        WRAPPERS.put(NBTPath.class, (Function<ArgumentNBTKey.h, ?>) NBTPath::new);
        RAW_TYPES.put(NBTPath.class, ArgumentNBTKey.h.class);
    }

    static ArgumentType<?> unwrap(ArgumentType<?> type) {
        if (type instanceof ExternArgumentType) {
            return ((ExternArgumentType<?>) type).unwrap();
        }
        return type;
    }

    static Object toWrapper(Object obj, Class<?> wrapperClass) {
        return WRAPPERS.getOrDefault(wrapperClass, f -> f).apply(obj);
    }

    static Class<?> getRawType(Class<?> wrapped) {
        return RAW_TYPES.getOrDefault(wrapped, wrapped);
    }

}

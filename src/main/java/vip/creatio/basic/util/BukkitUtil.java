package vip.creatio.basic.util;

import com.google.common.collect.HashBiMap;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import vip.creatio.basic.nbt.CompoundTag;
import vip.creatio.basic.packet.out.CommandsPacket;
import vip.creatio.common.collection.Pair;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.*;
import org.bukkit.*;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import vip.creatio.common.util.ReflectUtil;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class BukkitUtil {

    private BukkitUtil() {}


    // Reflection

    public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(23);
    public static final String NMS_PKG_NAME = "net.minecraft.server." + VERSION;
    public static final String CB_PKG_NAME = "org.bukkit.craftbukkit." + VERSION;


    public static CompoundTag parseNbt(String nbt) {
        try {
            return new CompoundTag(MojangsonParser.parse(nbt));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    // EntityTypes
    static final HashMap<
            Class<? extends org.bukkit.entity.Entity>,
            Pair<Class<? extends CraftEntity>, Class<? extends Entity>>>
            nmsEntityTypeMap = new HashMap<>();
    static final HashBiMap<Class<? extends org.bukkit.entity.Entity>, EntityType>
            entityClassTypeMap = HashBiMap.create();
    static final HashBiMap<
            Class<? extends net.minecraft.server.Entity>,
            EntityTypes<? extends net.minecraft.server.Entity>>
            nmsEntityClassTypeMap = HashBiMap.create();

    static {
        entityMapInit();
    }
    @SuppressWarnings("unchecked")
    // fill all the maps
    private static void entityMapInit() {
        // Get CB Entities
        try {
            String pkgName = CB_PKG_NAME;
            pkgName = pkgName.replace('.', '/');
            Enumeration<URL> e = EntityType.class.getClassLoader().getResources(pkgName);

            for (EntityType t : EntityType.values()) {
                entityClassTypeMap.put(t.getEntityClass(), t);
            }

            for (Field f : EntityTypes.class.getFields()) {
                if (!Modifier.isStatic(f.getModifiers())) continue;
                EntityTypes<?> eValue = (EntityTypes<?>) f.get(null);
                String cls = f.getGenericType().getTypeName();
                cls = cls.substring(cls.indexOf("<") + 1, cls.length() - 1);
                try {
                    Class<?> c = Class.forName(cls);
                    nmsEntityClassTypeMap.put((Class<? extends net.minecraft.server.Entity>) c, eValue);
                } catch (ClassNotFoundException ignored) {}
            }

            while (e.hasMoreElements()) {
                URL next = e.nextElement();
                if (next.getProtocol().equals("jar")) {
                    JarFile jar = ((JarURLConnection) next.openConnection()).getJarFile();
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();

                        if (name.charAt(0) == '/')
                            name = name.substring(1);

                        if (name.startsWith(pkgName + "/entity")) {
                            if (name.endsWith(".class") && !entry.isDirectory()) {
                                try {

                                    Class<?> cbClass = Class.forName(name = name.substring(0, name.length() - 6).replace('/', '.'));

                                    if (org.bukkit.entity.Entity.class.isAssignableFrom(cbClass)) {

                                        Class<?> nmsClass;

                                        // Getting NMS Class:
                                        //  first try to get through it's name, then it's Constructor parameter if failed.
                                        try {
                                            nmsClass = Class.forName(NMS_PKG_NAME + '.' + "Entity" +
                                                    name.substring(name.lastIndexOf('.') + 5 + 1));
                                        } catch (ClassNotFoundException ignored) {
                                            Constructor<? extends org.bukkit.entity.Entity> c;
                                            c = (Constructor<? extends org.bukkit.entity.Entity>)
                                                    cbClass.getDeclaredConstructors()[0];

                                            if (c.getParameterCount() < 2) continue;

                                            nmsClass = c.getParameterTypes()[1];
                                        }

                                        Class<? extends org.bukkit.entity.Entity> bukkitClass =
                                                (Class<? extends org.bukkit.entity.Entity>) cbClass.getInterfaces()[0];

                                        Pair<Class<? extends CraftEntity>, Class<? extends net.minecraft.server.Entity>> pair =
                                                new Pair<>(
                                                        (Class<? extends CraftEntity>) cbClass,
                                                        (Class<? extends net.minecraft.server.Entity>) nmsClass
                                                );

                                        nmsEntityTypeMap.put(bukkitClass, pair);
                                    }
                                } catch (ClassNotFoundException ignored) {}
                            }
                        }
                    }
                }
            }
        } catch (IOException | IllegalAccessException e) {
            System.err.println("Unable to load Craftbukkit Entity classes from Jar!");
            e.printStackTrace();
        }
    }


    // NamespacedKey/MinecraftKey/ResourceLocation

    public static NamespacedKey parseKey(@NotNull String key) {
        return CraftNamespacedKey.fromString(key);
    }

    public static final WorldServer DEFAULT_WORLD = NMS.toNms(Bukkit.getWorlds().get(0));

    // Server

    public static DedicatedServer getServer() {
        return NMS.toNms(Bukkit.getServer());
    }

    public static double[] getTps() {
        return getServer().recentTps;
    }

    /** Send a command map packet to sync command set with player, bypasses PlayerCommandsEvent */
    public static void syncCommand(Player p) {
        getServer().getCommandDispatcher().a(NMS.toNms(p));
        //PlayerUtil.sendPacket(p, new CommandsPacket(node));
    }

    public static void syncCommand() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            syncCommand(p);
        }
    }

    public static World getWorld(CommandSender sender) {
        return getWorld(NMS.toNms(sender));
    }

    public static Location getLocation(CommandSender sender) {
        return getLocation(NMS.toNms(sender));
    }

    public static Location getLocation(CommandListenerWrapper nms) {
        Vec3D vec = nms.getPosition();
        Vec2F rot = nms.i /* getDirection */ ();
        return new Location(getWorld(nms), vec.getX(), vec.getY(), vec.getZ(), rot.i, rot.j);
    }

    public static World getWorld(CommandListenerWrapper nms) {
        return NMS.toBukkit(nms.getWorld());
    }

    public static SimpleCommandMap getCommandMap() {
        return ((CraftServer) Bukkit.getServer()).getCommandMap();
    }

    public static Class<?> getNmsClass(String name) {
        return ReflectUtil.forName(NMS_PKG_NAME + "." + name);
    }

    public static Class<?> getCbClass(String name) {
        return ReflectUtil.forName(CB_PKG_NAME + '.' + name);
    }
}

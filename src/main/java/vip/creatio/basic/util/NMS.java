package vip.creatio.basic.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.server.*;
import net.minecraft.server.Item;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.util.CraftVector;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import vip.creatio.accessor.Func;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import vip.creatio.basic.math.Vec2;
import vip.creatio.basic.packet.VirtualEntity;
import vip.creatio.common.util.SysUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Instance conversion factory
 *
 * Nms <-> Bukkit, CraftBukkit <-> Bukkit, Wrapper <-> Raw
 *
 * Method naming in this class:
 * Nms -> Wrapper       to*WrapperName*()
 * Any -> Nms           toNms()
 * Any -> CraftBukkit   toCB()
 * Any -> Nms(Class)    toNms*TypeName*()
 * Any -> Wrapper       to*WrapperName*TypeName*()
 *
 * NOTE: should not be used by plugin without NmsClassLoader.
 * Even though you don't need to import NMS class when converting
 * objects through the use of this class, but the NMS class name
 * will still appear in the bytecodes of your compiled class files!
 *
 * <-UNSAFE->
 */
public final class NMS {

    //######################### External Utilities #########################//

    public static <T, R> List<R> batch(List<T> list, Function<T, R> function) {
        return SysUtil.batch(list, function);
    }

    public static <T, R> R[] batch(T[] list, Function<T, R> function) {
        return SysUtil.batch(list, function);
    }

    public static <T, R> R nullable(T obj, Function<T, R> function) {
        return SysUtil.nullable(obj, function);
    }

    //######################################################################//


    //############################### Entity ###############################//

    public static net.minecraft.server.Entity toNms(@NotNull Entity entity) {
        if (entity instanceof VirtualEntity) return ((VirtualEntity<?>) entity).toNms();
        return ((CraftEntity) entity).getHandle();
    }

    public static EntityLiving toNms(@NotNull LivingEntity entity) {
        return (EntityLiving) toNms((Entity) entity);
    }

    public static EntityHuman toNms(@NotNull HumanEntity entity) {
        return (EntityHuman) toNms((Entity) entity);
    }

    public static EntityPlayer toNms(@NotNull Player player) {
        return (EntityPlayer) toNms((Entity) player);
    }

    public static Entity toBukkit(net.minecraft.server.Entity entity) {
        return entity.getBukkitEntity();
    }

    public static LivingEntity toBukkit(EntityLiving entity) {
        return (LivingEntity) entity.getBukkitEntity();
    }

    public static Player toBukkit(EntityPlayer player) {
        return (Player) toBukkit((EntityLiving) player);
    }

    //######################################################################//


    //############################# Particles ##############################//

    private static final BiMap<org.bukkit.Particle, MinecraftKey> PARTICLES =
            Reflection.<BiMap<org.bukkit.Particle, MinecraftKey>>field(CraftParticle.class, "particles").get();
    private static final Map<org.bukkit.Particle, org.bukkit.Particle> ALIASES =
            Reflection.<Map<org.bukkit.Particle, org.bukkit.Particle>>field(CraftParticle.class, "aliases").get();

    public static net.minecraft.server.Particle<?> toNms(org.bukkit.Particle particle) {
        org.bukkit.Particle canonical = particle;
        if (ALIASES.containsKey(particle)) {
            canonical = ALIASES.get(particle);
        }

        return IRegistry.PARTICLE_TYPE.get(PARTICLES.get(canonical));
    }

    public static org.bukkit.Particle toBukkit(net.minecraft.server.Particle<?> nms) {
        return CraftParticle.toBukkit(nms);
    }

    public static org.bukkit.Particle toBukkit(ParticleParam nms) {
        return CraftParticle.toBukkit(nms);
    }

    //######################################################################//


    //############################# Utilities ##############################//

    public static NamespacedKey toBukkit(MinecraftKey nms) {
        return CraftNamespacedKey.fromMinecraft(nms);
    }

    public static MinecraftKey toNms(NamespacedKey key) {
        return CraftNamespacedKey.toMinecraft(key);
    }

    private static final BiMap<Difficulty, EnumDifficulty> difficultyMap =
            HashBiMap.create(Arrays.stream(Difficulty.values())
                    .collect(Collectors.toMap(d -> d, d -> EnumDifficulty.valueOf(d.name()))));

    public static EnumDifficulty toNms(Difficulty diff) {
        return difficultyMap.get(diff);
    }

    public static Difficulty toBukkit(EnumDifficulty nms) {
        return difficultyMap.inverse().get(nms);
    }

    public static SoundEffect toNms(Sound sound) {
        return CraftSound.getSoundEffect(sound);
    }

    public static Sound toBukkit(SoundEffect effect) {
        return CraftSound.getBukkit(effect);
    }

    //######################################################################//


    //########################## Data Structures ###########################//

    public static Vec3D toNms(Vector vec) {
        return CraftVector.toNMS(vec);
    }

    public static Vector toBukkit(Vec3D nms) {
        return CraftVector.toBukkit(nms);
    }

    public static Vec2F toNms(Vec2 vec) {
        return vec.unwrap();
    }

    public static Vec2 toWrapper(Vec2F vec) {
        return new Vec2(vec);
    }

    public static AxisAlignedBB toNms(BoundingBox box) {
        return new AxisAlignedBB(box.getMinX(), box.getMinY(), box.getMinZ(),
                box.getMaxX(), box.getMaxY(), box.getMaxZ());
    }

    public static BoundingBox toBukkit(AxisAlignedBB aabb) {
        return new BoundingBox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    //######################################################################//


    //########################## World & Server ############################//

    public static WorldServer toNms(org.bukkit.World world) {
        return ((CraftWorld) world).getHandle();
    }

    public static CraftWorld toBukkit(WorldServer nms) {
        return nms.getWorld();
    }

    public static DedicatedServer toNms(Server server) {
        return ((CraftServer) server).getServer();
    }

    //######################################################################//


    //############################# Command ################################//

    public static CommandListenerWrapper toNms(CommandSender sender) {
        return VanillaCommandWrapper.getListener(sender);
    }

    public static CommandSender toBukkit(CommandListenerWrapper wrapper) {
        return wrapper.getBukkitSender();
    }

    //######################################################################//


    //########################### EntityType ###############################//

    public static Class<? extends net.minecraft.server.Entity>
    toNmsEntityClass(Class<? extends org.bukkit.entity.Entity> bukkitClass) {
        return BukkitUtil.nmsEntityTypeMap.get(bukkitClass).getValue();
    }

    public static Class<? extends net.minecraft.server.Entity>
    toNmsEntityClass(EntityType type) {
        return BukkitUtil.nmsEntityTypeMap.get(type.getEntityClass()).getValue();
    }

    public static Class<? extends CraftEntity>
    toCBEntityClass(Class<? extends org.bukkit.entity.Entity> bukkitClass) {
        return BukkitUtil.nmsEntityTypeMap.get(bukkitClass).getKey();
    }

    public static Class<? extends CraftEntity>
    toCBEntityClass(EntityType type) {
        return BukkitUtil.nmsEntityTypeMap.get(type.getEntityClass()).getKey();
    }

    public static EntityType
    toBukkitEntityType(Class<? extends org.bukkit.entity.Entity> cls) {
        return BukkitUtil.entityClassTypeMap.get(cls);
    }

    @SuppressWarnings("unchecked")
    public static <T extends net.minecraft.server.Entity> EntityTypes<T>
    toNmsEntityType(Class<T> cls) {
        return (EntityTypes<T>) BukkitUtil.nmsEntityClassTypeMap.get(cls);
    }

    public static EntityType
    toBukkit(EntityTypes<?> types) {
        Class<?> cls = BukkitUtil.nmsEntityClassTypeMap.inverse().get(types);
        return BukkitUtil.entityClassTypeMap.get(cls);
    }

    public static EntityTypes<?>
    toNms(EntityType type) {
        return toNmsEntityType(toNmsEntityClass(type));
    }

    //######################################################################//


    //############################### Packet ################################//

    @SuppressWarnings("unchecked")
    public static <T extends Packet<?>> Class<T>
    toNmsPacket(Class<vip.creatio.basic.packet.Packet<T>> wrapped) {
        return (Class<T>) vip.creatio.basic.packet.Packet.getNmsClass(wrapped);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Packet<?>> Class<vip.creatio.basic.packet.Packet<T>>
    toWrappedPacket(Class<T> wrapped) {
        return (Class<vip.creatio.basic.packet.Packet<T>>)
                vip.creatio.basic.packet.Packet.getWrappedClass(wrapped);
    }

    //######################################################################//


    //############################### Block ################################//

    public static IBlockData toNms(BlockData data) {
        return ((CraftBlockData) data).getState();
    }

    public static BlockData toBukkit(IBlockData nms) {
        return CraftBlockData.fromData(nms);
    }

    public static BlockPosition toNmsPos(Block block) {
        return ((CraftBlock) block).getPosition();
    }

    public static BlockPosition toNms(BlockVector vec) {
        return new BlockPosition(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
    }

    public static IBlockData toNms(Block block) {
        return ((CraftBlock) block).getNMS();
    }

    public static Material getMat(net.minecraft.server.Block nms) {
        return CraftMagicNumbers.getMaterial(nms);
    }

    public static BlockFace toBukkit(EnumDirection dir) {
        return CraftBlock.notchToBlockFace(dir);
    }

    public static EnumDirection toNms(BlockFace face) {
        return CraftBlock.blockFaceToNotch(face);
    }

    //######################################################################//


    //################################ Item ################################//

    private static final Func<CraftItemStack> CRAFT_ITEM_STACK = Reflection.constructor(CraftItemStack.class, ItemStack.class);
    private static final Var<ItemStack> ITEM_STACK_HANDLE_VAR = Reflection.field(CraftItemStack.class, "handle");

    public static org.bukkit.inventory.ItemStack toBukkitCopy(@NotNull ItemStack nmsItem) {
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static org.bukkit.inventory.ItemStack toBukkit(@NotNull ItemStack nmsItem) {
        return CRAFT_ITEM_STACK.invoke(nmsItem);
    }

    public static ItemStack toNms(@NotNull org.bukkit.inventory.ItemStack item) {
        return ITEM_STACK_HANDLE_VAR.get(item);
    }

    public static ItemStack toNmsCopy(@NotNull org.bukkit.inventory.ItemStack item) {
        return CraftItemStack.asNMSCopy(item);
    }

    public static Item toNms(@NotNull Material mat) {
        return CraftMagicNumbers.getItem(mat);
    }

    public static Material toBukkit(@NotNull Item nmsMat) {
        return CraftMagicNumbers.getMaterial(nmsMat);
    }

    //######################################################################//

}

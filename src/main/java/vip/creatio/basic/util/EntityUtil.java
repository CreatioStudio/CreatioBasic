package vip.creatio.basic.util;

import vip.creatio.basic.util.damageSource.DmgSource;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.nbt.CompoundTag;
import vip.creatio.common.Mth;
import vip.creatio.accessor.Func;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import net.minecraft.server.*;
import net.minecraft.server.World;
import org.bukkit.Material;
import org.bukkit.*;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;

public final class EntityUtil {

    public static final EnumSet<GameMode> INVULNERABLE_MODE = EnumSet.of(GameMode.CREATIVE, GameMode.SPECTATOR);

    public static boolean isHandRaised(@NotNull LivingEntity entity) {
        return NMS.toNms(entity).isHandRaised();
    }

    public static EnumHand getRaisedHand(@NotNull LivingEntity entity) {
        return EnumHand.convert(NMS.toNms(entity).getRaisedHand());
    }

    public static boolean isBlocking(@NotNull LivingEntity entity) {
        return NMS.toNms(entity).isBlocking();
    }

    public static ItemStack getActiveItem(@NotNull LivingEntity entity) {
        return NMS.toBukkit(NMS.toNms(entity).getActiveItem());
    }

    public static void clearActiveItem(@NotNull LivingEntity entity) {
        NMS.toNms(entity).clearActiveItem();
    }

    public static void setEntityId(Entity entity, int eid) {
        setEntityId(NMS.toNms(entity), eid);
    }

    public static void setEntityId(net.minecraft.server.Entity entity, int eid) {
        entity.e /* setEntityId */ (eid);
    }

    public static void setUniqueId(Entity entity, UUID uuid) {
        setUniqueId(NMS.toNms(entity), uuid);
    }

    public static void setUniqueId(net.minecraft.server.Entity entity, UUID uuid) {
        entity.a_ /* setUniqueId */ (uuid);
    }

    public static void setCustomName(Entity entity, Component name) {
        NMS.toNms(entity).setCustomName(name.unwrap());
    }

    public static @Nullable Component getCustomName(Entity entity) {
        return Component.wrap(NMS.toNms(entity).getCustomName());
    }

    public static Component getDisplayName(Entity entity) {
        return Component.wrap(NMS.toNms(entity).getDisplayName());
    }

    /** Changing this object will not affect entity's data */
    public static CompoundTag getNbtTag(Entity entity) {
        CompoundTag tag = new CompoundTag();
        NMS.toNms(entity).save(tag.unwrap());
        return tag;
    }

    public static void setNbtTag(Entity entity, CompoundTag tag) {
        NMS.toNms(entity).load(tag.unwrap());
    }


    public static void releaseActiveItem(@NotNull LivingEntity entity) {
        ((EntityLiving) entity).releaseActiveItem();
    }

    public static Vector getViewVector(@NotNull Entity entity, float f) {
        net.minecraft.server.Entity nms = NMS.toNms(entity);
        return calcViewVector(nms.g(f) /* getViewPitch */, nms.h(f) /* getViewYaw */);
    }

    static Vector calcViewVector(float f, float f1) {
        float f2 = f * ((float)Math.PI / 180);
        float f3 = -f1 * ((float)Math.PI / 180);
        float f4 = Mth.cos(f3);
        float f5 = Mth.sin(f3);
        float f6 = Mth.cos(f2);
        float f7 = Mth.sin(f2);
        return new Vector(f5 * f6, -f7, f4 * f6);
    }

    private static final Var<Random> RANDOM = Reflection.field(net.minecraft.server.Entity.class, "random");
    public static Random getRandom(@NotNull LivingEntity entity) {
        return RANDOM.get(NMS.toNms(entity));
    }

    public static EntityEffect getArmorBreakEffect(byte slot) {
        switch (slot) {
            case 0:
                return EntityEffect.BREAK_EQUIPMENT_BOOTS;
            case 1:
                return EntityEffect.BREAK_EQUIPMENT_LEGGINGS;
            case 2:
                return EntityEffect.BREAK_EQUIPMENT_CHESTPLATE;
            case 3:
                return EntityEffect.BREAK_EQUIPMENT_HELMET;
            default:
                return EntityEffect.BREAK_EQUIPMENT_MAIN_HAND;
        }
    }

    public static EntityEffect getToolBreakEffect(byte slot) {
        switch (slot) {
            case 0:
                return EntityEffect.BREAK_EQUIPMENT_MAIN_HAND;
            case 1:
                return EntityEffect.BREAK_EQUIPMENT_OFF_HAND;
            default:
                return EntityEffect.BREAK_EQUIPMENT_MAIN_HAND;
        }
    }

    public static void damageArmor(@NotNull LivingEntity entity, float dmg) {
        if (dmg > 0.0F) {
            dmg /= 4.0F;
            if (dmg < 1.0F) {
                dmg = 1.0F;
            }

            if (entity.getEquipment() != null) {
                for (int i = 0, j = entity.getEquipment().getArmorContents().length; i < j; i++) {
                    ItemStack item = entity.getEquipment().getArmorContents()[i];
                    if (item != null && ItemUtil.isArmor(item.getType())) {
                        EntityEffect ee = getArmorBreakEffect((byte) i);
                        ItemUtil.damage(item, (int) dmg, entity, e -> e.playEffect(ee));
                    }
                }
            }
        }
    }

    public static void damageShield(Player player, float f) {
        ItemStack activeItem = getActiveItem(player);
        if (activeItem.getType() == Material.SHIELD) {

            if (f >= 3.0F) {
                int i = 1 + Mth.floor(f);

                EnumHand hand = getRaisedHand(player);
                ItemUtil.damage(activeItem, i, player, p ->
                        p.playEffect(getToolBreakEffect(hand.getId())));

                if (((Damageable) activeItem).getDamage() < 1) {
                    if (hand == EnumHand.MAIN_HAND) {
                        player.getInventory().setItemInMainHand(ItemUtil.NULL);
                    } else {
                        player.getInventory().setItemInOffHand(ItemUtil.NULL);
                    }

                    clearActiveItem(player);
                    player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 0.8F, 0.8F);
                }
            }
        }
    }

    public static void shieldBlock(LivingEntity self, LivingEntity entity) {
        Vector vec = entity.getLocation().subtract(self.getLocation()).toVector();
        NMS.toNms(self).a(0.5F, vec.getX(), vec.getZ());
    }

    public static void resetAttackCooldown(Player player) {
        ((EntityHuman) NMS.toNms(player)).resetAttackCooldown();
    }

    public static void knockback(LivingEntity entity, float k, double xMot, double yMot) {
        NMS.toNms(entity).a /*knockback*/ (k, xMot, yMot);
    }

    public static void wakeup(LivingEntity entity) {
        NMS.toNms(entity).entityWakeup();
    }

    public static float getNoActionTime(LivingEntity entity) {
        return NMS.toNms(entity).av;
    }

    public static void setNoActionTime(LivingEntity entity, float time) {
        NMS.toNms(entity).av = time;
    }

    public static void setHurtTicks(LivingEntity entity, int ticks) {
        NMS.toNms(entity).hurtTicks = ticks;
    }

    public static LivingEntity getLastDamager(LivingEntity entity) {
        net.minecraft.server.EntityLiving e = NMS.toNms(entity).getLastDamager();
        return e == null ? null : NMS.toBukkit(e);
    }

    public static void setLastDamager(LivingEntity entity, LivingEntity damager) {
        NMS.toNms(entity).setLastDamager(NMS.toNms(damager));
    }

    private static final Var<Integer> LAST_DMG_BY_PLAYER = Reflection.field(EntityLiving.class, "lastDamageByPlayerTime");
    private static int getLastDamageByPlayerTime(LivingEntity entity) {
        return LAST_DMG_BY_PLAYER.getInt(NMS.toNms(entity));
    }

    private static void setLastDamageByPlayerTime(LivingEntity entity, int tick) {
        LAST_DMG_BY_PLAYER.setInt(NMS.toNms(entity), tick);
    }

    public static void setKiller(LivingEntity entity, Player killer) {
        NMS.toNms(entity).killer = NMS.toNms(killer);
    }

    /** Signals that entity velocity should be changed */
    private static final Func<Void> VELOCITY_CHANGED = Reflection.method(EntityLiving.class, "velocityChanged");
    public static void velocityChanged(LivingEntity entity) {
        VELOCITY_CHANGED.invoke(NMS.toNms(entity));
    }

    public static float getYaw(Entity entity) {
        return NMS.toNms(entity).yaw;
    }

    public static void setYaw(Entity entity, float yaw) {
        NMS.toNms(entity).yaw = yaw;
    }

    public static float getPitch(Entity entity) {
        return NMS.toNms(entity).pitch;
    }

    public static void setPitch(Entity entity, float pitch) {
        NMS.toNms(entity).pitch = pitch;
    }

    public static void setMot(Entity entity, double x, double y, double z) {
        NMS.toNms(entity).setMot(x, y, z);
    }

    public static void setMot(Entity entity, Vector mot) {
        NMS.toNms(entity).setMot(NMS.toNms(mot));
    }

    public static void setLoc(Entity entity, double x, double y, double z) {
        net.minecraft.server.Entity e = NMS.toNms(entity);
        NMS.toNms(entity).setPositionRotation(x, y, z, e.yaw, e.pitch);
    }

    public static void setLoc(Entity entity, Vector vec) {
        setLoc(entity, vec.getX(), vec.getY(), vec.getZ());
    }

    public static void addLoc(Entity entity, double dx, double dy, double dz) {
        NMS.toNms(entity).getPositionVector().add(dx, dy, dz);
    }

    public static void addLoc(Entity entity, Vector vec) {
        net.minecraft.server.Entity e = NMS.toNms(entity);
        NMS.toNms(entity).setPositionRotation(e.locX() + vec.getX(), e.locY() + vec.getY(), e.locZ() + vec.getZ(),
                e.yaw, e.pitch);
    }

    /** Will not change entity's world */
    public static void setLoc(Entity entity, Location loc) {
        setLoc(entity, loc.getX(), loc.getY(), loc.getZ());
    }

    public static void removeAllEffects(LivingEntity entity) {
        NMS.toNms(entity).removeAllEffects();
    }

    private static final Func<SoundEffect> GET_SOUND_DEATH = Reflection.method(EntityLiving.class, "getSoundDeath");
    public static @Nullable Sound getDeathSound(LivingEntity entity) {
        SoundEffect eff = GET_SOUND_DEATH.invoke(NMS.toNms(entity));
        return eff == null ? null : NMS.toBukkit(eff);
    }

    private static final Func<SoundEffect> GET_SOUND_HURT = Reflection.method(EntityLiving.class, "getSoundHurt", DamageSource.class);
    public static @Nullable Sound getHurtSound(LivingEntity entity, DmgSource source) {
        SoundEffect eff = GET_SOUND_HURT.invoke(NMS.toNms(entity), source.unwrap());
        return eff == null ? null : NMS.toBukkit(eff);
    }

    private static final Func<SoundEffect> GET_SOUND_FALL = Reflection.method(EntityLiving.class, "getSoundFall", int.class);
    public static @Nullable Sound getFallSound(LivingEntity entity, int height) {
        SoundEffect eff = GET_SOUND_FALL.invoke(NMS.toNms(entity), height);
        return eff == null ? null : NMS.toBukkit(eff);
    }

    private static final Func<Float> GET_SOUND_VOLUME = Reflection.method(EntityLiving.class, "getSoundVolume");
    public static float getSoundVolume(LivingEntity entity) {
        return GET_SOUND_VOLUME.invoke(NMS.toNms(entity));
    }

    private static final Func<Float> GET_SOUND_PITCH = Reflection.method(EntityLiving.class, "dH");
    public static float getSoundPitch(LivingEntity entity) {
        return GET_SOUND_PITCH.invoke(NMS.toNms(entity));
    }

    public static void playHurtSound(LivingEntity entity) {
        Sound s = getHurtSound(entity, DmgSource.GENERIC);

        if (s != null) {
            entity.getWorld().playSound(entity.getLocation(), s, getSoundVolume(entity), getSoundPitch(entity));
        }
    }

    public static boolean checkTotemDeathProtection(LivingEntity entity) {
        EntityEquipment equip = entity.getEquipment();
        ItemStack item = equip == null ? null : equip.getItemInMainHand();
        if (item != null) {
            if (item.getType() != Material.TOTEM_OF_UNDYING) {
                item = null;
            } else {
                item = equip.getItemInOffHand();
                if (item.getType() != Material.TOTEM_OF_UNDYING) {
                    item = null;
                }
            }
        }

        EntityResurrectEvent event = new EntityResurrectEvent(entity);
        event.setCancelled(item == null);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            assert item != null;
            if (item.getAmount() > 0) {
                item.setAmount(item.getAmount() - 1);
            }
            if (entity instanceof Player) {
                ((Player) entity).incrementStatistic(Statistic.USE_ITEM, Material.TOTEM_OF_UNDYING);
            }

            entity.setHealth(1.0F);
            removeAllEffects(entity);
            entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1));
            entity.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
            entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0));

            entity.playEffect(EntityEffect.TOTEM_RESURRECT);
        }

        return !event.isCancelled();
    }

    /** Damage entity without damage modifier */
    public static boolean handleEntityDamage(LivingEntity self,
                                             Entity attacker,
                                             float amount,
                                             int noDamageTicks,
                                             int hurtTicks,
                                             boolean blockedByShield) {
        if (self.isDead() || self.getHealth() <= 0.0F) {
            return false;
        } else {
            if (self.isSleeping()) {
                wakeup(self);
            }

            setNoActionTime(self, 1.5F);
            boolean damageCaused = true;

            if ((float) self.getNoDamageTicks() > (float) self.getMaximumNoDamageTicks() / 2.0F) { // CraftBukkit - restore use of maxNoDamageTicks
                if (amount <= self.getLastDamage()) {
                    NMS.toNms(self).forceExplosionKnockback = true; // CraftBukkit - SPIGOT-949 - for vanilla consistency, cooldown does not prevent explosion knockback
                    return false;
                }

                self.setLastDamage(amount);
                damageCaused = false;
            } else {
                self.setLastDamage(amount);
                self.setNoDamageTicks(noDamageTicks); // CraftBukkit - restore use of maxNoDamageTicks
                setHurtTicks(self, hurtTicks);
            }

            if (self instanceof Animals) {
                ((Animals) self).setLoveModeTicks(0);
                if (self instanceof Tameable) {
                    ((EntityTameableAnimal) NMS.toNms(self)).setWillSit(false);
                }
            }

            NMS.toNms(self).ap = 0.0F;

            if (attacker != null) {
                if (attacker instanceof LivingEntity)
                    setLastDamager(self, (LivingEntity) attacker);

                if (attacker instanceof HumanEntity) {
                    setLastDamageByPlayerTime(self, 100);
                    setKiller(self, (Player) attacker);
                } else if (attacker instanceof Wolf) {
                    Wolf wolf = (Wolf) attacker;

                    if (wolf.isTamed()) {
                        setLastDamageByPlayerTime(self, 100);
                        Player owner = wolf.getOwner() == null ? null : Bukkit.getPlayer(wolf.getOwner().getUniqueId());

                        setKiller(self, owner);
                    }
                }
            }

            if (damageCaused) {
                if (blockedByShield) {
                    self.playEffect(EntityEffect.SHIELD_BLOCK);
                }

                if (!blockedByShield || amount > 0.0F) {
                    velocityChanged(self);
                }

                if (attacker != null) {
                    Vector vec = attacker.getLocation().subtract(self.getLocation()).toVector();
                    double d0 = vec.getX();

                    double d1 = vec.getZ();

                    for (; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                        d0 = (Math.random() - Math.random()) * 0.01D;
                    }

                    NMS.toNms(self).ap = (float) (Math.toDegrees(Mth.atan2(d1, d0)) - getYaw(self));
                    knockback(self, 0.4F, d0, d1);
                } else {
                    NMS.toNms(self).ap = (float) ((int) (Math.random() * 2.0D) * 180);
                }
            }

            if (self.getHealth() <= 0F) {
                if (!checkTotemDeathProtection(self)) {
                    Sound deathEffect = getDeathSound(self);

                    if (damageCaused && deathEffect != null) {
                        self.getWorld().playSound(
                                self.getLocation(),
                                deathEffect,
                                getSoundVolume(self),
                                getSoundPitch(self));
                    }

                    self.remove();
                }
            } else if (damageCaused) {
                playHurtSound(self);
            }

            return !blockedByShield || amount > 0.0F;
        }
    }

    public static int getHighestEnchantmentLevel(Enchantment ench, LivingEntity entity) {
        EntityEquipment eq = entity.getEquipment();
        if (eq != null) {
            int v = (eq.getItemInMainHand().getEnchantmentLevel(ench));
            int k = (eq.getItemInOffHand().getEnchantmentLevel(ench));
            return Math.max(k, v);
        }
        return 0;
    }

    public static double getAttributeValue(Attribute att, LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(att);
        return attribute == null ? 0.0D : attribute.getValue();
    }

    public static int getPotionEffect(PotionEffectType type, LivingEntity entity) {
        PotionEffect effect = entity.getPotionEffect(type);
        return effect == null ? 0 : effect.getAmplifier();
    }

    public static int getKnockbackBonus(LivingEntity entity) {
        return EnchantmentManager.b /* knockbackBonus */ (NMS.toNms(entity));
    }

    @Nullable
    public static <T extends net.minecraft.server.Entity> T create(World world, EntityTypes<T> types) {
        return types.a(world);
    }

    @Nullable
    public static <T extends net.minecraft.server.Entity> T create(org.bukkit.World world, EntityTypes<T> types) {
        return types.a(NMS.toNms(world));
    }

    public static Entity create(org.bukkit.World world, EntityType type) {
        return CraftEntity.getEntity((CraftServer) Bukkit.getServer(),
                create(world, NMS.toNms(type)));
    }

    public static @Nullable Entity create(org.bukkit.World world,
                                          @Nullable CompoundTag tag,
                                          @Nullable Component name,
                                          Location loc,
                                          EntityType type) {
        net.minecraft.server.Entity nms = NMS.toNms(type).createCreature(
                NMS.toNms(world),
                tag == null ? null : new CompoundTag().put("EntityTag", new CompoundTag(tag)).unwrap(),
                name == null ? null : name.unwrap(),
                null,
                NMS.toNmsPos(loc.getBlock()),
                EnumMobSpawn.COMMAND,
                false,
                false);
        if (nms == null) return null;
        nms.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        return CraftEntity.getEntity((CraftServer) Bukkit.getServer(), nms);
    }

    public static @Nullable Entity spawn(org.bukkit.World world,
                                         @Nullable CompoundTag tag,
                                         @Nullable Component name,
                                         Location loc,
                                         EntityType type) {
        Entity e = create(world, tag, name, loc, type);

        if (e != null) {
            net.minecraft.server.Entity nms = NMS.toNms(e);
            NMS.toNms(world).addAllEntities(nms, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return !nms.dead ? e : null;
        }

        return e;
    }


    public static double getReachDistance(Player p) {
        return 5D;
    }

}

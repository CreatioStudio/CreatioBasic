package vip.creatio.basic.packet.particle;

import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import vip.creatio.basic.util.NMS;

public class ParticleParamItem extends ParticleParam {

    private static final Var<net.minecraft.server.ItemStack> ITEMSTACK = Reflection.field(net.minecraft.server.ParticleParamItem.class, 1);

    private final ItemStack item;

    ParticleParamItem(net.minecraft.server.ParticleParamItem nms) {
        super(nms, NMS.toBukkit(nms.getParticle()));
        this.item = NMS.toBukkit(ITEMSTACK.get(nms));
    }

    @SuppressWarnings("unchecked")
    public ParticleParamItem(Particle particle, ItemStack item) {
        super(new net.minecraft.server.ParticleParamItem(
                (net.minecraft.server.Particle<net.minecraft.server.ParticleParamItem>) NMS.toNms(particle),
                NMS.toNms(item)), particle);
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public String toString() {
        return "ParticleParamItem{particle=" + particleType.name() + ",item=" + item.toString() + '}';
    }
}

package vip.creatio.basic.packet.particle;

import vip.creatio.basic.util.BukkitUtil;
import org.bukkit.Particle;

public class ParticleType extends ParticleParam {

    ParticleType(net.minecraft.server.ParticleType nms) {
        super(nms, BukkitUtil.toBukkit((net.minecraft.server.Particle<?>) nms));
    }


    public ParticleType(Particle particleType) {
        super((net.minecraft.server.ParticleParam) BukkitUtil.toNms(particleType), particleType);
    }
}

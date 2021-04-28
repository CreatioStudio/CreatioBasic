package vip.creatio.basic.packet.particle;

import org.bukkit.Particle;
import vip.creatio.basic.util.NMS;

public class ParticleType extends ParticleParam {

    ParticleType(net.minecraft.server.ParticleType nms) {
        super(nms, NMS.toBukkit((net.minecraft.server.Particle<?>) nms));
    }


    public ParticleType(Particle particleType) {
        super((net.minecraft.server.ParticleParam) NMS.toNms(particleType), particleType);
    }
}

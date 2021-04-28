package vip.creatio.basic.packet.particle;

import vip.creatio.basic.tools.Wrapper;
import org.bukkit.Particle;
import vip.creatio.basic.util.NMS;

public abstract class ParticleParam implements Wrapper<net.minecraft.server.ParticleParam> {

    protected final net.minecraft.server.ParticleParam original;
    protected final Particle particleType;

    protected ParticleParam(net.minecraft.server.ParticleParam original, Particle particleType) {
        this.original = original;
        this.particleType = particleType;
    }

    public Particle getParticle() {
        return particleType;
    }

    public static ParticleParam wrap(net.minecraft.server.ParticleParam nms) {
        if (nms instanceof net.minecraft.server.ParticleType) {
            return new ParticleType(NMS.toBukkit(nms));
        } else if (nms instanceof net.minecraft.server.ParticleParamBlock) {
            return new ParticleParamBlock((net.minecraft.server.ParticleParamBlock) nms);
        } else if (nms instanceof net.minecraft.server.ParticleParamItem) {
            return new ParticleParamItem((net.minecraft.server.ParticleParamItem) nms);
        } else if (nms instanceof net.minecraft.server.ParticleParamRedstone) {
            return new ParticleParamRedstone((net.minecraft.server.ParticleParamRedstone) nms);
        }
        throw new RuntimeException("Unknown subtype of ParticleParam: " + nms.getClass().getName());
    }

    @Override
    public net.minecraft.server.ParticleParam unwrap() {
        return original;
    }

    @Override
    public Class<? extends net.minecraft.server.ParticleParam> wrappedClass() {
        return original.getClass();
    }
}

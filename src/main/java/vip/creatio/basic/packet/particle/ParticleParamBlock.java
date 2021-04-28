package vip.creatio.basic.packet.particle;

import net.minecraft.server.IBlockData;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import vip.creatio.basic.util.NMS;

public class ParticleParamBlock extends ParticleParam {

    private static final Var<IBlockData> DATA = Reflection.field(net.minecraft.server.ParticleParamBlock.class, 1);

    private final BlockData blockData;

    ParticleParamBlock(net.minecraft.server.ParticleParamBlock nms) {
        super(nms, NMS.toBukkit(nms.getParticle()));
        this.blockData = NMS.toBukkit(DATA.get(nms));
    }

    @SuppressWarnings("unchecked")
    public ParticleParamBlock(Particle particle, BlockData data) {
        super(new net.minecraft.server.ParticleParamBlock(
                (net.minecraft.server.Particle<net.minecraft.server.ParticleParamBlock>) NMS.toNms(particle),
                NMS.toNms(data)),
                particle);
        this.blockData = data;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    @Override
    public String toString() {
        return "ParticleParamBlock{particle=" + particleType.name() + ",data=" + blockData.getAsString() + '}';
    }
}

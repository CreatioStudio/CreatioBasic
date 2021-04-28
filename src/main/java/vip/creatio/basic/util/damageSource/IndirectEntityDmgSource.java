package vip.creatio.basic.util.damageSource;

import net.minecraft.server.EntityDamageSourceIndirect;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import vip.creatio.basic.util.NMS;

public class IndirectEntityDmgSource extends EntityDmgSource {

    IndirectEntityDmgSource(EntityDamageSourceIndirect source) {
        super(source);
    }

    public IndirectEntityDmgSource(String msgId, Entity entity, @Nullable Entity owner) {
        super(new EntityDamageSourceIndirect(msgId, NMS.toNms(entity),
                owner == null ? null : NMS.toNms(owner)));
    }
}

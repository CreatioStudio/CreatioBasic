package vip.creatio.basic.util.damageSource;

import net.minecraft.server.EntityDamageSource;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import vip.creatio.basic.util.NMS;

public class EntityDmgSource extends DmgSource {

    EntityDmgSource(EntityDamageSource source) {
        super(source);
        net.minecraft.server.Entity e = source.getEntity();
    }

    public EntityDmgSource(String msgId, @Nullable Entity entity) {
        super(new EntityDamageSource(msgId, entity == null ? null : NMS.toNms(entity)));
    }

    public boolean isThorns() {
        return unwrap().y /* isThorns */ ();
    }

    @Override
    public EntityDamageSource unwrap() {
        return (EntityDamageSource) wrapper;
    }
}

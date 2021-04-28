package vip.creatio.basic.math;

import net.minecraft.server.Vec2F;
import vip.creatio.basic.tools.ElasticWrapper;
import vip.creatio.common.annotation.Immutable;

@Immutable
public class Vec2 implements ElasticWrapper<Vec2F> {

    public final float x;
    public final float y;

    public Vec2(Vec2F nms) {
        this.x = nms.i /* x */ ;
        this.y = nms.j /* y */ ;
    }

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object o) {
        if (o instanceof Vec2) {
            Vec2 another = (Vec2) o;
            return x == another.x && y == another.y;
        }
        return false;
    }

    @Override
    public Vec2F unwrap() {
        return new Vec2F(x, y);
    }

    @Override
    public Class<? extends Vec2F> wrappedClass() {
        return Vec2F.class;
    }
}

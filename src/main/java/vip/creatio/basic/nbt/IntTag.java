package vip.creatio.basic.nbt;

import net.minecraft.server.NBTTagInt;

public class IntTag extends NumbericTag<NBTTagInt> {

    public IntTag(NBTTagInt number) {
        super(number);
    }

    public static IntTag valueOf(int value) {
        return new IntTag(NBTTagInt.a(value));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof IntTag) {
            return number.equals(((IntTag) o).number);
        }
        return false;
    }

    @Override
    public NBTType getType() {
        return NBTType.INT;
    }

    @Override
    public Class<? extends NBTTagInt> wrappedClass() {
        return NBTTagInt.class;
    }
}

package vip.creatio.basic;

public final class SharedConstants {

    public static final String BASIC_PKG = "vip.creatio.basic";
    public static final String ACCESSOR_PKG = "vip.creatio.accessor";
    public static final String COMMON_PKG = "vip.creatio.common";
    public static final String ACCESSOR_GLOBAL_PKG = "vip.creatio.accessor.global";

    public static final String PACKET_IN_PKG = BASIC_PKG + ".packet.in";
    public static final String PACKET_OUT_PKG = BASIC_PKG + ".packet.out";
    public static final String MAIN_DELEGATE = BASIC_PKG + ".CreatioBasic";

    /** @see vip.creatio.common.util.ReflectUtil#CLASSNAME_ILLEGAL_CHARS */
    public static final String CLASSFILE_ILLEGAL_CHARS = " '[]{}()+=;,`~!@#%^&";    // without '*' and '-' for internal purpose

}

package vip.creatio.basic.tools.loader;

public interface PluginInterface {

    default void enable() {}

    default void disable() {}

    default void load() {}

}

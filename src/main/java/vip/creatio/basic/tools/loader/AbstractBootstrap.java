package vip.creatio.basic.tools.loader;

import org.bukkit.plugin.java.JavaPlugin;
import vip.creatio.common.ReflectUtil;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * A bootstrap class for plugin that requires custom class loader
 */
public abstract class AbstractBootstrap extends JavaPlugin {

    protected final NmsClassLoader loader;
    protected final PluginInterface delegate;

    protected AbstractBootstrap(NmsClassLoader loader, String mainClass) {
        this.loader = loader;
        init();
        this.delegate = getDelegate(mainClass);
    }

    protected AbstractBootstrap(String mainClass) {
        this.loader = new NmsClassLoader(getClass(), getClassLoader());
        init();
        this.delegate = getDelegate(mainClass);
    }

    @SuppressWarnings("unchecked")
    private PluginInterface getDelegate(String className) {
        Class<?> c = loader.getLoadedClasses().get(className);

        Constructor<PluginInterface> con = null;
        for (Constructor<?> constructor : c.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 1
                    && AbstractBootstrap.class.isAssignableFrom(constructor.getParameterTypes()[0])) {
                con = (Constructor<PluginInterface>) constructor;
            }
        }
        if (con == null) throw new RuntimeException("Delegated plugin should have a constructor with only parameter of AbstractBootstrap");
        return ReflectUtil.newInstance(con, this);
    }

    /** Mirror method of ReflectiveClassLoader::getClassIn, can only get classes loaded by NmsClassLoader */
    public final List<Class<?>> getClassIn(String pkg) {
        return loader.getClassIn(pkg);
    }

    /** Mirror method of ReflectiveClassLoader::getClassUnder, can only get classes loaded by NmsClassLoader */
    public final List<Class<?>> getClassUnder(String pkg) {
        return loader.getClassUnder(pkg);
    }

    protected void init() {
        onInit();
        loader.loadClasses();
    }

    protected void onInit() {}

    @Override
    public void onEnable() {
        delegate.enable();
    }

    @Override
    public void onDisable() {
        delegate.disable();
        loader.unloadClasses();
        loader.close();
    }

    @Override
    public void onLoad() {
        delegate.load();
    }
}

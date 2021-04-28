package vip.creatio.basic.internal;

import vip.creatio.basic.tools.loader.AbstractBootstrap;
import vip.creatio.basic.tools.loader.NmsClassLoader;
import vip.creatio.basic.tools.loader.PluginInterface;

public class CLibBasicBootstrap extends AbstractBootstrap {

    static PluginInterface clibBasic;

    public CLibBasicBootstrap() {
        super(new NmsClassLoader(CLibBasicBootstrap.class), "vip.creatio.basic.CLibBasic");
        if (clibBasic != null) throw new IllegalCallerException("Bootstrap constructor cannot be called twice!");
        clibBasic = delegate;
    }

    @Override
    protected void onInit() {
        loader.addIncludePath("vip.creatio.basic");
        loader.addIncludePath("vip.creatio.accessor");
        loader.addGlobalPath("vip.creatio.accessor.global");
    }

}

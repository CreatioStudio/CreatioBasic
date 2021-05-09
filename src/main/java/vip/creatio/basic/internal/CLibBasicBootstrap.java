package vip.creatio.basic.internal;

import vip.creatio.basic.SharedConstants;
import vip.creatio.basic.tools.loader.AbstractBootstrap;
import vip.creatio.basic.tools.loader.NmsClassLoader;
import vip.creatio.basic.tools.loader.PluginInterface;

public class CLibBasicBootstrap extends AbstractBootstrap {

    static PluginInterface clibBasic;

    public CLibBasicBootstrap() {
        super(new NmsClassLoader(CLibBasicBootstrap.class), SharedConstants.MAIN_DELEGATE);
        if (clibBasic != null) throw new IllegalCallerException("Bootstrap constructor cannot be called twice!");
        clibBasic = delegate;
    }

    @Override
    protected void onInit() {
        loader.addIncludePath(SharedConstants.BASIC_PKG);
    }

}

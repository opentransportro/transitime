package org.transitclock;

import lombok.Getter;

@Getter
public final class ApplicationContext {
    private static ApplicationContext defaultContext;
    private final ModuleRegistry moduleRegistry;
    private final SingletonRegistry singletonRegistry;

    public static synchronized ApplicationContext getDefaultContext() {
        return defaultContext;
    }

    public static synchronized ApplicationContext createDefaultContext(String agencyId) {
        if (defaultContext == null) {
            defaultContext = new ApplicationContext(agencyId);
        }
        return defaultContext;
    }

    private ApplicationContext(String agencyId) {
        this.moduleRegistry = new ModuleRegistry(agencyId);
        this.singletonRegistry = new SingletonRegistry();
    }

}

package org.transitclock;

import lombok.Getter;
import org.transitclock.config.data.AgencyConfig;

@Getter
public final class ApplicationContext {
    private static ApplicationContext defaultContext;
    private final ModuleRegistry moduleRegistry;
    private final SingletonRegistry singletonRegistry;

    public static synchronized ApplicationContext defaultContext() {
        if (defaultContext == null) {
            return createDefaultContext(AgencyConfig.getAgencyId());
        }
        return defaultContext;
    }

    public static synchronized ApplicationContext createDefaultContext(String agencyId) {
        if (defaultContext == null) {
            defaultContext = new ApplicationContext(agencyId);
        }
        return defaultContext;
    }

    public static ModuleRegistry moduleRegistry() {
        return defaultContext().moduleRegistry;
    }

    public static SingletonRegistry singletonRegistry() {
        return defaultContext().singletonRegistry;
    }

    public static <T> T singleton(Class<T> clazz) {
        return defaultContext().singletonRegistry.get(clazz);
    }

    public static <T> T registerSingleton(T clazz) {
        return defaultContext().singletonRegistry.register(clazz);
    }

    public static <T extends Module> T module(Class<T> clazz) {
        return defaultContext().moduleRegistry.get(clazz);
    }

    private ApplicationContext(String agencyId) {
        this.moduleRegistry = new ModuleRegistry(agencyId);
        this.singletonRegistry = new SingletonRegistry();
    }

}

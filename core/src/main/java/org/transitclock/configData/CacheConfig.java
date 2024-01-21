package org.transitclock.configData;

import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;

public class CacheConfig {
    private static final StringConfigValue memcachedHost = new StringConfigValue(
            "transitclock.cache.memcached.host",
            "127.0.0.1",
            "Specifies the host machine that memcache is running on.");

    private static final IntegerConfigValue memcachedPort = new IntegerConfigValue(
            "transitclock.cache.memcached.port", 11211, "Specifies the port that memcache is running on.");

    public static String getMemcachedHost() {
        return memcachedHost.getValue();
    }

    public static Integer getMemcachedPort() {
        return memcachedPort.getValue();
    }
}

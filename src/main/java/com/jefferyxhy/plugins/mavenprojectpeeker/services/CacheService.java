package com.jefferyxhy.plugins.mavenprojectpeeker.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.ExpiryPolicy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static com.jefferyxhy.plugins.mavenprojectpeeker.services.PluginDirectoryService.createFile;

@Service
public final class CacheService {
    private static final String CACHE_ALIAS = "persistentCache";
    private static final String CACHE_DIR = "cache";
    CacheManager cacheManager;

    public CacheService() {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(createFile(CACHE_DIR)))
                .withCache(CACHE_ALIAS, getCacheConfiguration()).build(true);
    }

    public static CacheService getInstance() {
        return ApplicationManager.getApplication().getService(CacheService.class);
    }

    public void clean() {
        getCache().clear();
    }

    public void put(String key, String value) {
        getCache().put(key, value);
    }

    public String get(String key) {
        return getCache().get(key);
    }

    private Cache<String, String> getCache() {
        return cacheManager.getCache(CACHE_ALIAS, String.class, String.class);
    }

    private CacheConfiguration<String, String> getCacheConfiguration() {
        return CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, String.class, getCacheResourcePoolsBuilder())
                .withExpiry(getExpiryPolicy()).build();
    }

    private ResourcePoolsBuilder getCacheResourcePoolsBuilder() {
        return ResourcePoolsBuilder.newResourcePoolsBuilder()
                .heap(10, MemoryUnit.MB)
                .disk(50, MemoryUnit.MB, true); // Enable disk storage
    }

    private ExpiryPolicy<Object, Object> getExpiryPolicy() {
        return ExpiryPolicyBuilder.timeToLiveExpiration(Duration.of(7, ChronoUnit.DAYS));
    }
}

package com.example.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 缓存与通用 Bean 配置。
 * <p>
 * 预占时效缓存策略：被动刷新（过期后首次请求触发），TTL 由配置项
 * {@code preoccupy.cache.ttl-minutes} 控制，默认 30 分钟。
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PREOCCUPY_DURATION_CACHE = "preoccupyDuration";

    @Value("${preoccupy.cache.ttl-minutes:30}")
    private int cacheTtlMinutes;

    @Value("${preoccupy.cache.max-size:1000}")
    private int cacheMaxSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(PREOCCUPY_DURATION_CACHE);
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(cacheMaxSize)
                .expireAfterWrite(Duration.ofMinutes(cacheTtlMinutes)));
        return manager;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

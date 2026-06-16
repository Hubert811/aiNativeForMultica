package com.example.service;

import com.example.client.WebshopClient;
import com.example.config.CacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 预占时效本地缓存服务。
 * <p>
 * 缓存键：{@code preoccupy_duration:{对接抬头ID}}
 * <br>缓存 TTL：30 分钟（可配置，见 {@code preoccupy.cache.ttl-minutes}）
 * <br>刷新机制：被动刷新（过期后首次请求触发）
 * <br>降级：缓存未命中且 Webshop 不可达时，返回 null（前端不展示倒计时）
 */
@Service
public class PreoccupyDurationCacheService {

    private static final Logger log = LoggerFactory.getLogger(PreoccupyDurationCacheService.class);

    /** 预占时效合法取值范围：1 ~ 720 小时（1 分钟 ~ 30 天） */
    public static final int MIN_DURATION_HOURS = 1;
    public static final int MAX_DURATION_HOURS = 720;

    private final CacheManager cacheManager;
    private final WebshopClient webshopClient;

    public PreoccupyDurationCacheService(CacheManager cacheManager, WebshopClient webshopClient) {
        this.cacheManager = cacheManager;
        this.webshopClient = webshopClient;
    }

    /**
     * 批量获取对接抬头预占时效（优先缓存，未命中批量查询 Webshop）。
     *
     * @param dockingHeaderIds 对接抬头 ID 集合
     * @return Map&lt;对接抬头ID, 预占时效(小时)&gt;，缺失或非法值不出现在 Map 中
     */
    public Map<String, Integer> batchGet(Collection<String> dockingHeaderIds) {
        if (dockingHeaderIds == null || dockingHeaderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Cache cache = cacheManager.getCache(CacheConfig.PREOCCUPY_DURATION_CACHE);
        Map<String, Integer> result = new HashMap<>();
        java.util.List<String> missed = new java.util.ArrayList<>();

        // 1. 先查缓存
        for (String id : dockingHeaderIds) {
            String key = cacheKey(id);
            Cache.ValueWrapper wrapper = cache == null ? null : cache.get(key);
            if (wrapper != null) {
                Integer value = (Integer) wrapper.get();
                if (value != null) {
                    result.put(id, value);
                }
                // value == null 表示该 ID 在 Webshop 中未配置，缓存了"空值"避免穿透
            } else {
                missed.add(id);
            }
        }

        if (missed.isEmpty()) {
            return result;
        }

        // 2. 缓存未命中，批量查询 Webshop
        Map<String, Integer> fetched;
        try {
            fetched = webshopClient.batchGetPreoccupyDuration(missed);
        } catch (Exception e) {
            log.warn("Webshop unreachable, degrading for missed ids={}", missed, e);
            fetched = Collections.emptyMap();
        }

        // 3. 校验并回填缓存
        for (String id : missed) {
            Integer value = fetched.get(id);
            Integer validated = validate(value);
            String key = cacheKey(id);
            if (cache != null) {
                // put 即使为 null 也缓存，避免缓存穿透；若需区分"未配置"与"未查询到"可改用 Optional
                cache.put(key, validated);
            }
            if (validated != null) {
                result.put(id, validated);
            }
        }

        return result;
    }

    /**
     * 校验预占时效取值范围（1 ~ 720 小时）。非法值返回 null。
     */
    public static Integer validate(Integer duration) {
        if (duration == null) {
            return null;
        }
        if (duration < MIN_DURATION_HOURS || duration > MAX_DURATION_HOURS) {
            log.warn("Preoccupy duration out of range [{}-{}]: {}", MIN_DURATION_HOURS, MAX_DURATION_HOURS, duration);
            return null;
        }
        return duration;
    }

    private static String cacheKey(String dockingHeaderId) {
        return "preoccupy_duration:" + dockingHeaderId;
    }
}

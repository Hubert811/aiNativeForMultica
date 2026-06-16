package com.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Webshop 客户端 —— 读取对接抬头预占时效配置。
 * <p>
 * 配置数据低频变更，由 {@link com.example.service.PreoccupyDurationCacheService}
 * 提供本地缓存（TTL 30 分钟）。本客户端只在缓存未命中时被调用。
 * <p>
 * 降级：Webshop 不可达时返回空 Map，由上层决定是否继续使用 null。
 */
@Component
public class WebshopClient {

    private static final Logger log = LoggerFactory.getLogger(WebshopClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public WebshopClient(
            RestTemplate restTemplate,
            @Value("${webshop.base-url:}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * 批量查询对接抬头预占时效配置。
     *
     * @param dockingHeaderIds 对接抬头 ID 集合
     * @return Map&lt;对接抬头ID, 预占时效(小时)&gt;；不可达时返回空 Map
     */
    public Map<String, Integer> batchGetPreoccupyDuration(Collection<String> dockingHeaderIds) {
        if (dockingHeaderIds == null || dockingHeaderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            log.warn("Webshop base-url not configured, returning empty map");
            return Collections.emptyMap();
        }

        try {
            // TODO: 实际项目替换为真实的 Webshop API 路径与参数格式
            String url = baseUrl + "/api/preoccupy-duration/batch?ids=" + String.join(",", dockingHeaderIds);
            ResponseEntity<PreoccupyDurationResponse> response =
                    restTemplate.getForEntity(url, PreoccupyDurationResponse.class);

            if (response.getBody() == null || response.getBody().data == null) {
                return Collections.emptyMap();
            }
            return response.getBody().data;
        } catch (Exception e) {
            log.error("Failed to fetch preoccupy duration from Webshop, ids={}", dockingHeaderIds, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Webshop 响应结构（简化示例）。
     */
    public static class PreoccupyDurationResponse {
        public Map<String, Integer> data;
    }
}

package com.example.service;

import com.example.client.WebshopClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

class PreoccupyDurationCacheServiceTest {

    private CacheManager cacheManager;
    private WebshopClient webshopClient;
    private PreoccupyDurationCacheService service;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager("preoccupyDuration");
        webshopClient = mock(WebshopClient.class);
        service = new PreoccupyDurationCacheService(cacheManager, webshopClient);
    }

    @Test
    void validate_rejectsOutOfRange() {
        assertNull(PreoccupyDurationCacheService.validate(null));
        assertNull(PreoccupyDurationCacheService.validate(0));
        assertNull(PreoccupyDurationCacheService.validate(-1));
        assertNull(PreoccupyDurationCacheService.validate(721));
        assertEquals(1, PreoccupyDurationCacheService.validate(1));
        assertEquals(720, PreoccupyDurationCacheService.validate(720));
        assertEquals(24, PreoccupyDurationCacheService.validate(24));
    }

    @Test
    void batchGet_cacheHit_doesNotCallWebshop() {
        // 预先填充缓存
        Cache cache = cacheManager.getCache("preoccupyDuration");
        assertNotNull(cache);
        cache.put("preoccupy_duration:HDR-1", 24);

        Map<String, Integer> result = service.batchGet(List.of("HDR-1"));

        assertEquals(Map.of("HDR-1", 24), result);
        verifyNoInteractions(webshopClient);
    }

    @Test
    void batchGet_cacheMiss_callsWebshopAndCaches() {
        when(webshopClient.batchGetPreoccupyDuration(anyCollection()))
                .thenReturn(Map.of("HDR-1", 48, "HDR-2", 24));

        Map<String, Integer> first = service.batchGet(List.of("HDR-1", "HDR-2"));
        assertEquals(Map.of("HDR-1", 48, "HDR-2", 24), first);
        verify(webshopClient, times(1)).batchGetPreoccupyDuration(List.of("HDR-1", "HDR-2"));

        // 第二次请求应命中缓存，不再调用 Webshop
        reset(webshopClient);
        Map<String, Integer> second = service.batchGet(List.of("HDR-1", "HDR-2"));
        assertEquals(Map.of("HDR-1", 48, "HDR-2", 24), second);
        verifyNoInteractions(webshopClient);
    }

    @Test
    void batchGet_webshopUnreachable_returnsEmpty() {
        when(webshopClient.batchGetPreoccupyDuration(anyCollection()))
                .thenThrow(new RuntimeException("connection refused"));

        Map<String, Integer> result = service.batchGet(List.of("HDR-1"));
        assertTrue(result.isEmpty());
    }

    @Test
    void batchGet_outOfRangeValue_filteredOut() {
        when(webshopClient.batchGetPreoccupyDuration(anyCollection()))
                .thenReturn(Map.of("HDR-1", 0, "HDR-2", 9999, "HDR-3", 12));

        Map<String, Integer> result = service.batchGet(List.of("HDR-1", "HDR-2", "HDR-3"));
        assertEquals(Map.of("HDR-3", 12), result);
    }

    @Test
    void batchGet_emptyInput_returnsEmpty() {
        Map<String, Integer> result = service.batchGet(Collections.emptyList());
        assertTrue(result.isEmpty());
        verifyNoInteractions(webshopClient);
    }
}

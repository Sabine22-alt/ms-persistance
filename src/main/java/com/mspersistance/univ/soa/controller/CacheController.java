package com.mspersistance.univ.soa.controller;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Endpoint pour monitorer le cache et le vider si nÃ©cessaire.
 */
@RestController
@RequestMapping("/api/persistance/cache")
@CrossOrigin(origins = "*")
public class CacheController {

    private final CacheManager cacheManager;

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * GET /api/persistance/cache/stats - Statistiques du cache
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache caffeineCache) {
                var caffeineStats = caffeineCache.getNativeCache().stats();

                Map<String, Object> cacheStats = new HashMap<>();
                cacheStats.put("hitCount", caffeineStats.hitCount());
                cacheStats.put("missCount", caffeineStats.missCount());
                cacheStats.put("hitRate", caffeineStats.hitRate());
                cacheStats.put("evictionCount", caffeineStats.evictionCount());
                cacheStats.put("estimatedSize", caffeineCache.getNativeCache().estimatedSize());

                stats.put(cacheName, cacheStats);
            }
        });

        return ResponseEntity.ok(stats);
    }

    /**
     * DELETE /api/persistance/cache - Vider tout le cache
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName ->
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear()
        );

        Map<String, String> response = new HashMap<>();
        response.put("message", "Tous les caches ont Ã©tÃ© vidÃ©s");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/persistance/cache/{cacheName} - Vider un cache spÃ©cifique
     */
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache '" + cacheName + "' vidÃ©");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }
}


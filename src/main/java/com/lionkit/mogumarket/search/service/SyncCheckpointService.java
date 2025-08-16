package com.lionkit.mogumarket.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SyncCheckpointService {
    private final StringRedisTemplate redisTemplate;
    private static final String KEY = "sync:product:lastSyncedAt";

    public LocalDateTime loadLastSyncedAt() {
        String v = redisTemplate.opsForValue().get(KEY);
        return (v == null) ? LocalDateTime.MIN : LocalDateTime.parse(v);
    }

    public void saveLastSyncedAt(LocalDateTime t) {
        redisTemplate.opsForValue().set(KEY, t.toString());
    }
}
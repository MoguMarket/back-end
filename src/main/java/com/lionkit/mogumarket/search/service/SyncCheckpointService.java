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
    private static final LocalDateTime DEFAULT_SYNC_TIME = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

    public LocalDateTime loadLastSyncedAt() {
        String v = redisTemplate.opsForValue().get(KEY);
        return (v == null) ? DEFAULT_SYNC_TIME : LocalDateTime.parse(v);
    }

    public void saveLastSyncedAt(LocalDateTime t) {
        redisTemplate.opsForValue().set(KEY, t.toString());
    }
}
package com.lionkit.mogumarket.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisSearchRankService {

    private final StringRedisTemplate redisTemplate;

    public void increaseKeywordScore(String keyword) {
        redisTemplate.opsForZSet().incrementScore("search_rank", keyword, 1);
    }

    public List<String> getTopKeywords(int count) {
        return redisTemplate.opsForZSet()
                .reverseRange("search_rank", 0, count - 1)
                .stream().toList();
    }
}


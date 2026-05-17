package com.example.vocabook.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    // 저장
    public void save(String key, Object value, Duration seconds) {
        redisTemplate.opsForValue().set(key, value, seconds);
    }

    public void save(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 조회
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 삭제
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 저장 여부 확인
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}
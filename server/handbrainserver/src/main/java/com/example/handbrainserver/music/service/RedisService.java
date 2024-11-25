package com.example.handbrainserver.music.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Redis에 데이터 저장 (만료 시간 설정)
    public void setWithExpiration(String key, String value, long timeoutInSeconds) {
        redisTemplate.opsForValue().set(key, value, timeoutInSeconds);
    }

    // Redis에서 데이터 조회
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // Redis에서 데이터 삭제
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}


package com.maximys777.pugs.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TelegramLinkService {

    private final StringRedisTemplate redisTemplate;

    @Value("${telegram.bot.name}")
    private String botName;

    public String generateLinkUrl(String username) {
        String token = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("link:" + token, username, Duration.ofMinutes(5));

        return String.format("https://t.me/%s?start=%s", botName, token);
    }

    public String getUsernameByLinkToken(String token) {
        String key = "link:" + token;
        String username = redisTemplate.opsForValue().get(key);

        if (username != null) {
            redisTemplate.delete(key);
        }

        return username;
    }
}

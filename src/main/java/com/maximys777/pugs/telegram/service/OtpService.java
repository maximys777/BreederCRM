package com.maximys777.pugs.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom random = new SecureRandom();

    public String generateOtpCode(String username) {
        String code = String.format("%06d", random.nextInt(1000000));
        String key = "otp:" + username;

        redisTemplate.opsForValue().set(key, code, Duration.ofSeconds(30));

        return code;
    }

    public boolean validateOtpCode(String username, String inputCode) {
        String key = "otp:" + username;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode != null && storedCode.equals(inputCode)) {
            redisTemplate.delete(key);
            
            return true;
        }

        return false;
    }
}

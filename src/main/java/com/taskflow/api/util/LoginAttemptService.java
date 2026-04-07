package com.taskflow.api.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    // Key: IP address, Value: failed attempt count
    private final Cache<String, Integer> attemptsCache = Caffeine.newBuilder()
            .expireAfterWrite(LOCK_DURATION_MINUTES, TimeUnit.MINUTES)
            .build();

    public void loginSucceeded(String ip) {
        attemptsCache.invalidate(ip);
    }

    public void loginFailed(String ip) {
        int attempts = getAttempts(ip);
        attemptsCache.put(ip, attempts + 1);
    }

    public boolean isBlocked(String ip) {
        return getAttempts(ip) >= MAX_ATTEMPTS;
    }

    public int getAttempts(String ip) {
        Integer attempts = attemptsCache.getIfPresent(ip);
        return attempts == null ? 0 : attempts;
    }

    public int getRemainingAttempts(String ip) {
        return Math.max(0, MAX_ATTEMPTS - getAttempts(ip));
    }
}
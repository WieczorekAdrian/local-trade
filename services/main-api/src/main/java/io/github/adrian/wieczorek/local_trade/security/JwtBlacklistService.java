package io.github.adrian.wieczorek.local_trade.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtBlacklistService {

  private final StringRedisTemplate redisTemplate;

  private static final String BLACKLIST_VALUE = "blacklisted";

  public void blacklistToken(String token, long timeToLiveInSeconds) {
    if (timeToLiveInSeconds > 0) {
      redisTemplate.opsForValue().set(token, BLACKLIST_VALUE,
          Duration.ofSeconds(timeToLiveInSeconds));
    }
  }

  public boolean isTokenBlacklisted(String token) {
    return redisTemplate.hasKey(token);
  }

}

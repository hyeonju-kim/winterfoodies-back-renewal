package com.winter.winter_back.common.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;

/**
 * description    : 캐시 (레디스) 설정 파일
 * fileName       : CacheConfig
 * date           : 25. 9. 21.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 21.         hj kim             최초 생성
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // 로그인 토큰(TokenVo)을 저장하는 캐시. refresh 토큰이 살아 있는 동안은 남아 있어야 토큰 갱신이 가능하다
    public static final String TOKEN_CACHE_NAME = "winter_cache";

    @Value("${jwt.expire-time.refresh:1440}")
    private long refreshTokenExpireMinute;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                GenericJacksonJsonRedisSerializer.builder()
                                        // 캐시 값을 원래 타입(TokenVo 등)으로 복원하기 위해 타입 정보 저장, 허용 패키지는 제한
                                        .enableDefaultTyping(BasicPolymorphicTypeValidator.builder()
                                                .allowIfSubType("com.winter.winter_back.")
                                                .allowIfSubType("java.util.")
                                                .allowIfSubType("java.time.")
                                                .build())
                                        .build()
                        )
                )
                .entryTtl(Duration.ofMinutes(300))
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(TOKEN_CACHE_NAME, defaultConfig.entryTtl(Duration.ofMinutes(refreshTokenExpireMinute)))
                .build();
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // String 직렬화 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}

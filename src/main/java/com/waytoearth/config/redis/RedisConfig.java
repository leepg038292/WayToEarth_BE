package com.waytoearth.config.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {

    /**
     * 랭킹 전용 RedisTemplate (ZSet 작업용)
     * - 크루 랭킹, 멤버 랭킹, 성장률 랭킹에 사용
     * - ZSet으로 실시간 정렬된 랭킹 관리
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key는 항상 String으로 저장 (예: "crew:ranking:growth:2024-03")
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value는 JSON으로 저장 (크루 정보, 점수 등)
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        log.info("Redis ZSet 랭킹 시스템 준비 완료 - RedisTemplate configured");
        return template;
    }

    /**
     * 문자열 전용 RedisTemplate (메타데이터용)
     * - 랭킹 계산 시간, 통계 정보 등 간단한 데이터 저장
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);

        log.info("StringRedisTemplate 설정 완료 - 메타데이터 관리용");
        return template;
    }
}
package ru.goidaai.test_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Конфигурация Redis кэширования
 * По умолчанию отключена (требуется явное включение через application.properties)
 */
@Configuration
public class RedisConfig {

    @Value("${redis.enabled:false}")
    private boolean redisEnabled;

    @Value("${redis.host:localhost}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private int redisPort;

    @Value("${redis.password:}")
    private String redisPassword;

    @Value("${redis.ttl.minutes:10}")
    private int cacheTtlMinutes;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        if (!redisEnabled) {
            // Возвращаем заглушку если Redis отключен
            return new LettuceConnectionFactory(new LettuceConnectionFactory.StandaloneConfiguration() {{
                setHostName("localhost");
                setPort(6379);
            }}) {
                @Override
                public void afterPropertiesSet() {
                    // Не инициализируем соединение если Redis отключен
                }
            };
        }
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        factory.setHostName(redisHost);
        factory.setPort(redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            factory.setPassword(redisPassword);
        }
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(cacheTtlMinutes))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withCacheConfiguration("assets", config.entryTtl(Duration.ofMinutes(5)))
            .withCacheConfiguration("rates", config.entryTtl(Duration.ofMinutes(1)))
            .withCacheConfiguration("dashboard", config.entryTtl(Duration.ofMinutes(2)))
            .withCacheConfiguration("transactions", config.entryTtl(Duration.ofMinutes(1)))
            .build();
    }
}

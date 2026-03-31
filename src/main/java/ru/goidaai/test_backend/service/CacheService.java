package ru.goidaai.test_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Сервис кэширования на основе Redis
 * Предоставляет методы для работы с кэшем
 * По умолчанию отключен (требуется явное включение через application.properties)
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.enabled:false}")
    private boolean redisEnabled;

    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Получить значение из кэша
     */
    public <T> T get(String key, Class<T> clazz) {
        if (!redisEnabled) {
            return null;
        }
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && clazz.isInstance(value)) {
                return clazz.cast(value);
            }
            return null;
        } catch (Exception e) {
            log.error("Ошибка получения из кэша ключа {}: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Сохранить значение в кэш
     */
    public void set(String key, Object value, long ttl, TimeUnit unit) {
        if (!redisEnabled) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value, ttl, unit);
            log.debug("Значение сохранено в кэш с ключом {}", key);
        } catch (Exception e) {
            log.error("Ошибка сохранения в кэш ключа {}: {}", key, e.getMessage());
        }
    }

    /**
     * Удалить значение из кэша
     */
    public void delete(String key) {
        if (!redisEnabled) {
            return;
        }
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Значение удалено из кэша с ключом {}", key);
            }
        } catch (Exception e) {
            log.error("Ошибка удаления из кэша ключа {}: {}", key, e.getMessage());
        }
    }

    /**
     * Проверить наличие ключа в кэше
     */
    public boolean hasKey(String key) {
        if (!redisEnabled) {
            return false;
        }
        try {
            Boolean hasKey = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            log.error("Ошибка проверки ключа в кэше {}: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Очистить кэш по паттерну
     */
    public void deleteByPattern(String pattern) {
        if (!redisEnabled) {
            return;
        }
        try {
            redisTemplate.delete(redisTemplate.keys(pattern));
            log.debug("Кэш очищен по паттерну {}", pattern);
        } catch (Exception e) {
            log.error("Ошибка очистки кэша по паттерну {}: {}", pattern, e.getMessage());
        }
    }

    /**
     * Инкрементировать значение
     */
    public Long increment(String key, long delta) {
        if (!redisEnabled) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Ошибка инкрементирования ключа {}: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Декрементировать значение
     */
    public Long decrement(String key, long delta) {
        if (!redisEnabled) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("Ошибка декрементирования ключа {}: {}", key, e.getMessage());
            return null;
        }
    }
}

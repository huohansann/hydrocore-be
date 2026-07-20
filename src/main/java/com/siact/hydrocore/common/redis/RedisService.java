package com.siact.hydrocore.common.redis;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Shared Redis boundary. Redis storage is string based; object caches are encoded explicitly as JSON.
 */
@Component
public class RedisService {
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setString(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setString(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String getString(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public boolean expire(String key, long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key);
        return expire == null ? -2L : expire;
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public boolean deleteAll(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return false;
        }
        Long deleted = redisTemplate.delete(keys);
        return deleted != null && deleted > 0;
    }

    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    public <T> void setJson(String key, T value) {
        setString(key, JSON.toJSONString(value));
    }

    public <T> void setJson(String key, T value, long timeout, TimeUnit unit) {
        setString(key, JSON.toJSONString(value), timeout, unit);
    }

    public <T> T getJson(String key, Class<T> clazz) {
        rejectObjectClass(clazz);
        String json = getString(key);
        if (json == null) {
            return null;
        }
        return JSON.parseObject(json, clazz);
    }

    public <T> T getJson(String key, TypeReference<T> typeReference) {
        rejectObjectTypeReference(typeReference);
        String json = getString(key);
        if (json == null) {
            return null;
        }
        return JSON.parseObject(json, typeReference);
    }

    public void putHashString(String key, String hKey, String value) {
        redisTemplate.<String, String>opsForHash().put(key, hKey, value);
    }

    public void putAllHashString(String key, Map<String, String> values) {
        if (values != null && !values.isEmpty()) {
            redisTemplate.<String, String>opsForHash().putAll(key, values);
        }
    }

    public String getHashString(String key, String hKey) {
        return redisTemplate.<String, String>opsForHash().get(key, hKey);
    }

    public List<String> multiGetHashString(String key, Collection<String> hKeys) {
        if (hKeys == null || hKeys.isEmpty()) {
            return Collections.emptyList();
        }
        return redisTemplate.<String, String>opsForHash().multiGet(key, hKeys);
    }

    public boolean deleteHash(String key, String hKey) {
        Long deleted = redisTemplate.<String, String>opsForHash().delete(key, hKey);
        return deleted != null && deleted > 0;
    }

    public <T> void putHashJson(String key, String hKey, T value) {
        putHashString(key, hKey, JSON.toJSONString(value));
    }

    public <T> void putHashJson(String key, String hKey, T value, long timeout, TimeUnit unit) {
        putHashJson(key, hKey, value);
        if (timeout > 0) {
            expire(key, timeout, unit);
        }
    }

    public <T> T getHashJson(String key, String hKey, Class<T> clazz) {
        rejectObjectClass(clazz);
        String json = getHashString(key, hKey);
        if (json == null) {
            return null;
        }
        return JSON.parseObject(json, clazz);
    }

    public <T> T getHashJson(String key, String hKey, TypeReference<T> typeReference) {
        rejectObjectTypeReference(typeReference);
        String json = getHashString(key, hKey);
        if (json == null) {
            return null;
        }
        return JSON.parseObject(json, typeReference);
    }

    public <T> List<T> multiGetHashJson(String key, Collection<String> hKeys, Class<T> clazz) {
        rejectObjectClass(clazz);
        return multiGetHashString(key, hKeys).stream()
                .map(json -> json == null ? null : JSON.parseObject(json, clazz))
                .collect(Collectors.toList());
    }

    public boolean tryLock(String key, String value, long timeout) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS));
    }

    public void unlock(String key, String value) {
        redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), value);
    }

    private void rejectObjectClass(Class<?> clazz) {
        if (Object.class.equals(clazz)) {
            throw new IllegalArgumentException("Redis JSON reads require a concrete target type, not Object.class");
        }
    }

    private void rejectObjectTypeReference(TypeReference<?> typeReference) {
        if (Object.class.equals(typeReference.getType())) {
            throw new IllegalArgumentException("Redis JSON reads require a concrete target type, not Object.class");
        }
    }
}

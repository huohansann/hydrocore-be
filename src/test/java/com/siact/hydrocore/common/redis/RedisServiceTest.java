package com.siact.hydrocore.common.redis;

import com.alibaba.fastjson2.TypeReference;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisServiceTest {
    private RedisTemplate<String, String> redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private HashOperations<String, String, String> hashOperations;
    private RedisService redisService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.<String, String>opsForHash()).thenReturn(hashOperations);
        redisService = new RedisService(redisTemplate);
    }

    @Test
    void setJsonStoresPlainJsonStringWithoutClassMetadata() {
        CacheDto dto = new CacheDto();
        dto.setCode("A1");

        redisService.setJson("cache:1", dto, 5, TimeUnit.MINUTES);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq("cache:1"), jsonCaptor.capture(), eq(5L), eq(TimeUnit.MINUTES));
        assertThat(jsonCaptor.getValue()).contains("\"code\":\"A1\"");
        assertThat(jsonCaptor.getValue()).doesNotContain("@type");
        assertThat(jsonCaptor.getValue()).doesNotContain("com.siact");
    }

    @Test
    void getJsonRequiresConcreteClass() {
        when(valueOperations.get("cache:1")).thenReturn("{\"code\":\"A1\"}");

        CacheDto dto = redisService.getJson("cache:1", CacheDto.class);

        assertThat(dto.getCode()).isEqualTo("A1");
    }

    @Test
    void getJsonRejectsObjectClass() {
        assertThatThrownBy(() -> redisService.getJson("cache:1", Object.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Object.class");
    }

    @Test
    void getJsonSupportsTypeReference() {
        when(valueOperations.get("cache:list")).thenReturn("[{\"code\":\"A1\"},{\"code\":\"B2\"}]");

        List<CacheDto> list = redisService.getJson("cache:list", new TypeReference<List<CacheDto>>() {});

        assertThat(list).extracting(CacheDto::getCode).containsExactly("A1", "B2");
    }

    @Test
    void getJsonRejectsObjectTypeReference() {
        assertThatThrownBy(() -> redisService.getJson("cache:1", new TypeReference<Object>() {}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Object.class");
    }

    @Test
    void hashJsonStoresAndReadsTypedList() {
        CacheDto dto = new CacheDto();
        dto.setCode("A1");
        redisService.putHashJson("nodeHistory", "field-1", Arrays.asList(dto), 60, TimeUnit.SECONDS);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(hashOperations).put(eq("nodeHistory"), eq("field-1"), jsonCaptor.capture());
        assertThat(jsonCaptor.getValue()).contains("\"code\":\"A1\"");
        verify(redisTemplate).expire("nodeHistory", 60, TimeUnit.SECONDS);

        when(hashOperations.get("nodeHistory", "field-1")).thenReturn("[{\"code\":\"A1\"}]");
        List<CacheDto> cached = redisService.getHashJson(
                "nodeHistory",
                "field-1",
                new TypeReference<List<CacheDto>>() {}
        );
        assertThat(cached).extracting(CacheDto::getCode).containsExactly("A1");
    }

    @Test
    void multiGetHashStringKeepsFrontendCacheValuesAsStrings() {
        when(hashOperations.multiGet("FrontendCache_7", Arrays.asList("theme", "layout")))
                .thenReturn(Arrays.asList("dark", "{\"compact\":true}"));

        List<String> values = redisService.multiGetHashString("FrontendCache_7", Arrays.asList("theme", "layout"));

        assertThat(values).containsExactly("dark", "{\"compact\":true}");
    }

    @Test
    void deleteAllAndTryLockUseStringTemplateOperations() {
        Collection<String> keys = Arrays.asList("token:refresh:7:a", "token:refresh:7:b");
        when(redisTemplate.delete(keys)).thenReturn(2L);
        when(valueOperations.setIfAbsent("token:lock:old", "1", 10, TimeUnit.SECONDS)).thenReturn(true);

        assertThat(redisService.deleteAll(keys)).isTrue();
        assertThat(redisService.tryLock("token:lock:old", "1", 10)).isTrue();
    }

    @Test
    void unlockOnlyDeletesWhenLockValueMatches() {
        when(redisTemplate.execute(any(), eq(Collections.singletonList("token:lock:old")), eq("1"))).thenReturn(1L);

        redisService.unlock("token:lock:old", "1");

        verify(redisTemplate).execute(any(), eq(Collections.singletonList("token:lock:old")), eq("1"));
    }

    @Data
    private static class CacheDto {
        private String code;
    }
}

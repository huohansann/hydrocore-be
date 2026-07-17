package com.siact.hydrocore.core.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeThreadPoolPropertiesTest {
    @Test
    void bindsRuntimeThreadPoolOverrides() {
        Map<String, Object> source = new HashMap<>();
        source.put("hydrocore.thread-pools.io.core-size", "8");
        source.put("hydrocore.thread-pools.io.max-size", "16");
        source.put("hydrocore.thread-pools.io.queue-capacity", "256");
        source.put("hydrocore.thread-pools.io.thread-name-prefix", "hydro-io-");

        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new OriginTrackedMapPropertySource("test", source));

        RuntimeThreadPoolProperties properties = Binder.get(environment)
                .bind("hydrocore.thread-pools", Bindable.of(RuntimeThreadPoolProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertThat(properties.getIo().getCoreSize()).isEqualTo(8);
        assertThat(properties.getIo().getMaxSize()).isEqualTo(16);
        assertThat(properties.getIo().getQueueCapacity()).isEqualTo(256);
        assertThat(properties.getIo().getThreadNamePrefix()).isEqualTo("hydro-io-");
    }
}

package com.siact.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-11-21 11:14
 * @className : KilnProperty
 * @description : kiln 配置类
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.kiln")
public class KilnProperty {
    private Config config;
    private Map<String, IntervalControl> intervalControl = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class Config {
        private long interval;
        private long fireChangeCycle = 21;
    }

    @Getter
    @Setter
    public static class IntervalControl {
        // 计算平均温度的换火周期区间
        private List<Integer> range;
        // 告警限与控制限的差值
        private List<Integer> diffValue;
    }
}

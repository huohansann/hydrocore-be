package com.siact.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
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
    private Algorithm algorithm;
    private Map<String, IntervalControl> intervalControl = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class Config {
        private long fireChangeCycle = 21;
    }

    @Getter
    @Setter
    public static class Algorithm {
        // 算法调用地址
        private String baseUrl;
        // 智能计算值定时任务调用时间
        private long intelligentInterval;
        // 控制算法调用超时时间
        private int intelligentTimeout = 30_000;
        // 智能控制算法 deltaC 调用暂停时间
        private long intelliStopInterval;
        // 温度预测定时任务调用时间
        private long predictedInterval;
    }

    @Getter
    @Setter
    public static class IntervalControl {
        // 计算平均温度的换火周期区间
        private List<Integer> range;
        // 上下限差值
        private BigDecimal spd;
        // 告警限与控制限的差值
        private List<Integer> diffValue;
    }
}

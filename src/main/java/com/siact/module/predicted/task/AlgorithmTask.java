package com.siact.module.predicted.task;

import com.siact.common.redis.RedisService;
import com.siact.module.predicted.service.AlgorithmPredictedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RefreshScope
public class AlgorithmTask {

    // 是否开启算法预测,默认false
    @Value("${algorithm.prediction.enable:false}")
    private Boolean algorithmPredictionEnable;

    @Autowired
    private AlgorithmPredictedService algorithmPredictedService;
    @Autowired
    private RedisService redisService;


    /**
     * 每分钟调用一次算法  获取预测数据
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void algorithmInference() {
        if (!algorithmPredictionEnable) {
            log.info("算法预测配置未开启");
            return;
        }
        algorithmPredictedService.algorithmInference();
    }

    /**
     * 每月1号定时删除之前的调用记录
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void deleteAlgorithmCallInfoBeforeTime() {
        // 默认删除上月前的数据(保留一个月)
        algorithmPredictedService.deleteAlgorithmCallInfoBeforeTime("");
    }

    /**
     * 每十分钟调用一次算法  获取智能计算值
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void getIntelligentComputing() {
        Object cacheObject = redisService.getCacheObject("getIntelligentComputing");

        if (cacheObject != null) {
            redisService.setCacheObject("getIntelligentComputing", "1", 10L, TimeUnit.SECONDS);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                algorithmPredictedService.getIntelligentComputing();
            }
        }).start();
    }

}

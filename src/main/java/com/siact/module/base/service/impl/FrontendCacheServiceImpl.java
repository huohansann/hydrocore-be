package com.siact.module.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.redis.RedisService;
import com.siact.module.base.service.FrontendCacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FrontendCacheServiceImpl implements FrontendCacheService {

    @Value("${frontend.cache.expire:60}")
    private Long frontendCacheExpire;

    @Autowired
    private RedisService redisService;

    @Override
    public void setConfig(String userId, String key, String value) {
        log.info("设置配置setConfig userId: {}, key: {}, value: {}", userId, key, value);
        //  设置key的话  过期时间为10分钟
        // 为每个人设置hash格式的redis key为userId value为 code - value
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put(key, value);
        redisService.setCacheMap(getFrontendCache(userId), paramMap);

        redisService.expire(getFrontendCache(userId), frontendCacheExpire, TimeUnit.DAYS);
    }

    @Override
    public Map<String, String> getConfig(String userId, String keys) {
        log.info("获取配置getConfig userId: {}, keys: {}", userId, keys);
        if (StringUtils.isBlank(keys)) {
            return new HashMap<>();
        }

        String[] keyArr = keys.split(ConstantSymbol.COMMA);
        // 获取key对应的value
        List<Object> cacheValueList = redisService.getMultiCacheMapValue(getFrontendCache(userId), Arrays.asList(keyArr));
        if (ObjectUtils.isEmpty(cacheValueList)) {
            return null;
        }

        // 刷新过期时间
        redisService.expire(getFrontendCache(userId), frontendCacheExpire, TimeUnit.DAYS);

        // 转换为json字符串返回
        LinkedHashMap<String, String> resultMap = new LinkedHashMap<>();
        for (int i = 0; i < keyArr.length; i++) {
            Object cacheVal = cacheValueList.get(i);
            resultMap.put(keyArr[i], ObjectUtils.isEmpty(cacheVal) ? null : JSON.toJSONString(cacheVal));
        }

        return resultMap;
    }

    @Override
    public void deleteConfig(String userId, String keys) {
        log.info("删除配置deleteConfig userId: {}, keys: {}", userId, keys);
        if (StringUtils.isBlank(keys)) {
            // 如果没有传入keys  删除整个hash
            redisService.deleteObject(getFrontendCache(userId));
        } else {
            // 传入keys 遍历删除对应的key
            String[] keyArr = keys.split(ConstantSymbol.COMMA);
            for (String key : keyArr) {
                redisService.deleteCacheMapValue(getFrontendCache(userId), key);
            }
        }

    }

    private String getFrontendCache(String userId) {
        return "FrontendCache_" + userId;
    }
}

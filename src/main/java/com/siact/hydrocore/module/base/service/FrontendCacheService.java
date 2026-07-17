package com.siact.hydrocore.module.base.service;

import java.util.Map;

public interface FrontendCacheService {
    /**
     * 设置前端缓存配置
     * @param userId
     * @param key
     * @param value
     */
    void setConfig(String userId, String key, String value);

    /**
     * 获取前端缓存配置
     * @param userId
     * @param key
     * @return
     */
    Map<String,String> getConfig(String userId, String key);

    /**
     * 删除前端缓存配置
     * @param userId
     * @param keys
     */
    void deleteConfig(String userId, String keys);
}

package com.siact.module.algorithm.services;

import com.alibaba.fastjson2.JSONObject;

import java.util.function.Supplier;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-08 15:11
 * @className : AlgorithmService
 * @description : 算法服务业务类
 */
public interface AlgorithmService {

    /**
     * 调用并解析指定 {@code type} 的算法服务, 并返回算法结果
     *
     * @param type     算法调用类型
     * @param params   算法调用参数 json 字符串
     * @param supplier 请求调用回调函数
     * @return 返回算法接口调用结果
     */
    JSONObject callResolve(String type, String params, Supplier<String> supplier);
}

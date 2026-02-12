package com.siact.module.algorithm.services.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.siact.common.exception.BizException;
import com.siact.common.utils.TimeUtil;
import com.siact.module.algorithm.services.AlgorithmService;
import com.siact.module.model.entity.AlgorithmCallInfoEntity;
import com.siact.module.model.service.AlgorithmCallInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.function.Supplier;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-08 15:16
 * @className : AlgorithmServiceImpl
 * @description : 算法服务业务类实现
 */
@Slf4j
@Service
public class AlgorithmServiceImpl implements AlgorithmService {
    private final AlgorithmCallInfoService algorithmCallInfoService;

    public AlgorithmServiceImpl(AlgorithmCallInfoService algorithmCallInfoService) {
        this.algorithmCallInfoService = algorithmCallInfoService;
    }

    /**
     * 调用并解析指定 {@code type} 的算法服务, 并返回算法结果
     *
     * @param type     算法调用类型
     * @param params   算法调用参数 json 字符串
     * @param supplier 请求调用回调函数
     * @return 返回算法接口调用结果
     */
    @Override
    public JSONObject callResolve(String type, String params, Supplier<String> supplier) {
        AlgorithmCallInfoEntity entity = new AlgorithmCallInfoEntity();
        long callId = IdWorker.getId(entity);
        entity.setId(callId);
        entity.setType(type);
        entity.setReqTime(TimeUtil.getNow());
        entity.setReqJson(params);
        entity.setCreateTime(new Date());

        String respText = null;
        JSONObject response;

        try {
            respText = supplier.get();
            // respText = "{\"code\":200,\"status\":\"SUCCESS\",\"message\":\"控制任务执行成功\",\"result\":{\"timestamp\":\"2026-02-09T03:17:00.112427\",\"status\":\"normal\",\"send_reminder\":false,\"gas_setValue_transformer\":7788,\"gas_setValue_lstm\":7828,\"gas_deltaC_transformer\":0,\"temps\":{\"熔窑碹顶温度2\":{\"setValue\":1370.0,\"transformer\":1423.0,\"lstm\":1423.0,\"DT\":0.0,\"transformer_deltaT\":53.0,\"lstm_deltaT\":53.0},\"熔窑碹顶温度6\":{\"setValue\":1370.0,\"transformer\":1546.5,\"lstm\":1546.5,\"DT\":1.31,\"transformer_deltaT\":176.5,\"lstm_deltaT\":176.5},\"熔窑碹顶温度13\":{\"setValue\":1370.0,\"transformer\":1363.94,\"lstm\":1363.94,\"DT\":0.02,\"transformer_deltaT\":-6.06,\"lstm_deltaT\":-6.06}},\"experience_result\":{\"adjust_detail\":{\"transformer\":{\"adjusts\":{\"deltaC1\":0,\"deltaC2\":-10,\"deltaC3\":-10},\"send_reminder\":false},\"lstm\":{\"adjusts\":{\"deltaC1\":0,\"deltaC2\":-10,\"deltaC3\":-10},\"send_reminder\":false}},\"delta_C\":{\"transformer\":-10.0,\"lstm\":-10.0}},\"last_gas_sum_sv\":7808.0}}";
            response = JSONObject.parseObject(respText);

            log.info("算法请求参数: {}, 结果: {}", params, response);
            entity.setRespTime(TimeUtil.getNow());
            entity.setRespJson(JSON.toJSONString(response));
        } catch (Exception e) {
            log.error("算法调用参数异常, 入参: {}, 响应: {}", params, respText, e);
            String error = "出现异常: 请求返回" + respText + ", 异常信息: " + e.getMessage();
            entity.setRespTime(TimeUtil.getNow());
            entity.setRespJson(error);
            throw new BizException(error);
        } finally {
            algorithmCallInfoService.save(entity);
        }

        //解析结果
        String code = response.getString("code");
        if (!"200".equals(code)) {
            log.error("算法调用参数异常, 入参: {}, 响应: {}", params, response);
            String error = entity.getRespJson() + "出现异常:请求返回" + respText;
            entity.setRespTime(TimeUtil.getNow());
            entity.setRespJson(error);
            algorithmCallInfoService.updateById(entity);
            throw new BizException(error);
        }
        return response;
    }
}

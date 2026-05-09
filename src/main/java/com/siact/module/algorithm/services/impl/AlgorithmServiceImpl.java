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
            // respText = "{\"code\":\"200\",\"status\":\"SUCCESS\",\"message\":\"控制任务执行成功\",\"result\":{\"timestamp\":\"2026-05-09 16:46:00\",\"status\":\"normal\",\"send_reminder\":false,\"temps\":{\"熔窑碹顶温度2\":{\"setValue\":1417.5,\"raw_rnn\":1418.824,\"final_mean\":1417.82,\"trend_anchor\":1417.72,\"pred_value\":1418.824,\"deltaT\":1.324,\"DT\":-0.1,\"slope_10\":-0.0097,\"safety_status\":\"normal\",\"infer_guard\":{\"skip_model\":false,\"reason\":\"ok_with_outliers\",\"feature_count\":17,\"input_length\":80,\"outlier_info\":{\"1#炉助燃风流量实际值\":{\"type\":\"invalid_feature\",\"positions\":[27,47],\"total_points\":80,\"outlier_count\":2,\"original_positions\":[67,87]}},\"outlier_ratio\":0.025,\"max_consecutive\":1,\"invalid_outlier_info\":{\"1#炉助燃风流量实际值\":{\"type\":\"invalid_feature\",\"positions\":[27,47],\"total_points\":80,\"outlier_count\":2,\"original_positions\":[67,87]}},\"non_finite_outlier_info\":{},\"jump_outlier_info\":{}},\"output_guard\":{\"output_guard\":\"step_limited\",\"step_limit\":1.3}},\"熔窑碹顶温度6\":{\"setValue\":1535.0,\"raw_rnn\":1533.858,\"final_mean\":1533.46,\"trend_anchor\":1534.169,\"pred_value\":1533.858,\"deltaT\":-1.142,\"DT\":0.69,\"slope_10\":0.037,\"safety_status\":\"normal\",\"infer_guard\":{\"skip_model\":false,\"reason\":\"ok\",\"feature_count\":4,\"input_length\":80},\"output_guard\":{\"output_guard\":\"step_limited\",\"step_limit\":1.3}},\"熔窑碹顶温度13\":{\"setValue\":1359.0,\"raw_rnn\":1358.276,\"final_mean\":1358.15,\"trend_anchor\":1358.227,\"pred_value\":1358.206,\"deltaT\":-0.794,\"DT\":0.0,\"slope_10\":-0.0011,\"safety_status\":\"normal\",\"infer_guard\":{\"skip_model\":false,\"reason\":\"ok_with_outliers\",\"feature_count\":13,\"input_length\":80,\"outlier_info\":{\"熔窑碹顶温度13\":{\"type\":\"invalid_feature\",\"positions\":[11],\"total_points\":80,\"outlier_count\":1,\"original_positions\":[51]}},\"outlier_ratio\":0.0125,\"max_consecutive\":1,\"invalid_outlier_info\":{\"熔窑碹顶温度13\":{\"type\":\"invalid_feature\",\"positions\":[11],\"total_points\":80,\"outlier_count\":1,\"original_positions\":[51]}},\"non_finite_outlier_info\":{},\"jump_outlier_info\":{}},\"output_guard\":{\"output_guard\":\"step_limited\",\"step_limit\":0.3}}},\"experience_result\":{\"adjust_detail\":{\"deltaC1\":0,\"deltaC2\":0,\"deltaC3\":0},\"delta_C\":0,\"adjust_detail_jilu\":{\"deltaC1\":0,\"deltaC2\":0,\"deltaC3\":0},\"delta_C_jilu\":0},\"flag\":true,\"last_gas_sum_sv\":7410.0,\"0508\":{\"timestamp\":\"2026-05-09 16:46:00\",\"status\":\"normal\",\"send_reminder\":false,\"temps\":{\"熔窑碹顶温度2\":{\"setValue\":1417.5,\"raw_rnn\":1418.223,\"final_mean\":1417.82,\"trend_anchor\":1417.72,\"pred_value\":1417.82,\"deltaT\":0.32,\"DT\":-0.1,\"slope_10\":-0.0097},\"熔窑碹顶温度6\":{\"setValue\":1535.0,\"raw_rnn\":1533.445,\"final_mean\":1533.46,\"trend_anchor\":1534.169,\"pred_value\":1533.445,\"deltaT\":-1.555,\"DT\":0.69,\"slope_10\":0.037},\"熔窑碹顶温度13\":{\"setValue\":1359.0,\"raw_rnn\":1358.323,\"final_mean\":1358.15,\"trend_anchor\":1358.227,\"pred_value\":1358.221,\"deltaT\":-0.779,\"DT\":0.0,\"slope_10\":-0.0011}},\"experience_result\":{\"adjust_detail\":{\"deltaC1\":0,\"deltaC2\":0,\"deltaC3\":0},\"delta_C\":0,\"adjust_detail_jilu\":{\"deltaC1\":0,\"deltaC2\":0,\"deltaC3\":0},\"delta_C_jilu\":0},\"flag\":true,\"last_gas_sum_sv\":7410.0}},\"success\":true}";
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

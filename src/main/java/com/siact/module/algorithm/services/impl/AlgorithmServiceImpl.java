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
            // respText = "{\"code\":\"200\",\"status\":\"SUCCESS\",\"message\":\"控制任务执行成功\",\"result\":{\"timestamp\":\"2026-04-16 10:30:00\",\"status\":\"normal\",\"send_reminder\":false,\"gas_setValue_transformer\":7350,\"gas_setValue_rnn\":7360,\"gas_deltaC_transformer\":0,\"temps\":{\"熔窑碹顶温度2\":{\"setValue\":1415.0,\"raw_transformer\":1414.601,\"transformer_bias_corr\":1411.034,\"final_mean\":1411.38,\"trend_anchor\":1411.384,\"z_value\":-0.18,\"base_pred\":1411.486,\"ensemble_pred\":1411.468,\"oos_enabled\":false,\"oos_metrics\":{\"mae_base_ewm\":0.708496,\"mae_corrected_ewm\":0.696049,\"count\":47794,\"updated_at\":\"2026-04-16T02:30:09.941929\"},\"residual_correction\":-0.018,\"ensemble_deltaT\":-3.53,\"DT\":0.01,\"slope_10\":-0.0039,\"mean_diff_20_10\":0.0171,\"state_tag\":0.0},\"熔窑碹顶温度6\":{\"setValue\":1542.0,\"raw_transformer\":1541.436,\"transformer_bias_corr\":1538.581,\"final_mean\":1539.91,\"trend_anchor\":1539.897,\"z_value\":-1.08,\"base_pred\":1540.197,\"ensemble_pred\":1539.963,\"oos_enabled\":true,\"oos_metrics\":{\"mae_base_ewm\":0.768835,\"mae_corrected_ewm\":0.637608,\"count\":47794,\"updated_at\":\"2026-04-16T02:30:10.094386\"},\"residual_correction\":-0.234,\"ensemble_deltaT\":-2.04,\"DT\":0.0,\"slope_10\":-0.0069,\"mean_diff_20_10\":-0.0054,\"state_tag\":0.0},\"熔窑碹顶温度13\":{\"setValue\":1370.0,\"raw_transformer\":1364.756,\"transformer_bias_corr\":1364.756,\"final_mean\":1365.44,\"trend_anchor\":1365.443,\"z_value\":-1.69,\"base_pred\":1365.336,\"ensemble_pred\":1365.346,\"oos_enabled\":false,\"oos_metrics\":{\"mae_base_ewm\":0.041862,\"mae_corrected_ewm\":0.065376,\"count\":47760,\"updated_at\":\"2026-04-16T02:30:10.252575\"},\"residual_correction\":0.01,\"ensemble_deltaT\":-4.65,\"DT\":0.01,\"slope_10\":-0.002,\"mean_diff_20_10\":0.0067,\"state_tag\":0.0}},\"experience_result\":{\"adjust_detail\":{\"deltaC1\":0,\"deltaC2\":0,\"deltaC3\":0},\"delta_C\":0,\"adjust_detail_jilu\":{\"deltaC1\":0,\"deltaC2\":0,\"deltaC3\":10},\"delta_C_jilu\":0},\"flag\":true,\"last_gas_sum_sv\":7360.0,\"result1\":{\"timestamp\":\"2026-04-16 10:30:00\",\"status\":\"normal\",\"send_reminder\":false,\"gas_setValue_transformer\":7350,\"gas_setValue_rnn\":7360,\"gas_deltaC_transformer\":0,\"temps\":{\"熔窑碹顶温度13\":{\"setValue\":1370.0,\"raw_transformer\":1363.721,\"raw_rnn\":1365.934,\"transformer_bias_corr\":1364.071,\"rnn_bias_corr\":1365.386,\"guarded_t_pred\":1364.071,\"guarded_l_pred\":1365.386,\"final_mean\":1365.39,\"trend_anchor\":1365.396,\"stable_anchor\":1365.443,\"future_trend_proxy\":0.0338,\"hist_trend_20\":-0.01,\"z_value_t\":-3.44,\"z_value_l\":-0.16,\"base_pred\":1365.155,\"base_pred_t\":1364.936,\"base_pred_l\":1365.375,\"ensemble_pred\":1365.181,\"ensemble_pred_t\":1364.936,\"ensemble_pred_l\":1365.375,\"oos_enabled\":false,\"oos_enabled_t\":false,\"oos_enabled_l\":false,\"oos_metrics\":{\"mae_base_ewm\":0.386982,\"mae_corrected_ewm\":0.3689,\"count\":20158,\"updated_at\":\"2026-04-16T02:30:12.025809\"},\"residual_correction\":0.0,\"residual_correction_t\":0.0,\"residual_correction_l\":0.0,\"ensemble_deltaT\":-4.82,\"ensemble_deltaT2\":-4.61,\"ensemble_deltaT_t\":-5.06,\"ensemble_deltaT_l\":-4.62,\"DT\":0.05,\"slope_10\":-0.0149,\"mean_diff_20_10\":-0.061,\"state_tag\":0.0,\"target_mode\":\"point\",\"target_time\":\"2026-04-16T11:50:00\"}},\"experience_result\":{\"adjust_detail\":{\"deltaC1\":0,\"deltaC1_EXP\":0},\"delta_C\":0,\"adjust_detail_jilu\":{\"deltaC1\":0,\"deltaC1_EXP\":0},\"delta_C_jilu\":0},\"model_result\":{\"deltaC1\":0,\"deltaC2\":0},\"flag\":true,\"last_gas_sum_sv\":7360.0},\"result3\":{\"timestamp\":\"2026-04-16 10:30:00\",\"status\":\"normal\",\"send_reminder\":false,\"temps\":{\"熔窑碹顶温度13\":{\"setValue\":1370.0,\"raw_rnn\":1365.934,\"rnn_bias_corr\":1365.386,\"guarded_l_pred\":1365.386,\"final_mean\":1365.39,\"trend_anchor\":1365.396,\"stable_anchor\":1365.443,\"future_trend_proxy\":0.0338,\"hist_trend_20\":-0.01,\"z_value_l\":-0.16,\"base_pred\":1365.326,\"ensemble_pred\":1365.326,\"oos_enabled\":false,\"oos_metrics\":{\"mae_base_ewm\":0.277767,\"mae_corrected_ewm\":0.277767,\"count\":10174,\"updated_at\":\"2026-04-16T02:30:13.781900\"},\"residual_correction\":0.0,\"ensemble_deltaT2\":-4.61,\"ensemble_deltaT\":-4.67,\"DT\":0.05,\"slope_10\":-0.0149,\"mean_diff_20_10\":-0.061,\"state_tag\":0.0,\"target_mode\":\"point\",\"target_time\":\"2026-04-16T11:50:00\"}},\"experience_result\":{\"adjust_detail\":{\"deltaC1\":0,\"deltaC1_EXP\":0},\"delta_C\":0,\"adjust_detail_jilu\":{\"deltaC1\":0,\"deltaC1_EXP\":0},\"delta_C_jilu\":0},\"flag\":true,\"last_gas_sum_sv\":7360.0},\"result5\":{\"timestamp\":\"2026-04-16 10:30:00\",\"status\":\"normal\",\"send_reminder\":false,\"temps\":{\"熔窑碹顶温度13\":{\"setValue\":1370.0,\"raw_rnn\":1364.618,\"final_mean\":1365.39,\"trend_anchor\":1365.396,\"stable_anchor\":1365.443,\"future_trend_proxy\":0.0338,\"hist_trend_20\":-0.01,\"base_pred\":1365.445,\"ensemble_pred\":1365.445,\"oos_enabled\":true,\"oos_metrics\":{\"mae_base_ewm\":0.156062,\"mae_corrected_ewm\":0.156062,\"count\":8766,\"updated_at\":\"2026-04-16T02:30:17.237624\"},\"residual_correction\":0.049,\"ensemble_deltaT2\":-4.61,\"ensemble_deltaT\":-4.56,\"DT\":0.05,\"slope_10\":-0.0149,\"mean_diff_20_10\":-0.061,\"state_tag\":0.0,\"target_mode\":\"point\",\"target_time\":\"2026-04-16T11:50:00\"}},\"experience_result\":{\"adjust_detail\":{\"deltaC1\":0,\"deltaC1_EXP\":0},\"delta_C\":0,\"adjust_detail_jilu\":{\"deltaC1\":0,\"deltaC1_EXP\":0},\"delta_C_jilu\":0},\"model_result\":{\"deltaC1\":0,\"deltaC2\":0},\"flag\":true,\"last_gas_sum_sv\":7360.0},\"result6\":{\"timestamp\":\"2026-04-16 10:30:00\",\"status\":\"normal\",\"send_reminder\":false,\"temps\":{\"熔窑碹顶温度13\":{\"setValue\":1370.0,\"raw_rnn\":1364.618,\"final_mean\":1365.39,\"trend_anchor\":1365.396,\"stable_anchor\":1365.443,\"future_trend_proxy\":0.0338,\"hist_trend_20\":-0.01,\"base_pred\":1365.445,\"ensemble_pred\":1365.445,\"oos_enabled\":true,\"oos_metrics\":{\"mae_base_ewm\":0.156062,\"mae_corrected_ewm\":0.156062,\"count\":8766,\"updated_at\":\"2026-04-16T02:30:17.237624\"},\"residual_correction\":0.049,\"ensemble_deltaT2\":-4.61,\"ensemble_deltaT\":-4.56,\"DT\":0.05,\"slope_10\":-0.0149,\"mean_diff_20_10\":-0.061,\"state_tag\":0.0,\"target_mode\":\"point\",\"target_time\":\"2026-04-16T11:50:00\"}},\"experience_result\":{\"adjust_detail\":{\"deltaC1\":0,\"deltaC1_EXP\":0},\"delta_C\":0,\"adjust_detail_jilu\":{\"deltaC1\":0,\"deltaC1_EXP\":0},\"delta_C_jilu\":0},\"model_result\":{\"deltaC1\":0,\"deltaC2\":0},\"flag\":true,\"last_gas_sum_sv\":7360.0},\"result7\":{\"timestamp\":\"2026-04-16 10:30:00\",\"status\":\"normal\",\"send_reminder\":false,\"temps\":{\"熔窑碹顶温度2\":{\"setValue\":1415.0,\"raw_rnn\":1416.726,\"final_mean\":1411.38,\"trend_anchor\":1411.421,\"pred_value\":1411.398,\"deltaT\":-3.602,\"DT\":0.06,\"slope_10\":-0.0039},\"熔窑碹顶温度6\":{\"setValue\":1542.0,\"raw_rnn\":1540.011,\"final_mean\":1539.91,\"trend_anchor\":1540.06,\"pred_value\":1539.993,\"deltaT\":-2.007,\"DT\":0.15,\"slope_10\":0.0056},\"熔窑碹顶温度13\":{\"setValue\":1370.0,\"raw_rnn\":1365.624,\"final_mean\":1365.39,\"trend_anchor\":1365.413,\"pred_value\":1365.49,\"deltaT\":-4.51,\"DT\":0.04,\"slope_10\":-0.0175}},\"experience_result\":{\"adjust_detail\":{\"deltaC1\":0,\"deltaC2\":0,\"deltaC3\":0},\"delta_C\":0,\"adjust_detail_jilu\":{\"deltaC1\":0,\"deltaC2\":0,\"deltaC3\":10},\"delta_C_jilu\":0},\"flag\":true,\"last_gas_sum_sv\":7360.0}},\"success\":true}";
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

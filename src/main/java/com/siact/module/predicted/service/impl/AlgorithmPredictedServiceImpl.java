package com.siact.module.predicted.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.siact.common.config.KilnProperty;
import com.siact.common.constant.ConstantBase;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.constant.ConstantTime;
import com.siact.common.exception.CustomException;
import com.siact.common.utils.TimeUtil;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.service.TplService;
import com.siact.module.base.vo.ControlIntervalConfigVO;
import com.siact.module.base.vo.TplVO;
import com.siact.module.control.dto.GasKeyCodeDTO;
import com.siact.module.control.entity.ExpertExperienceEntity;
import com.siact.module.control.entity.GasValueEntity;
import com.siact.module.control.entity.IntelligentComputingEntity;
import com.siact.module.control.mapper.ExpertExperienceMapper;
import com.siact.module.control.mapper.IntelligentComputingMapper;
import com.siact.module.control.service.GasValueService;
import com.siact.module.model.dto.*;
import com.siact.module.model.entity.AlgorithmCallInfoEntity;
import com.siact.module.model.entity.ModelConfigParamEntity;
import com.siact.module.model.entity.ModelInfoEntity;
import com.siact.module.model.entity.ModelPublishInfoEntity;
import com.siact.module.model.feign.AlgorithmFeign;
import com.siact.module.model.service.AlgorithmCallInfoService;
import com.siact.module.model.service.ModelConfigParamService;
import com.siact.module.model.service.ModelInfoService;
import com.siact.module.model.service.ModelPublishInfoService;
import com.siact.module.model.utils.AlgorithmDataCodeUtil;
import com.siact.module.predicted.dto.AlgorithmPredictionCallDataDTO;
import com.siact.module.predicted.dto.AlgorithmPredictionDataCodeTplDTO;
import com.siact.module.predicted.dto.AlgorithmPredictionDataParamsDTO;
import com.siact.module.predicted.entity.PredictedDataEntity;
import com.siact.module.predicted.enums.AlgorithmCallStatusEnum;
import com.siact.module.predicted.enums.PredictedTypeEnum;
import com.siact.module.predicted.service.AlgorithmPredictedService;
import com.siact.module.predicted.service.PredictedDataService;
import com.siact.module.process.enums.ProcessConfig;
import com.siact.module.process.service.IProcessLogService;
import com.siact.module.process.vo.ProcessLogVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AlgorithmPredictedServiceImpl implements AlgorithmPredictedService {

    @Autowired
    private TplService tplService;

    @Autowired
    private ModelInfoService modelInfoService;

    @Autowired
    private ModelConfigParamService modelConfigParamService;

    @Autowired
    private AlgorithmFeign algorithmFeign;

    @Autowired
    private AlgorithmCallInfoService algorithmCallInfoService;

    @Autowired
    private PredictedDataService predictedDataService;

    @Autowired
    private ModelPublishInfoService modelPublishInfoService;

    @Autowired
    private IProcessLogService processLogService;

    @Autowired
    private AlgorithmDataCodeUtil algorithmDataCodeUtil;

    @Autowired
    private IntelligentComputingMapper intelligentComputingMapper;

    @Autowired
    private ControlIntervalConfigService configService;

    @Autowired
    private ProcessConfig processConfig;

    private @Resource ExpertExperienceMapper expertExperienceMapper;

    private @Resource GasValueService gasValueService;

    private @Resource KilnProperty property;

    @Override
    public void algorithmInference() {

        String nowTimeStr = TimeUtil.getNowStr(ConstantTime.DATE_TIME_MM_00);

        // 1:读取模型数据当中的预测相关的dataCode MC1/MC5/MC10
        List<AlgorithmPredictionDataCodeTplDTO> predictionDataList = tplService.getListByCode("algorithmPredictionDataCode", AlgorithmPredictionDataCodeTplDTO.class);

        List<String> dataCodeList = predictionDataList.stream().map(AlgorithmPredictionDataCodeTplDTO::getDataCode).distinct().collect(Collectors.toList());

        // 1.2:过滤出已选择的模型数据
        List<ModelPublishInfoEntity> lastPublishInfoList = modelPublishInfoService.queryLastPublishInfoByDataCodeList(dataCodeList);
        String allSelectedModelIdList = lastPublishInfoList.stream().map(ModelPublishInfoEntity::getPublishModelInfoIds).collect(Collectors.joining(ConstantSymbol.COMMA));
        // 1.3: 根据配置的模型id查找模型信息
        List<ModelInfoEntity> modelInfoEntityList = modelInfoService.listByIds(Arrays.asList(allSelectedModelIdList.split(ConstantSymbol.COMMA)));
        // 1.4: 为防止调用算法失败,额外过滤出已完成回调的模型信息
        modelInfoEntityList = modelInfoEntityList.stream()
                .filter(o -> o.getAlgorithmCallStatus().equals(AlgorithmCallStatusEnum.SUCCESS.getStatus()))
                .collect(Collectors.toList());

        if (modelInfoEntityList.isEmpty()) {
            log.info("没有已完成回调的模型数据,nowTimeStr:{}", nowTimeStr);
            return;
        }

        // 1:构造模型调用的入参
        AlgorithmPublishModelParamDTO modelCallParamDTO = generateModelCallParam(dataCodeList, nowTimeStr, modelInfoEntityList);
        log.info("调用模型预测,入参:{}", JSON.toJSONString(modelCallParamDTO));

        // 2:调用 算法的预测接口  并 记录调用信息
        LinkedHashMap<String, Object> response = callAlgorithmInterFaceData(modelCallParamDTO);


        // 3:解析算法返回的数据  并 记录预测数据
        parseCallRespDataAndSavePredictionData(response, modelCallParamDTO, modelInfoEntityList, nowTimeStr);
    }

    @Override
    public void deleteAlgorithmCallInfoBeforeTime(String time) {
        if (StringUtils.isBlank(time)) {
            // 默认,删除当前时间前一个月的数据
            DateTime curTime = DateUtil.beginOfMonth(new Date());
            DateTime beforeMonthTime = DateUtil.offset(curTime, DateField.MONTH, -1);
            time = beforeMonthTime.toString(ConstantTime.DATE_TIME);
        }

        algorithmCallInfoService.deleteBeforeTime(time);
    }

    @Value("${algorithm.baseUrl}")
    private String baseUrl;

    @Override
    public void getIntelligentComputing() {
        // 将当前时间的秒 归0处理
        LocalDateTime now = LocalDateTime.now().withSecond(0);
        log.info("开始获取智能计算数据,执行时间:{}", now.format(TimeUtil.df));
        //组装参数
        JSONObject params = new JSONObject();

        //当前工艺对应换火周期
        params.put("fire_change_cycle", property.getConfig().getFireChangeCycle());
        params.put("model", "LightGBM2");
        params.put("method", "model");

        JSONObject intelligentComputingParams = JSONObject.parseObject(tplService.selectTplByCode("intelligentComputingParams").getTplContent());
        params.put("ts", intelligentComputingParams.getString("ts"));
        params.put("startTime", now.plusMinutes(-intelligentComputingParams.getInteger("tracingTime")).format(TimeUtil.df));
        params.put("endTime", now.format(TimeUtil.df));
        JSONObject data = new JSONObject();
        intelligentComputingParams.getJSONArray("keyData").forEach(o -> {
            JSONObject itemJson = JSONObject.from(o);
            data.put(itemJson.getString("algorithmCode"), itemJson.getString("dataCode"));
        });
        params.put("data", data);

        /* 模型调用变更, 去掉 last_deltaC 参数 */
        // 查出末尾25条数据,并根据createTime进行倒序排列
        // LambdaQueryWrapper<IntelligentComputingEntity> queryWrapper = new LambdaQueryWrapper<>();
        // queryWrapper.orderByDesc(IntelligentComputingEntity::getCreateTime);
        // queryWrapper.last("limit 25");
        // List<IntelligentComputingEntity> intelligentComputingEntities = intelligentComputingMapper.selectList(queryWrapper);

        // 倒序查询25条数据后, 过滤出createdTime % lastDeltaCInterval ==0 的数据 取后5条
        // Integer lastDeltaCInterval = intelligentComputingParams.getInteger("lastDeltaCInterval");
        // intelligentComputingEntities = intelligentComputingEntities.stream()
        //         .filter(o -> LocalDateTime.parse(o.getCreateTime(), TimeUtil.df).getMinute() % lastDeltaCInterval == 0)
        //         .limit(5)
        //         .collect(Collectors.toList());
        // log.info("间隔:{},过滤出的5条数据:{}", lastDeltaCInterval, intelligentComputingEntities.size());

        setDeltaC("MC1", params/* , intelligentComputingEntities */);
        setDeltaC("MC2", params/* , intelligentComputingEntities */);
        setDeltaC("MC3", params/* , intelligentComputingEntities */);
        setDeltaC("MC4", params/* , intelligentComputingEntities */);
        setDeltaC("MC5", params/* , intelligentComputingEntities */);
        setDeltaC("MC6", params/* , intelligentComputingEntities */);
        setDeltaC("MC7", params/* , intelligentComputingEntities */);
        setDeltaC("MC8", params/* , intelligentComputingEntities */);
        setDeltaC("MC9", params/* , intelligentComputingEntities */);
        setDeltaC("MC10", params/* , intelligentComputingEntities */);

        //调用接口
        String responseStr = null;
        JSONObject response;

        AlgorithmCallInfoEntity entity = new AlgorithmCallInfoEntity();
        long callId = IdWorker.getId(entity);

        entity.setId(callId);
        entity.setType("control");
        entity.setReqTime(TimeUtil.getNow());
        entity.setReqJson(JSONObject.toJSONString(params));
        entity.setCreateTime(new Date());
        // 异步保存算法调用记录
        new Thread(() -> {
            try {
                algorithmCallInfoService.save(entity);
            } catch (Exception e) {
                log.error("智控算法调用记录保存异常,入参:{},异常信息:{}", params, e.getMessage());
            }
        }).start();

        try {
            responseStr = HttpUtil.post(baseUrl + "/control", params.toJSONString(), 500000);
            // responseStr = "{\"code\":\"200\",\"status\":\"SUCCESS\",\"message\":\"控制任务执行成功\",\"result\":{\"Experience\":{\"MC1\":{\"mc_id\":1,\"timestamp\":\"2025-11-28T08:32:13.542121\",\"max_temp\":1383.9,\"min_temp\":1383.9,\"Max_Threshold\":1376.0,\"Min_Threshold\":1373.0,\"Control_Target\":1374.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1383.9,\"t1\":1383.9,\"t2\":1383.9,\"mean_temp\":1383.9},\"min_temp\":{\"t0_min\":1383.9,\"t1_min\":1383.8,\"t2_min\":1383.8,\"mean_temp\":1383.9},\"timestamp\":\"2025-11-28T08:32:13.793061\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1383.9,\"t1_max\":1383.9,\"t2_max\":1383.9,\"mean_temp_max\":1383.9,\"t0_min\":1383.9,\"t1_min\":1383.8,\"t2_min\":1383.8,\"mean_temp_min\":1383.9},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1383.9,\"t1_max\":1383.9,\"t2_max\":1383.9,\"mean_temp_max\":1383.9,\"t0_min\":1383.9,\"t1_min\":1383.8,\"t2_min\":1383.8,\"mean_temp_min\":1383.9},\"status\":\"other\"},\"MC2\":{\"mc_id\":2,\"timestamp\":\"2025-11-28T08:32:13.587252\",\"max_temp\":1492.5,\"min_temp\":1489.7,\"Max_Threshold\":1486.0,\"Min_Threshold\":1483.0,\"Control_Target\":1484.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1492.6,\"t1\":1492.5,\"t2\":1492.5,\"mean_temp\":1492.5},\"min_temp\":{\"t0_min\":1489.7,\"t1_min\":1489.6,\"t2_min\":1489.6,\"mean_temp\":1489.7},\"timestamp\":\"2025-11-28T08:32:13.887133\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1492.6,\"t1_max\":1492.5,\"t2_max\":1492.5,\"mean_temp_max\":1492.5,\"t0_min\":1489.7,\"t1_min\":1489.6,\"t2_min\":1489.6,\"mean_temp_min\":1489.7},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1492.6,\"t1_max\":1492.5,\"t2_max\":1492.5,\"mean_temp_max\":1492.5,\"t0_min\":1489.7,\"t1_min\":1489.6,\"t2_min\":1489.6,\"mean_temp_min\":1489.7},\"status\":\"other\"},\"MC3\":{\"mc_id\":3,\"timestamp\":\"2025-11-28T08:32:13.595955\",\"max_temp\":1539.8,\"min_temp\":1538.4,\"Max_Threshold\":1551.0,\"Min_Threshold\":1548.0,\"Control_Target\":1549.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1539.8,\"t1\":1539.8,\"t2\":1539.8,\"mean_temp\":1539.8},\"min_temp\":{\"t0_min\":1538.3,\"t1_min\":1538.4,\"t2_min\":1538.4,\"mean_temp\":1538.4},\"timestamp\":\"2025-11-28T08:32:13.944615\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1539.8,\"t1_max\":1539.8,\"t2_max\":1539.8,\"mean_temp_max\":1539.8,\"t0_min\":1538.3,\"t1_min\":1538.4,\"t2_min\":1538.4,\"mean_temp_min\":1538.4},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1539.8,\"t1_max\":1539.8,\"t2_max\":1539.8,\"mean_temp_max\":1539.8,\"t0_min\":1538.3,\"t1_min\":1538.4,\"t2_min\":1538.4,\"mean_temp_min\":1538.4},\"status\":\"other\"},\"MC4\":{\"mc_id\":4,\"timestamp\":\"2025-11-28T08:32:13.628962\",\"max_temp\":1586.5,\"min_temp\":1585.5,\"Max_Threshold\":1597.0,\"Min_Threshold\":1594.0,\"Control_Target\":1596.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1586.5,\"t1\":1586.5,\"t2\":1586.6,\"mean_temp\":1586.5},\"min_temp\":{\"t0_min\":1585.5,\"t1_min\":1585.5,\"t2_min\":1585.5,\"mean_temp\":1585.5},\"timestamp\":\"2025-11-28T08:32:14.009617\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1586.5,\"t1_max\":1586.5,\"t2_max\":1586.6,\"mean_temp_max\":1586.5,\"t0_min\":1585.5,\"t1_min\":1585.5,\"t2_min\":1585.5,\"mean_temp_min\":1585.5},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1586.5,\"t1_max\":1586.5,\"t2_max\":1586.6,\"mean_temp_max\":1586.5,\"t0_min\":1585.5,\"t1_min\":1585.5,\"t2_min\":1585.5,\"mean_temp_min\":1585.5},\"status\":\"other\"},\"MC5\":{\"mc_id\":5,\"timestamp\":\"2025-11-28T08:32:14.515739\",\"max_temp\":1587.3,\"min_temp\":1585.0,\"Max_Threshold\":1588.0,\"Min_Threshold\":1584.0,\"Control_Target\":1586.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1587.3,\"t1\":1587.2,\"t2\":1587.3,\"mean_temp\":1587.3},\"min_temp\":{\"t0_min\":1585.1,\"t1_min\":1584.9,\"t2_min\":1584.8,\"mean_temp\":1585.0},\"timestamp\":\"2025-11-28T08:32:14.601288\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1587.3,\"t1_max\":1587.2,\"t2_max\":1587.3,\"mean_temp_max\":1587.3,\"t0_min\":1585.1,\"t1_min\":1584.9,\"t2_min\":1584.8,\"mean_temp_min\":1585.0},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1587.3,\"t1_max\":1587.2,\"t2_max\":1587.3,\"mean_temp_max\":1587.3,\"t0_min\":1585.1,\"t1_min\":1584.9,\"t2_min\":1584.8,\"mean_temp_min\":1585.0},\"status\":\"normal\"},\"MC6\":{\"mc_id\":6,\"timestamp\":\"2025-11-28T08:32:14.569737\",\"max_temp\":1567.4,\"min_temp\":1566.1,\"Max_Threshold\":1567.0,\"Min_Threshold\":1565.0,\"Control_Target\":1566.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1567.4,\"t1\":1567.4,\"t2\":1567.5,\"mean_temp\":1567.4},\"min_temp\":{\"t0_min\":1566.0,\"t1_min\":1566.2,\"t2_min\":1566.1,\"mean_temp\":1566.1},\"timestamp\":\"2025-11-28T08:32:14.796939\"}],\"method1\":{\"delta_T\":-2.7,\"delta_C\":0,\"10min_pred_temp\":1563.3,\"t0_max\":1567.4,\"t1_max\":1567.4,\"t2_max\":1567.5,\"mean_temp_max\":1567.4,\"t0_min\":1566.0,\"t1_min\":1566.2,\"t2_min\":1566.1,\"mean_temp_min\":1566.1},\"method2\":{\"delta_T\":1.4,\"delta_C\":0,\"t0_max\":1567.4,\"t1_max\":1567.4,\"t2_max\":1567.5,\"mean_temp_max\":1567.4,\"t0_min\":1566.0,\"t1_min\":1566.2,\"t2_min\":1566.1,\"mean_temp_min\":1566.1},\"status\":\"over_temp\",\"history_correction_method1\":{\"init_adjust\":-6,\"last_deltaCs\":[0.0,0.0,0.0],\"weights\":[0.1,0.2,0.3],\"final_adjust\":-6.0},\"history_correction_method2\":{\"init_adjust\":-6,\"last_deltaCs\":[0.0,0.0,0.0],\"weights\":[0.1,0.2,0.3],\"final_adjust\":-6.0}},\"MC7\":{\"mc_id\":7,\"timestamp\":\"2025-11-28T08:32:14.592290\",\"max_temp\":1566.8,\"min_temp\":1566.0,\"Max_Threshold\":1567.0,\"Min_Threshold\":1565.0,\"Control_Target\":1566.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1566.7,\"t1\":1566.8,\"t2\":1566.8,\"mean_temp\":1566.8},\"min_temp\":{\"t0_min\":1566.1,\"t1_min\":1566.1,\"t2_min\":1566.0,\"mean_temp\":1566.0},\"timestamp\":\"2025-11-28T08:32:14.744928\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1566.7,\"t1_max\":1566.8,\"t2_max\":1566.8,\"mean_temp_max\":1566.8,\"t0_min\":1566.1,\"t1_min\":1566.1,\"t2_min\":1566.0,\"mean_temp_min\":1566.0},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1566.7,\"t1_max\":1566.8,\"t2_max\":1566.8,\"mean_temp_max\":1566.8,\"t0_min\":1566.1,\"t1_min\":1566.1,\"t2_min\":1566.0,\"mean_temp_min\":1566.0},\"status\":\"normal\"},\"MC8\":{\"mc_id\":8,\"timestamp\":\"2025-11-28T08:32:14.682289\",\"max_temp\":1540.9,\"min_temp\":1540.2,\"Max_Threshold\":1539.0,\"Min_Threshold\":1537.0,\"Control_Target\":1538.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1541.0,\"t1\":1540.8,\"t2\":1541.0,\"mean_temp\":1540.9},\"min_temp\":{\"t0_min\":1540.3,\"t1_min\":1540.2,\"t2_min\":1540.2,\"mean_temp\":1540.2},\"timestamp\":\"2025-11-28T08:32:14.977111\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1541.0,\"t1_max\":1540.8,\"t2_max\":1541.0,\"mean_temp_max\":1540.9,\"t0_min\":1540.3,\"t1_min\":1540.2,\"t2_min\":1540.2,\"mean_temp_min\":1540.2},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1541.0,\"t1_max\":1540.8,\"t2_max\":1541.0,\"mean_temp_max\":1540.9,\"t0_min\":1540.3,\"t1_min\":1540.2,\"t2_min\":1540.2,\"mean_temp_min\":1540.2},\"status\":\"other\"},\"MC9\":{\"mc_id\":9,\"timestamp\":\"2025-11-28T08:32:15.314902\",\"max_temp\":1501.6,\"min_temp\":1501.1,\"Max_Threshold\":1502.0,\"Min_Threshold\":1500.0,\"Control_Target\":1501.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1501.5,\"t1\":1501.7,\"t2\":1501.6,\"mean_temp\":1501.6},\"min_temp\":{\"t0_min\":1501.0,\"t1_min\":1501.1,\"t2_min\":1501.1,\"mean_temp\":1501.1},\"timestamp\":\"2025-11-28T08:32:15.385964\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1501.5,\"t1_max\":1501.7,\"t2_max\":1501.6,\"mean_temp_max\":1501.6,\"t0_min\":1501.0,\"t1_min\":1501.1,\"t2_min\":1501.1,\"mean_temp_min\":1501.1},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1501.5,\"t1_max\":1501.7,\"t2_max\":1501.6,\"mean_temp_max\":1501.6,\"t0_min\":1501.0,\"t1_min\":1501.1,\"t2_min\":1501.1,\"mean_temp_min\":1501.1},\"status\":\"normal\"},\"MC10\":{\"mc_id\":10,\"timestamp\":\"2025-11-28T08:32:15.407966\",\"max_temp\":1451.0,\"min_temp\":1450.3,\"Max_Threshold\":1453.0,\"Min_Threshold\":1452.0,\"Control_Target\":1452.5,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1451.0,\"t1\":1451.0,\"t2\":1451.0,\"mean_temp\":1451.0},\"min_temp\":{\"t0_min\":1450.3,\"t1_min\":1450.3,\"t2_min\":1450.4,\"mean_temp\":1450.3},\"timestamp\":\"2025-11-28T08:32:15.558111\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1451.0,\"t1_max\":1451.0,\"t2_max\":1451.0,\"mean_temp_max\":1451.0,\"t0_min\":1450.3,\"t1_min\":1450.3,\"t2_min\":1450.4,\"mean_temp_min\":1450.3},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1451.0,\"t1_max\":1451.0,\"t2_max\":1451.0,\"mean_temp_max\":1451.0,\"t0_min\":1450.3,\"t1_min\":1450.3,\"t2_min\":1450.4,\"mean_temp_min\":1450.3},\"status\":\"other\"},\"last_gasSetValue\":[900.0,981.0,986.0,1000.0,995.0,985.0,940.0,415.0]},\"Model\":{\"MC1\":{\"mc_id\":1,\"timestamp\":\"2025-11-28T08:32:13.542121\",\"max_temp\":1383.9,\"min_temp\":1383.9,\"Max_Threshold\":1376.0,\"Min_Threshold\":1373.0,\"Control_Target\":1374.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1383.9,\"t1\":1383.9,\"t2\":1383.9,\"mean_temp\":1383.9},\"min_temp\":{\"t0_min\":1383.9,\"t1_min\":1383.8,\"t2_min\":1383.8,\"mean_temp\":1383.9},\"timestamp\":\"2025-11-28T08:32:13.793061\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1383.9,\"t1_max\":1383.9,\"t2_max\":1383.9,\"mean_temp_max\":1383.9,\"t0_min\":1383.9,\"t1_min\":1383.8,\"t2_min\":1383.8,\"mean_temp_min\":1383.9},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1383.9,\"t1_max\":1383.9,\"t2_max\":1383.9,\"mean_temp_max\":1383.9,\"t0_min\":1383.9,\"t1_min\":1383.8,\"t2_min\":1383.8,\"mean_temp_min\":1383.9},\"status\":\"other\"},\"MC2\":{\"mc_id\":2,\"timestamp\":\"2025-11-28T08:32:13.587252\",\"max_temp\":1492.5,\"min_temp\":1489.7,\"Max_Threshold\":1486.0,\"Min_Threshold\":1483.0,\"Control_Target\":1484.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1492.6,\"t1\":1492.5,\"t2\":1492.5,\"mean_temp\":1492.5},\"min_temp\":{\"t0_min\":1489.7,\"t1_min\":1489.6,\"t2_min\":1489.6,\"mean_temp\":1489.7},\"timestamp\":\"2025-11-28T08:32:13.887133\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1492.6,\"t1_max\":1492.5,\"t2_max\":1492.5,\"mean_temp_max\":1492.5,\"t0_min\":1489.7,\"t1_min\":1489.6,\"t2_min\":1489.6,\"mean_temp_min\":1489.7},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1492.6,\"t1_max\":1492.5,\"t2_max\":1492.5,\"mean_temp_max\":1492.5,\"t0_min\":1489.7,\"t1_min\":1489.6,\"t2_min\":1489.6,\"mean_temp_min\":1489.7},\"status\":\"other\"},\"MC3\":{\"mc_id\":3,\"timestamp\":\"2025-11-28T08:32:13.595955\",\"max_temp\":1539.8,\"min_temp\":1538.4,\"Max_Threshold\":1551.0,\"Min_Threshold\":1548.0,\"Control_Target\":1549.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1539.8,\"t1\":1539.8,\"t2\":1539.8,\"mean_temp\":1539.8},\"min_temp\":{\"t0_min\":1538.3,\"t1_min\":1538.4,\"t2_min\":1538.4,\"mean_temp\":1538.4},\"timestamp\":\"2025-11-28T08:32:13.944615\"}],\"method1\":{\"delta_T\":null,\"delta_C\":-1,\"10min_pred_temp\":null,\"t0_max\":1539.8,\"t1_max\":1539.8,\"t2_max\":1539.8,\"mean_temp_max\":1539.8,\"t0_min\":1538.3,\"t1_min\":1538.4,\"t2_min\":1538.4,\"mean_temp_min\":1538.4},\"method2\":{\"delta_T\":null,\"delta_C\":-1,\"t0_max\":1539.8,\"t1_max\":1539.8,\"t2_max\":1539.8,\"mean_temp_max\":1539.8,\"t0_min\":1538.3,\"t1_min\":1538.4,\"t2_min\":1538.4,\"mean_temp_min\":1538.4},\"status\":\"other\"},\"MC4\":{\"mc_id\":4,\"timestamp\":\"2025-11-28T08:32:13.628962\",\"max_temp\":1586.5,\"min_temp\":1585.5,\"Max_Threshold\":1597.0,\"Min_Threshold\":1594.0,\"Control_Target\":1596.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1586.5,\"t1\":1586.5,\"t2\":1586.6,\"mean_temp\":1586.5},\"min_temp\":{\"t0_min\":1585.5,\"t1_min\":1585.5,\"t2_min\":1585.5,\"mean_temp\":1585.5},\"timestamp\":\"2025-11-28T08:32:14.009617\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1586.5,\"t1_max\":1586.5,\"t2_max\":1586.6,\"mean_temp_max\":1586.5,\"t0_min\":1585.5,\"t1_min\":1585.5,\"t2_min\":1585.5,\"mean_temp_min\":1585.5},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1586.5,\"t1_max\":1586.5,\"t2_max\":1586.6,\"mean_temp_max\":1586.5,\"t0_min\":1585.5,\"t1_min\":1585.5,\"t2_min\":1585.5,\"mean_temp_min\":1585.5},\"status\":\"other\"},\"MC5\":{\"mc_id\":5,\"timestamp\":\"2025-11-28T08:32:14.515739\",\"max_temp\":1587.3,\"min_temp\":1585.0,\"Max_Threshold\":1588.0,\"Min_Threshold\":1584.0,\"Control_Target\":1586.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1587.3,\"t1\":1587.2,\"t2\":1587.3,\"mean_temp\":1587.3},\"min_temp\":{\"t0_min\":1585.1,\"t1_min\":1584.9,\"t2_min\":1584.8,\"mean_temp\":1585.0},\"timestamp\":\"2025-11-28T08:32:14.601288\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1587.3,\"t1_max\":1587.2,\"t2_max\":1587.3,\"mean_temp_max\":1587.3,\"t0_min\":1585.1,\"t1_min\":1584.9,\"t2_min\":1584.8,\"mean_temp_min\":1585.0},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1587.3,\"t1_max\":1587.2,\"t2_max\":1587.3,\"mean_temp_max\":1587.3,\"t0_min\":1585.1,\"t1_min\":1584.9,\"t2_min\":1584.8,\"mean_temp_min\":1585.0},\"status\":\"normal\"},\"MC6\":{\"mc_id\":6,\"timestamp\":\"2025-11-28T08:32:14.569737\",\"max_temp\":1567.4,\"min_temp\":1566.1,\"Max_Threshold\":1567.0,\"Min_Threshold\":1565.0,\"Control_Target\":1566.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1567.4,\"t1\":1567.4,\"t2\":1567.5,\"mean_temp\":1567.4},\"min_temp\":{\"t0_min\":1566.0,\"t1_min\":1566.2,\"t2_min\":1566.1,\"mean_temp\":1566.1},\"timestamp\":\"2025-11-28T08:32:14.796939\"}],\"method1\":{\"delta_T\":-2.7,\"delta_C\":-6,\"10min_pred_temp\":1563.3,\"t0_max\":1567.4,\"t1_max\":1567.4,\"t2_max\":1567.5,\"mean_temp_max\":1567.4,\"t0_min\":1566.0,\"t1_min\":1566.2,\"t2_min\":1566.1,\"mean_temp_min\":1566.1},\"method2\":{\"delta_T\":1.4,\"delta_C\":-6,\"t0_max\":1567.4,\"t1_max\":1567.4,\"t2_max\":1567.5,\"mean_temp_max\":1567.4,\"t0_min\":1566.0,\"t1_min\":1566.2,\"t2_min\":1566.1,\"mean_temp_min\":1566.1},\"status\":\"over_temp\",\"history_correction_method1\":{\"init_adjust\":-6,\"last_deltaCs\":[0.0,0.0,0.0],\"weights\":[0.1,0.2,0.3],\"final_adjust\":-6.0},\"history_correction_method2\":{\"init_adjust\":-6,\"last_deltaCs\":[0.0,0.0,0.0],\"weights\":[0.1,0.2,0.3],\"final_adjust\":-6.0}},\"MC7\":{\"mc_id\":7,\"timestamp\":\"2025-11-28T08:32:14.592290\",\"max_temp\":1566.8,\"min_temp\":1566.0,\"Max_Threshold\":1567.0,\"Min_Threshold\":1565.0,\"Control_Target\":1566.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1566.7,\"t1\":1566.8,\"t2\":1566.8,\"mean_temp\":1566.8},\"min_temp\":{\"t0_min\":1566.1,\"t1_min\":1566.1,\"t2_min\":1566.0,\"mean_temp\":1566.0},\"timestamp\":\"2025-11-28T08:32:14.744928\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1566.7,\"t1_max\":1566.8,\"t2_max\":1566.8,\"mean_temp_max\":1566.8,\"t0_min\":1566.1,\"t1_min\":1566.1,\"t2_min\":1566.0,\"mean_temp_min\":1566.0},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1566.7,\"t1_max\":1566.8,\"t2_max\":1566.8,\"mean_temp_max\":1566.8,\"t0_min\":1566.1,\"t1_min\":1566.1,\"t2_min\":1566.0,\"mean_temp_min\":1566.0},\"status\":\"normal\"},\"MC8\":{\"mc_id\":8,\"timestamp\":\"2025-11-28T08:32:14.682289\",\"max_temp\":1540.9,\"min_temp\":1540.2,\"Max_Threshold\":1539.0,\"Min_Threshold\":1537.0,\"Control_Target\":1538.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1541.0,\"t1\":1540.8,\"t2\":1541.0,\"mean_temp\":1540.9},\"min_temp\":{\"t0_min\":1540.3,\"t1_min\":1540.2,\"t2_min\":1540.2,\"mean_temp\":1540.2},\"timestamp\":\"2025-11-28T08:32:14.977111\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1541.0,\"t1_max\":1540.8,\"t2_max\":1541.0,\"mean_temp_max\":1540.9,\"t0_min\":1540.3,\"t1_min\":1540.2,\"t2_min\":1540.2,\"mean_temp_min\":1540.2},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1541.0,\"t1_max\":1540.8,\"t2_max\":1541.0,\"mean_temp_max\":1540.9,\"t0_min\":1540.3,\"t1_min\":1540.2,\"t2_min\":1540.2,\"mean_temp_min\":1540.2},\"status\":\"other\"},\"MC9\":{\"mc_id\":9,\"timestamp\":\"2025-11-28T08:32:15.314902\",\"max_temp\":1501.6,\"min_temp\":1501.1,\"Max_Threshold\":1502.0,\"Min_Threshold\":1500.0,\"Control_Target\":1501.0,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1501.5,\"t1\":1501.7,\"t2\":1501.6,\"mean_temp\":1501.6},\"min_temp\":{\"t0_min\":1501.0,\"t1_min\":1501.1,\"t2_min\":1501.1,\"mean_temp\":1501.1},\"timestamp\":\"2025-11-28T08:32:15.385964\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1501.5,\"t1_max\":1501.7,\"t2_max\":1501.6,\"mean_temp_max\":1501.6,\"t0_min\":1501.0,\"t1_min\":1501.1,\"t2_min\":1501.1,\"mean_temp_min\":1501.1},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1501.5,\"t1_max\":1501.7,\"t2_max\":1501.6,\"mean_temp_max\":1501.6,\"t0_min\":1501.0,\"t1_min\":1501.1,\"t2_min\":1501.1,\"mean_temp_min\":1501.1},\"status\":\"normal\"},\"MC10\":{\"mc_id\":10,\"timestamp\":\"2025-11-28T08:32:15.407966\",\"max_temp\":1451.0,\"min_temp\":1450.3,\"Max_Threshold\":1453.0,\"Min_Threshold\":1452.0,\"Control_Target\":1452.5,\"predict_history\":[{\"attempt\":1,\"max_temp\":{\"t0\":1451.0,\"t1\":1451.0,\"t2\":1451.0,\"mean_temp\":1451.0},\"min_temp\":{\"t0_min\":1450.3,\"t1_min\":1450.3,\"t2_min\":1450.4,\"mean_temp\":1450.3},\"timestamp\":\"2025-11-28T08:32:15.558111\"}],\"method1\":{\"delta_T\":null,\"delta_C\":0,\"10min_pred_temp\":null,\"t0_max\":1451.0,\"t1_max\":1451.0,\"t2_max\":1451.0,\"mean_temp_max\":1451.0,\"t0_min\":1450.3,\"t1_min\":1450.3,\"t2_min\":1450.4,\"mean_temp_min\":1450.3},\"method2\":{\"delta_T\":null,\"delta_C\":0,\"t0_max\":1451.0,\"t1_max\":1451.0,\"t2_max\":1451.0,\"mean_temp_max\":1451.0,\"t0_min\":1450.3,\"t1_min\":1450.3,\"t2_min\":1450.4,\"mean_temp_min\":1450.3},\"status\":\"other\"},\"last_gasSetValue\":[900.0,981.0,986.0,1000.0,995.0,985.0,940.0,415.0]}},\"success\":true}";
            response = JSONObject.parseObject(responseStr);

            log.info("智控计算参数,:{},结果:{}", params, response);
            entity.setRespTime(TimeUtil.getNow());
            entity.setRespJson(JSON.toJSONString(response));
        } catch (Exception e) {
            log.error("智控计算参数异常,入参:{},响应:{}", params, responseStr, e);
            entity.setRespTime(TimeUtil.getNow());
            entity.setRespJson("出现异常:请求返回" + responseStr + ",异常信息:" + e.getMessage());
            return;
        } finally {
            algorithmCallInfoService.saveOrUpdate(entity);
        }

        //解析结果
        String code = response.getString("code");
        if (!"200".equals(code)) {
            log.error("智控计算参数异常,入参:{},响应:{}", params, response);
            entity.setRespTime(TimeUtil.getNow());
            entity.setRespJson(entity.getRespJson() + "出现异常:请求返回" + responseStr);
            algorithmCallInfoService.updateById(entity);
            return;
        }

        JSONObject result = response.getJSONObject("result");
        // 基于 model
        JSONObject result1Json = result.getJSONObject("Model");
        // 基于专家经验
        JSONObject result2Json = result.getJSONObject("Experience");

        IntelligentComputingEntity intelligentComputingEntity = new IntelligentComputingEntity();
        ExpertExperienceEntity expertExperienceEntity = new ExpertExperienceEntity();

        String createTime = now.format(TimeUtil.df);
        String resultTime = TimeUtil.getNow();

        intelligentComputingEntity.setCreateTime(createTime);
        intelligentComputingEntity.setResultTime(resultTime);
        intelligentComputingEntity.setMc1(getDeltaC("MC1", result1Json));
        intelligentComputingEntity.setMc2(getDeltaC("MC2", result1Json));
        intelligentComputingEntity.setMc3(getDeltaC("MC3", result1Json));
        intelligentComputingEntity.setMc4(getDeltaC("MC4", result1Json));
        intelligentComputingEntity.setMc5(getDeltaC("MC5", result1Json));
        intelligentComputingEntity.setMc6(getDeltaC("MC6", result1Json));
        intelligentComputingEntity.setMc7(getDeltaC("MC7", result1Json));
        intelligentComputingEntity.setMc8(getDeltaC("MC8", result1Json));
        intelligentComputingEntity.setMc9(getDeltaC("MC9", result1Json));
        intelligentComputingEntity.setMc10(getDeltaC("MC10", result1Json));
        intelligentComputingEntity.setData(result1Json.toJSONString());
        intelligentComputingMapper.insert(intelligentComputingEntity);


        expertExperienceEntity.setCreateTime(createTime);
        expertExperienceEntity.setResultTime(resultTime);
        expertExperienceEntity.setMc1(getDeltaC("MC1", result2Json));
        expertExperienceEntity.setMc2(getDeltaC("MC2", result2Json));
        expertExperienceEntity.setMc3(getDeltaC("MC3", result2Json));
        expertExperienceEntity.setMc4(getDeltaC("MC4", result2Json));
        expertExperienceEntity.setMc5(getDeltaC("MC5", result2Json));
        expertExperienceEntity.setMc6(getDeltaC("MC6", result2Json));
        expertExperienceEntity.setMc7(getDeltaC("MC7", result2Json));
        expertExperienceEntity.setMc8(getDeltaC("MC8", result2Json));
        expertExperienceEntity.setMc9(getDeltaC("MC9", result2Json));
        expertExperienceEntity.setMc10(getDeltaC("MC10", result2Json));
        expertExperienceEntity.setData(result2Json.toJSONString());
        expertExperienceMapper.insert(expertExperienceEntity);

        // 保存天然气数据
        JSONArray lastGasSetValue = result1Json.getJSONArray("last_gasSetValue");
        TplVO controlGasDataCode = tplService.selectTplByCode("controlGasDataCode");
        Map<String, GasKeyCodeDTO> gasKeyCodes = JSON.parseArray(controlGasDataCode.getTplContent(), GasKeyCodeDTO.class).stream().collect(Collectors.toMap(GasKeyCodeDTO::getKey, o -> o, (v1, v2) -> v1));

        List<GasValueEntity> gasValues = new ArrayList<>();
        for (int i = 0; i < lastGasSetValue.size(); i++) {
            GasKeyCodeDTO dto = gasKeyCodes.get("MC" + (i + 1));
            gasValues.add(GasValueEntity.builder()
                    .time(resultTime)
                    .dataKey(dto.getKey())
                    .dataCode(dto.getCode())
                    .gasValue(lastGasSetValue.getBigDecimal(i))
                    .build()
            );
        }
        gasValueService.saveBatch(gasValues);
    }

    private void setDeltaC(String mc, JSONObject params/* , List<IntelligentComputingEntity> intelligentComputingEntities */) {
        // JSONObject deltaCJson = new JSONObject();
        // deltaCJson.put("last10", getDeltaC(mc, intelligentComputingEntities.get(0)));
        // deltaCJson.put("last20", getDeltaC(mc, intelligentComputingEntities.get(1)));
        // deltaCJson.put("last30", getDeltaC(mc, intelligentComputingEntities.get(2)));
        // deltaCJson.put("last40", getDeltaC(mc, intelligentComputingEntities.get(3)));
        // deltaCJson.put("last50", getDeltaC(mc, intelligentComputingEntities.get(4)));
        // params.put(mc + "_last_deltaC", deltaCJson);

        ControlIntervalConfigVO controlIntervalConfigVO = new ControlIntervalConfigVO();
        controlIntervalConfigVO.setMeasurePoint(mc);
        ControlIntervalConfigDTO configDTO = configService.get(controlIntervalConfigVO);

        //MC温度上限
        params.put(mc + "_MAX_THRESHOLD", Double.valueOf(configDTO.getUpControl()));
        //MC温度下限
        params.put(mc + "_MIN_THRESHOLD", Double.valueOf(configDTO.getLowControl()));
        //MC控制目标
        params.put(mc + "_CONTROL_TARGET", Double.valueOf(configDTO.getTemperatureSet()));
    }

    // private JSONObject getDeltaC(String mc, IntelligentComputingEntity entity) {
    //     JSONObject jsonObject = new JSONObject();
    //     jsonObject.put("time", entity.getCreateTime());
    //
    //     //entity通过反射拼接get方法
    //     String method = "getMc" + mc.replace("MC", "");
    //     try {
    //         Method method1 = IntelligentComputingEntity.class.getMethod(method);
    //         jsonObject.put("delta_C", method1.invoke(entity));
    //     } catch (Exception e) {
    //         log.error("反射获取方法异常,method:{},entity:{}", method, entity, e);
    //     }
    //
    //     return jsonObject;
    // }

    private Double getDeltaC(String mc, JSONObject resultJson) {
        JSONObject json = resultJson.getJSONObject(mc);
        return json.getJSONObject("method2").getDouble("delta_C");
    }


    @NotNull
    private AlgorithmPublishModelParamDTO generateModelCallParam(List<String> dataCodeList, String nowTimeStr, List<ModelInfoEntity> modelInfoEntityList) {
        // 获取模型对应的参数配置
        List<ModelConfigParamEntity> modelConfigList = modelConfigParamService.queryValidParamEntityList(dataCodeList, null);

        Map<Long, ModelConfigParamEntity> paranInfoMap = modelConfigList.stream().collect(Collectors.toMap(ModelConfigParamEntity::getId, o -> o));

        // 查询当前时间的运行工况
        ProcessLogVO curTimeProcessLog = processLogService.queryByDate(nowTimeStr);
        String operatingCode = curTimeProcessLog.getOperatingCode();
        if (ObjectUtils.isEmpty(operatingCode)) {
            log.error("没有查询到当前时间对应的运行工况,nowTimeStr:{}", nowTimeStr);
            throw new CustomException("没有查询到当前时间对应的运行工况,nowTimeStr:" + nowTimeStr);
        }

        // 初始化调用算法的参数
        List<AlgorithmPublishModelParamDetailDTO> paramList = new ArrayList<>();

        // 遍历所有匹配到完成回调的模型
        for (ModelInfoEntity modelInfoEntity : modelInfoEntityList) {

            AlgorithmPublishModelParamDetailDTO detailParam = new AlgorithmPublishModelParamDetailDTO();

            Long modelId = modelInfoEntity.getId();
            detailParam.setModel_id(modelId + "");

            String modelName = modelInfoEntity.getModelName();
            detailParam.setModel_name(modelName);

            // 设置算法 如:bp,XGBoot,LSTM...
            String algorithmCode = modelInfoEntity.getAlgorithmCode();
            detailParam.setMethod(algorithmCode);

            Long configParamId = modelInfoEntity.getConfigParamId();

            String dataCode = modelInfoEntity.getDataCode();
            String predictedTypeCode = modelInfoEntity.getPredictedTypeCode();
            ModelConfigParamEntity configParamEntity = paranInfoMap.get(configParamId);

            if (configParamEntity == null) {
                log.error("模型参数配置不存在,dataCode:{},predictedTypeCode:{}", dataCode, predictedTypeCode);
                continue;
            }

            // 处理公共参数
            // data传的是 k:算法code  v:孪生code 的格式
            String publicSetting = configParamEntity.getPublicSetting();
            ModelConfigParamDTO publicParamDto = JSONObject.parseObject(publicSetting, ModelConfigParamDTO.class);
            List<AlgorithmDataCodeDTO> featuresDataCodeDtoList = modelConfigParamService.parsePublicParam(publicParamDto);
            Map<String, String> data = new HashMap<>();
            for (AlgorithmDataCodeDTO dto : featuresDataCodeDtoList) {
                data.put(dto.getAlgorithmCode(), dto.getDataCode());
            }
            detailParam.setData(data);

            // 取值为参数设置中开始结束滑块时间的长度
            List<ModelConfigParamDetailDTO> timeRange = publicParamDto.getRange();
            Map<String, Object> timeRangeCodeMap = timeRange.stream().collect(Collectors.toMap(ModelConfigParamDetailDTO::getParamCode, ModelConfigParamDetailDTO::getValue));

            Integer hisDataStartTime = (Integer) timeRangeCodeMap.get("hisDataStartTime");
            Integer hisDataEndTime = (Integer) timeRangeCodeMap.get("hisDataEndTime");

            detailParam.setRangeStart(hisDataStartTime);// 开始时间范围,单位是分钟
            detailParam.setRangeEnd(hisDataEndTime);// 结束时间范围,单位是分钟

            AlgorithmPredictionDataParamsDTO predictionDataParamsDTO =
                    tplService.getByCode("predictionDataParams", AlgorithmPredictionDataParamsDTO.class);

            detailParam.setSample(predictionDataParamsDTO.getSample());

            detailParam.setWork_code_num(processConfig.getProcessOneHotEncoder().size()); // 固定16种运行工况 ProcessOneHotEncoderEnum
            // 获取当前时间的运行工况
            detailParam.setWork_code(processConfig.getProcessAlgorithmCodeByType(operatingCode));

            PredictedTypeEnum predictedTypeEnum = PredictedTypeEnum.getEnumByCode(predictedTypeCode);
            if (predictedTypeEnum == null) {
                log.error("模型预测类型不存在,dataCode:{},predictedTypeCode:{}", dataCode, predictedTypeCode);
                continue;
            }

            detailParam.setType(predictedTypeEnum.getAlgorithmCode());
            detailParam.setFuture_number(predictedTypeEnum.getStep());


            // 处理算法配置参数
            String algorithmSetting = configParamEntity.getAlgorithmSetting();
            List<ModelConfigParamDetailDTO> algorithmParamDtoList = JSONArray.parseArray(algorithmSetting, ModelConfigParamDetailDTO.class);
            ModelConfigParamDetailDTO selectedAlgorithmConfig = algorithmParamDtoList.stream().filter(ModelConfigParamDetailDTO::getSelected).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(selectedAlgorithmConfig)) {
                throw new CustomException("未选中算法," + configParamEntity.getPredictedTypeCode());
            }

            // 设置需要计算的目标列
            // 需要计算的目标列  MC1~MC10  对应的算法code
            AlgorithmDataCodeDTO algorithmDataCodeDTO = algorithmDataCodeUtil.getByDataCode(configParamEntity.getDataCode());
            if (ObjectUtils.isEmpty(algorithmDataCodeDTO)) {
                throw new CustomException("未找到对应的算法数据code:" + configParamEntity.getDataCode());
            }
            detailParam.setTarget(algorithmDataCodeDTO.getAlgorithmCode());

            // 算法参数
            Map<String, Object> methodPar = new HashMap<>();
            for (ModelConfigParamDetailDTO detailDTO : selectedAlgorithmConfig.getParamList()) {
                String paramCode = detailDTO.getParamCode();
                methodPar.put(paramCode, detailDTO.getValue());
            }
            // 设置算法参数
            detailParam.setMethod_par(methodPar);

            paramList.add(detailParam);
        }

        AlgorithmPublishModelParamDTO modelParamDTO = new AlgorithmPublishModelParamDTO();
        modelParamDTO.setTime(nowTimeStr);
        modelParamDTO.setParams(paramList);
        return modelParamDTO;
    }

    @Nullable
    private LinkedHashMap<String, Object> callAlgorithmInterFaceData(AlgorithmPublishModelParamDTO modelCallParamDTO) {
        LinkedHashMap<String, Object> response = null;

        AlgorithmCallInfoEntity entity = new AlgorithmCallInfoEntity();
        long callId = IdWorker.getId(entity);
        entity.setId(callId);
        entity.setType("inference");
        entity.setReqTime(TimeUtil.getNow());
        entity.setReqJson(JSON.toJSONString(modelCallParamDTO));
        entity.setCreateTime(new Date());

        try {
            response = algorithmFeign.inference(modelCallParamDTO);
            log.info("调用模型预测,:{},结果:{}", modelCallParamDTO, response);
            entity.setRespTime(TimeUtil.getNow());
            entity.setRespJson(JSON.toJSONString(response));
        } catch (Exception e) {
            log.error("调用模型预测异常,入参:{},响应:{}", modelCallParamDTO, response, e);
            entity.setRespTime(TimeUtil.getNow());
            entity.setRespJson("出现异常:请求返回" + JSON.toJSONString(response) + ",异常信息:" + e.getMessage());
            return null;
        } finally {
            algorithmCallInfoService.save(entity);
        }

        // 3:解析预测结果
        String code = response.get("code").toString();
        if (!"200".equals(code)) {
            log.error("调用模型预测异常,入参:{},响应:{}", modelCallParamDTO, response);
            entity.setRespTime(TimeUtil.getNow());
            entity.setRespJson(entity.getRespJson() + "出现异常:请求返回" + JSON.toJSONString(response));
            algorithmCallInfoService.updateById(entity);
            return null;
        }
        return response;
    }

    private void parseCallRespDataAndSavePredictionData(LinkedHashMap<String, Object> response, AlgorithmPublishModelParamDTO modelCallParamDTO, List<ModelInfoEntity> modelInfoEntityList, String nowTimeStr) {
        if (response == null || response.get("result") == null) {
            log.error("调用模型无预测数据,入参:{},算法响应:{}", modelCallParamDTO, response);
            return;
        }
        Object result = response.get("result");

        AlgorithmPredictionCallDataDTO callDataInfo = JSONObject.parseObject(JSON.toJSONString(result), AlgorithmPredictionCallDataDTO.class);

        String predictionTime = callDataInfo.getTime();
        Map<String, List<BigDecimal>> callDataMap = callDataInfo.getResult();
        if (ObjectUtils.isEmpty(callDataMap)) {
            log.error("调用模型无预测数据,入参:{},算法响应:{}", modelCallParamDTO, response);
            return;
        }

        List<PredictedDataEntity> predictedDataList = new ArrayList<>();
        for (ModelInfoEntity modelInfoEntity : modelInfoEntityList) {

            List<BigDecimal> dataValList = callDataMap.get(modelInfoEntity.getId() + "");
            if (ObjectUtils.isEmpty(dataValList)) {
                log.error("模型无预测数据,modelId:{},nowTimeStr:{},callDataInfo:{}", modelInfoEntity.getId(), nowTimeStr, callDataInfo);
                continue;
            }

            // 当前模型如果是非多步  则取第一个预测结果
            String predictedTypeCode = modelInfoEntity.getPredictedTypeCode();
            PredictedTypeEnum predictedTypeEnum = PredictedTypeEnum.getEnumByCode(predictedTypeCode);
            if (predictedTypeEnum == null) {
                log.error("模型预测类型不存在,modelId:{},predictedTypeCode:{},nowTimeStr:{}", modelInfoEntity.getId(), predictedTypeCode, nowTimeStr);
                continue;
            }
            if (PredictedTypeEnum.singleType().equals(predictedTypeEnum.getType())) {
                // 当前模型是单步预测 则取第一个预测结果 数据时间为下一个时间点
                String dataTime = TimeUtil.getCalcTime(predictionTime, predictedTypeEnum.getStep(), ConstantBase.MIN);
                BigDecimal curDataVal = dataValList.get(0);
                predictedDataList.add(new PredictedDataEntity(null, modelInfoEntity.getDataCode(), predictedTypeEnum.getType(), predictedTypeEnum.getCode(), dataTime, curDataVal, "℃", new Date()));
            } else {
                // 当前模型是多步预测  则取多步预测的步长的预测结果
                for (int i = 0; i < predictedTypeEnum.getStep() * 2; i++) {

                    // ps:目前多步预测返回的是160个数据  其中 30s一个点 ,但是数据库当中 1min一个点,因此只保留偶数索引的点
                    if (i % 2 != 0) {
                        continue;
                    }
                    String dataTime = TimeUtil.getCalcTime(predictionTime, i / 2, ConstantBase.MIN);
                    BigDecimal curDataVal = dataValList.get(i);
                    predictedDataList.add(new PredictedDataEntity(null, modelInfoEntity.getDataCode(), predictedTypeEnum.getType(), predictedTypeEnum.getCode(), dataTime, curDataVal, "℃", new Date()));
                }
            }
        }

        // 3:保存/更新数据表(同时间点进行覆盖  单步覆盖单步  多步覆盖多步  即 根据typeCode进行 和 time进行覆盖)
        saveOrUpdateBatchByAlgorithmResult(predictedDataList);
    }

    /**
     * 根据算法结果,批量保存/更新数据
     *
     * @param predictedDataList
     */
    private void saveOrUpdateBatchByAlgorithmResult(List<PredictedDataEntity> predictedDataList) {
        // 1. 根据业务键查询已存在的记录
        List<PredictedDataEntity> existingList = predictedDataService.list(new LambdaQueryWrapper<PredictedDataEntity>()
                .in(PredictedDataEntity::getDataCode, predictedDataList.stream().map(PredictedDataEntity::getDataCode).distinct().collect(Collectors.toList()))
                .in(PredictedDataEntity::getPredictedTypeCode, predictedDataList.stream().map(PredictedDataEntity::getPredictedTypeCode).distinct().collect(Collectors.toList()))
                .in(PredictedDataEntity::getTime, predictedDataList.stream().map(PredictedDataEntity::getTime).distinct().collect(Collectors.toList())));

        // 2. 构建业务键到实体的映射
        Map<String, PredictedDataEntity> existingMap = existingList.stream()
                .collect(Collectors.toMap(
                        entity -> entity.getDataCode() + "_" + entity.getPredictedTypeCode() + "_" + entity.getTime(),
                        entity -> entity,
                        (e1, e2) -> e1 // 处理重复键
                ));

        // 3. 分离需要插入和更新的数据
        List<PredictedDataEntity> insertList = new ArrayList<>();
        List<PredictedDataEntity> updateList = new ArrayList<>();
        for (PredictedDataEntity entity : predictedDataList) {
            String existingKey = entity.getDataCode() + "_" + entity.getPredictedTypeCode() + "_" + entity.getTime();
            if (existingMap.containsKey(existingKey)) {
                // 设置ID用于更新
                entity.setId(existingMap.get(existingKey).getId());
                updateList.add(entity);
            } else {
                insertList.add(entity);
            }
        }
        // 4. 批量操作
        if (!insertList.isEmpty()) {
            predictedDataService.saveBatch(insertList, 1000);
        }
        if (!updateList.isEmpty()) {
            predictedDataService.updateBatchById(updateList, 1000);
        }
    }

    /**
     * 初始化intelligentComputing表的create_time字段
     * 逻辑 result_time向前取距离最近的5分钟的时间点
     */
    public void initIntelligentComputingCreateTime(Boolean isForce) {
        // 1. 查询所有result_time
        LambdaQueryWrapper<IntelligentComputingEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (isForce == null || !isForce) {
            // 非强制只补充没有create_time的记录
            queryWrapper.isNull(IntelligentComputingEntity::getCreateTime);
        }

        List<IntelligentComputingEntity> intelligentComputingList = intelligentComputingMapper.selectList(queryWrapper);
        if (ObjectUtils.isEmpty(intelligentComputingList)) {
            return;
        }

        // 2. 遍历更新create_time
        for (IntelligentComputingEntity entity : intelligentComputingList) {
            String resultTime = entity.getResultTime();
            if (ObjectUtils.isEmpty(resultTime)) {
                continue;
            }
            // 向前取距离最近的5分钟的时间点
            String createTime = getNearest5MinuteTimeForward(resultTime);
            entity.setCreateTime(createTime);
        }

        // 3. 批量更新
        if (!intelligentComputingList.isEmpty()) {
            for (IntelligentComputingEntity computingEntity : intelligentComputingList) {
                intelligentComputingMapper.updateById(computingEntity);
            }
        }

    }

    /**
     * 计算离指定时间最近的5分钟时间点（只向前取）
     *
     * @param timeString 时间字符串，格式为 "yyyy-MM-dd HH:mm:ss"
     * @return 最近的5分钟时间点（向前取整）
     */
    public String getNearest5MinuteTimeForward(String timeString) {
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 解析时间字符串
        LocalDateTime time = LocalDateTime.parse(timeString, formatter);

        // 获取分钟数
        int minute = time.getMinute();

        // 计算向下取整到最近的5分钟间隔（只向前取）
        int roundedMinute = (minute / 5) * 5;

        // 设置为计算出的分钟数，并将秒和纳秒置为0
        time = time.withMinute(roundedMinute).withSecond(0);
        return time.format(TimeUtil.df);
    }
}

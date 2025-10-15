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
import com.siact.common.constant.ConstantBase;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.constant.ConstantTime;
import com.siact.common.exception.CustomException;
import com.siact.common.utils.TimeUtil;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.service.TplService;
import com.siact.module.base.vo.ControlIntervalConfigVO;
import com.siact.module.control.entity.IntelligentComputingEntity;
import com.siact.module.control.mapper.IntelligentComputingMapper;
import com.siact.module.model.dto.AlgorithmDataCodeDTO;
import com.siact.module.model.dto.AlgorithmPublishModelParamDTO;
import com.siact.module.model.dto.AlgorithmPublishModelParamDetailDTO;
import com.siact.module.model.dto.ModelConfigParamDTO;
import com.siact.module.model.dto.ModelConfigParamDetailDTO;
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
import com.siact.module.predicted.entity.PredictedDataEntity;
import com.siact.module.predicted.enums.AlgorithmCallStatusEnum;
import com.siact.module.predicted.enums.PredictedTypeEnum;
import com.siact.module.predicted.service.AlgorithmPredictedService;
import com.siact.module.predicted.service.PredictedDataService;
import com.siact.module.process.enums.ProcessOneHotEncoderEnum;
import com.siact.module.process.service.IProcessLogService;
import com.siact.module.process.vo.ProcessLogVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        //组装参数
        JSONObject params = new JSONObject();

        //当前工艺对应换火周期
        params.put("fire_change_cycle", 24);
        params.put("model", "LightGBM2");
        params.put("method", "model");

        JSONObject intelligentComputingParams = JSONObject.parseObject(tplService.selectTplByCode("intelligentComputingParams").getTplContent());
        params.put("ts", intelligentComputingParams.getString("ts"));
        LocalDateTime now = LocalDateTime.now();
        params.put("startTime", now.plusMinutes(-intelligentComputingParams.getInteger("tracingTime")).format(TimeUtil.df));
        params.put("endTime", now.format(TimeUtil.df));
        JSONObject data = new JSONObject();
        intelligentComputingParams.getJSONArray("keyData").forEach(o -> {
            JSONObject itemJson = JSONObject.from(o);
            data.put(itemJson.getString("algorithmCode"), itemJson.getString("dataCode"));
        });
        params.put("data", data);

        LambdaQueryWrapper<IntelligentComputingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(IntelligentComputingEntity::getResultTime);
        queryWrapper.last("limit 7");
        List<IntelligentComputingEntity> intelligentComputingEntities = intelligentComputingMapper.selectList(queryWrapper);
        setDeltaC("MC1", params, intelligentComputingEntities);
        setDeltaC("MC2", params, intelligentComputingEntities);
        setDeltaC("MC3", params, intelligentComputingEntities);
        setDeltaC("MC4", params, intelligentComputingEntities);
        setDeltaC("MC5", params, intelligentComputingEntities);
        setDeltaC("MC6", params, intelligentComputingEntities);
        setDeltaC("MC7", params, intelligentComputingEntities);
        setDeltaC("MC8", params, intelligentComputingEntities);
        setDeltaC("MC9", params, intelligentComputingEntities);
        setDeltaC("MC10", params, intelligentComputingEntities);


        //调用接口
//        LinkedHashMap<String, Object> response = null;
        String responseStr = null;
        JSONObject response = new JSONObject();
        AlgorithmCallInfoEntity entity = new AlgorithmCallInfoEntity();
        long callId = IdWorker.getId(entity);
        entity.setId(callId);
        entity.setType("control");
        entity.setReqTime(TimeUtil.getNow());
        entity.setReqJson(JSONObject.toJSONString(params));
        entity.setCreateTime(new Date());

        try {
            responseStr = HttpUtil.post(baseUrl + "/control", params.toJSONString(), 500000);
            response = JSONObject.parseObject(responseStr);

            log.info("智控计算参数,:{},结果:{}", params, response);
            entity.setRespTime(TimeUtil.getNow());
            entity.setRespJson(JSON.toJSONString(response));
        } catch (Exception e) {
            log.error("智控计算参数异常,入参:{},响应:{}", params, responseStr, e);
            entity.setRespTime(TimeUtil.getNow());
            entity.setRespJson("出现异常:请求返回" + responseStr + ",异常信息:" + e.getMessage());
            return ;
        } finally {
            algorithmCallInfoService.save(entity);
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

        JSONObject resultJson = response.getJSONObject("result");
        IntelligentComputingEntity intelligentComputingEntity = new IntelligentComputingEntity();

        intelligentComputingEntity.setResultTime(TimeUtil.getNow());
        intelligentComputingEntity.setMc1(getDeltaC("MC1", resultJson));
        intelligentComputingEntity.setMc2(getDeltaC("MC2", resultJson));
        intelligentComputingEntity.setMc3(getDeltaC("MC3", resultJson));
        intelligentComputingEntity.setMc4(getDeltaC("MC4", resultJson));
        intelligentComputingEntity.setMc5(getDeltaC("MC5", resultJson));
        intelligentComputingEntity.setMc6(getDeltaC("MC6", resultJson));
        intelligentComputingEntity.setMc7(getDeltaC("MC7", resultJson));
        intelligentComputingEntity.setMc8(getDeltaC("MC8", resultJson));
        intelligentComputingEntity.setMc9(getDeltaC("MC9", resultJson));
        intelligentComputingEntity.setMc10(getDeltaC("MC10", resultJson));
        intelligentComputingEntity.setData(resultJson.toJSONString());

        intelligentComputingMapper.insert(intelligentComputingEntity);
    }

    private void setDeltaC(String mc, JSONObject params, List<IntelligentComputingEntity> intelligentComputingEntities) {
        JSONObject deltaCJson = new JSONObject();
        deltaCJson.put("last10", getDeltaC(mc,intelligentComputingEntities.get(0)));
        deltaCJson.put("last20", getDeltaC(mc,intelligentComputingEntities.get(1)));
        deltaCJson.put("last30", getDeltaC(mc,intelligentComputingEntities.get(2)));
        deltaCJson.put("last40", getDeltaC(mc,intelligentComputingEntities.get(3)));
        deltaCJson.put("last50", getDeltaC(mc,intelligentComputingEntities.get(4)));
        deltaCJson.put("last60", getDeltaC(mc,intelligentComputingEntities.get(5)));
        deltaCJson.put("last70", getDeltaC(mc,intelligentComputingEntities.get(6)));
        params.put(mc + "_last_deltaC", deltaCJson);

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

    private JSONObject getDeltaC(String mc, IntelligentComputingEntity entity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("time", entity.getResultTime());

        //entity通过反射拼接get方法
        String method = "getMc" + mc.replace("MC", "");
        try {
            Method method1 = IntelligentComputingEntity.class.getMethod(method);
            jsonObject.put("delta_C", method1.invoke(entity));
        } catch (Exception e) {
            log.error("反射获取方法异常,method:{},entity:{}", method, entity, e);
        }

        return jsonObject;
    }

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

            // TODO 这段代码可能改成根据前端入参进行处理
            int sampleTime = 60;
            String sampleUnit = "s";
            detailParam.setSample(sampleTime + sampleUnit.toLowerCase());

            detailParam.setWork_code_num(ProcessOneHotEncoderEnum.values().length); // 固定12种运行工况 ProcessOneHotEncoderEnum
            // 获取当前时间的运行工况
            detailParam.setWork_code(ProcessOneHotEncoderEnum.getAlgorithmCodeByType(operatingCode));

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
                for (int i = 0; i < predictedTypeEnum.getStep(); i++) {
                    String dataTime = TimeUtil.getCalcTime(predictionTime, i, ConstantBase.MIN);
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
}

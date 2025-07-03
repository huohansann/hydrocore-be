package com.siact.module.predicted.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.siact.common.constant.ConstantBase;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.constant.ConstantTime;
import com.siact.common.utils.TimeUtil;
import com.siact.module.base.service.TplService;
import com.siact.module.model.dto.AlgorithmDataCodeDTO;
import com.siact.module.model.dto.AlgorithmPublishModelParamDTO;
import com.siact.module.model.dto.AlgorithmPublishModelParamDetailDTO;
import com.siact.module.model.dto.ModelConfigParamDTO;
import com.siact.module.model.entity.AlgorithmCallInfoEntity;
import com.siact.module.model.entity.ModelConfigParamEntity;
import com.siact.module.model.entity.ModelInfoEntity;
import com.siact.module.model.entity.ModelPublishInfoEntity;
import com.siact.module.model.feign.AlgorithmFeign;
import com.siact.module.model.service.AlgorithmCallInfoService;
import com.siact.module.model.service.ModelConfigParamService;
import com.siact.module.model.service.ModelInfoService;
import com.siact.module.model.service.ModelPublishInfoService;
import com.siact.module.predicted.dto.AlgorithmPredictionCallDataDTO;
import com.siact.module.predicted.dto.AlgorithmPredictionDataCodeTplDTO;
import com.siact.module.predicted.entity.PredictedDataEntity;
import com.siact.module.predicted.enums.AlgorithmCallStatusEnum;
import com.siact.module.predicted.enums.PredictedTypeEnum;
import com.siact.module.predicted.service.PredictedDataService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
public class AlgorithmTask {

    // 是否开启算法预测,默认false
    @Value("${algorithm.prediction.enable:false}")
    private Boolean algorithmPredictionEnable;

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


    /**
     * 每分钟调用一次算法  获取预测数据
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void algorithmInference() {
        if (!algorithmPredictionEnable) {
            log.info("算法预测未开启");
            return;
        }

        String nowTimeStr = TimeUtil.getNowStr(ConstantTime.DATE_TIME_MM_00);

        // 1:读取模型数据当中的预测相关的模型
        List<AlgorithmPredictionDataCodeTplDTO> predictionDataList = tplService.getListByCode("algorithmPredictionDataCode", AlgorithmPredictionDataCodeTplDTO.class);

        List<String> dataCodeList = predictionDataList.stream().map(AlgorithmPredictionDataCodeTplDTO::getDataCode).distinct().collect(Collectors.toList());

        List<ModelInfoEntity> modelInfoEntityList = modelInfoService.queryModelByDataCodeAndPredictedTypeCodes(dataCodeList, null, AlgorithmCallStatusEnum.SUCCESS.getStatus());
        if (modelInfoEntityList.isEmpty()) {
            log.info("没有已完成回调的模型数据,nowTimeStr:{}", nowTimeStr);
            return;
        }

        // 1.2:过滤出已选择的模型数据
        List<ModelPublishInfoEntity> lastPublishInfoList = modelPublishInfoService.queryLastPublishInfoByDataCodeList(dataCodeList);
        String allSelectedModelIdList = lastPublishInfoList.stream().map(ModelPublishInfoEntity::getPublishModelInfoIds).collect(Collectors.joining(ConstantSymbol.COMMA));

        modelInfoEntityList = modelInfoEntityList.stream()
                .filter(modelInfoEntity -> allSelectedModelIdList.contains( modelInfoEntity.getId() + ""))
                .collect(Collectors.toList());
        if (modelInfoEntityList.isEmpty()) {
            log.info("没有已选择的模型数据,nowTimeStr:{}", nowTimeStr);
            return;
        }

        // 1:构造模型调用的入参
        AlgorithmPublishModelParamDTO modelCallParamDTO = generateModelCallParam(dataCodeList, nowTimeStr, modelInfoEntityList);
        log.info("调用模型预测,入参:{}", modelCallParamDTO);

        // 2:调用 算法的预测接口  并 记录调用信息
        LinkedHashMap<String, Object> response = callAlgorithmInterFaceData(modelCallParamDTO);


        // 3:解析算法返回的数据  并 记录预测数据
        parseCallRespDataAndSavePredictionData(response, modelCallParamDTO, modelInfoEntityList, nowTimeStr);
    }

    @NotNull
    private AlgorithmPublishModelParamDTO generateModelCallParam(List<String> dataCodeList, String nowTimeStr, List<ModelInfoEntity> modelInfoEntityList) {
        // 获取模型对应的参数配置
        List<ModelConfigParamEntity> modelConfigList = modelConfigParamService.queryValidParamEntityList(dataCodeList, null);

        Map<Long, ModelConfigParamEntity> paranInfoMap = modelConfigList.stream().collect(Collectors.toMap(ModelConfigParamEntity::getId, o -> o));

        // 所有dataCode 中所有选中的模型
        List<AlgorithmPublishModelParamDetailDTO> paramList = new ArrayList<>();
        AlgorithmPublishModelParamDTO modelParamDTO = new AlgorithmPublishModelParamDTO();
        modelParamDTO.setTime(nowTimeStr);
        modelParamDTO.setParams(paramList);

        for (ModelInfoEntity modelInfoEntity : modelInfoEntityList) {

            AlgorithmPublishModelParamDetailDTO detailParam = new AlgorithmPublishModelParamDetailDTO();

            Long modelId = modelInfoEntity.getId();
            detailParam.setModel_id(modelId + "");

            String modelName = modelInfoEntity.getModelName();
//            modelName = "111"; // TODO 测试数据 删除
            detailParam.setModel_name(modelName);

            String algorithmCode = modelInfoEntity.getAlgorithmCode();
            detailParam.setMethod(algorithmCode);

            Long configParamId = modelInfoEntity.getConfigParamId();

            String dataCode = modelInfoEntity.getDataCode();
            String predictedTypeCode = modelInfoEntity.getPredictedTypeCode();
//            configParamId = 1939854806733119490L; // TODO 测试数据 删除
            ModelConfigParamEntity configParamEntity = paranInfoMap.get(configParamId);

            if (configParamEntity == null) {
                log.error("模型参数配置不存在,dataCode:{},predictedTypeCode:{}", dataCode, predictedTypeCode);
                continue;
            }
            String publicSetting = configParamEntity.getPublicSetting();
            ModelConfigParamDTO publicParamDto = JSONObject.parseObject(publicSetting, ModelConfigParamDTO.class);
            List<AlgorithmDataCodeDTO> featuresDataCodeDtoList = modelConfigParamService.parsePublicParam(publicParamDto);
            Map<String, String> data = new HashMap<>();
            for (AlgorithmDataCodeDTO dto : featuresDataCodeDtoList) {
                data.put(dto.getAlgorithmCode(), dto.getDataCode());
            }
            detailParam.setData(data);

            detailParam.setWork_code_num(9);
            detailParam.setWork_code(9);

            PredictedTypeEnum predictedTypeEnum = PredictedTypeEnum.getEnumByCode(predictedTypeCode);
            if (predictedTypeEnum == null) {
                log.error("模型预测类型不存在,dataCode:{},predictedTypeCode:{}", dataCode, predictedTypeCode);
                continue;
            }
            detailParam.setStep(predictedTypeEnum.getStep() * 60);// 单位是秒
            paramList.add(detailParam);
        }
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
        Object result = response.get("result");
        if (result == null) {
            log.error("调用模型无预测数据,入参:{},响应:{}", modelCallParamDTO, response);
            return;
        }

        AlgorithmPredictionCallDataDTO callDataInfo = JSONObject.parseObject(JSON.toJSONString(result), AlgorithmPredictionCallDataDTO.class);

        String predictionTime = callDataInfo.getTime();
        Map<String, List<BigDecimal>> callDataMap = callDataInfo.getResult();

        List<PredictedDataEntity> predictedDataList = new ArrayList<>();
        for (ModelInfoEntity modelInfoEntity : modelInfoEntityList) {

            List<BigDecimal> dataValList = callDataMap.get(modelInfoEntity.getId() + "");

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
                for (int i = 1; i <= predictedTypeEnum.getStep(); i++) {
                    String dataTime = TimeUtil.getCalcTime(predictionTime, i, ConstantBase.MIN);
                    BigDecimal curDataVal = dataValList.get(i);
                    predictedDataList.add(new PredictedDataEntity(null, modelInfoEntity.getDataCode(), predictedTypeEnum.getType(), predictedTypeEnum.getCode(), dataTime, curDataVal, "℃", new Date()));
                }
            }
        }

        // 3:保存/更新数据表(同时间点进行覆盖  单步覆盖单步  多步覆盖多步  即 根据typeCode进行 和 time进行覆盖)
        for (PredictedDataEntity entity : predictedDataList) {
            LambdaQueryWrapper<PredictedDataEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PredictedDataEntity::getDataCode, entity.getDataCode());
            queryWrapper.eq(PredictedDataEntity::getPredictedTypeCode, entity.getPredictedTypeCode());
            queryWrapper.eq(PredictedDataEntity::getTime, entity.getTime());
            predictedDataService.saveOrUpdate(entity, queryWrapper);
        }
    }
}

package com.siact.module.model.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.utils.TimeUtil;
import com.siact.module.model.dto.AlgorithmCallBackModelEvaluationInfoDetailDTO;
import com.siact.module.model.dto.AlgorithmCallBackModelInfoDTO;
import com.siact.module.model.entity.AlgorithmCallInfoEntity;
import com.siact.module.model.entity.ModelInfoEntity;
import com.siact.module.model.mapper.AlgorithmCallInfoMapper;
import com.siact.module.model.service.AlgorithmCallInfoService;
import com.siact.module.model.service.ModelInfoService;
import com.siact.module.predicted.enums.AlgorithmCallStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;

@Slf4j
@Service
@RefreshScope
public class AlgorithmCallInfoServiceImpl  extends ServiceImpl<AlgorithmCallInfoMapper, AlgorithmCallInfoEntity> implements AlgorithmCallInfoService {

    @Value("${algorithm.modelBasePath}")
    private String modelBasePath;


    @Autowired
    private ModelInfoService modelInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCallBackModelInfo(LinkedHashMap<String, Object> params) {
        // 获取参数模板
        log.info("获取模型信息：{}", JSON.toJSONString(params));
        // 解析回调当中的数据
        AlgorithmCallBackModelInfoDTO backModelInfoDTO = JSONObject.parseObject(JSON.toJSONString(params), AlgorithmCallBackModelInfoDTO.class);

        // 获取或掉的modelId,需要根据该id,修改模型的信息
        String modelId = backModelInfoDTO.getModel_id();

        ModelInfoEntity modelInfo = modelInfoService.getById(modelId);

        // 1:先失效同算法的其他模型
        modelInfoService.invalidModelInfo(modelInfo.getDataCode(),modelInfo.getPredictedTypeCode(),modelInfo.getAlgorithmCode());

        // 2:再更新 回调模型信息
        updateCallBackModelInfo(modelInfo, backModelInfoDTO);

        // 3:更新请求记录数据
        Long algorithmCallId = modelInfo.getAlgorithmCallId();
        AlgorithmCallInfoEntity callInfo = getById(algorithmCallId);
        callInfo.setRespTime(TimeUtil.getNow());
        callInfo.setRespJson(JSON.toJSONString(params));
        updateById(callInfo);
    }

    private void updateCallBackModelInfo(ModelInfoEntity modelInfo, AlgorithmCallBackModelInfoDTO backModelInfoDTO) {
        // 设置算法模型名称
        modelInfo.setModelName(backModelInfoDTO.getModel_name());

        // 模型存放路径 /modelBasePath/年/月/日/模型名称
        String modelPath = getModelPath(backModelInfoDTO.getModel_name());
        modelInfo.setModelPath(modelPath);

        // TODO 上传模型文件至minio并保存minio的地址
        String minioPath = uploadMinio(modelPath);
        modelInfo.setModelMinioPath(minioPath);

        // 设置评价数据 // TODO 后期确认,目前评价数据 用的是测试集数据
        AlgorithmCallBackModelEvaluationInfoDetailDTO evaluationInfo = backModelInfoDTO.getEvaluation().getTest();
        modelInfo.setDetermination(evaluationInfo.getR2());
        modelInfo.setMse(evaluationInfo.getMse());
        modelInfo.setMae(evaluationInfo.getMae());
        modelInfo.setAccuracy(evaluationInfo.getAccuracy());
        modelInfo.setAlgorithmCallStatus(AlgorithmCallStatusEnum.SUCCESS.getStatus());

        modelInfoService.updateById(modelInfo);
    }

    @Override
    public Long addAlgorithmCallInfo(String type, Long modelId, String reqTime, String reqJson, String resTime, String resJson) {
        AlgorithmCallInfoEntity entity = new AlgorithmCallInfoEntity();
        long callId = IdWorker.getId(entity);
        entity.setId(callId);
        entity.setType(type);
        entity.setModelId(modelId);
        entity.setReqTime(reqTime);
        entity.setReqJson(reqJson);
        entity.setRespTime(resTime);
        entity.setRespJson(resJson);
        entity.setCreateTime(new Date());
        baseMapper.insert(entity);
        return callId;
    }

    @Override
    public void deleteBeforeTime(String time) {
        // 删除早于time的数据
        LambdaQueryWrapper<AlgorithmCallInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(AlgorithmCallInfoEntity::getReqTime, time);

        baseMapper.delete(queryWrapper);
    }

    private String uploadMinio(String modelPath) {
        // TODO 将文件上传至minio当中
        return "minioPath:" + modelPath;
    }

    /**
     * 获取模型存放路径
     * 格式: /modelBasePath/年/月/日/模型名称
     *
     * @param modelName
     * @return
     */
    @NotNull
    private String getModelPath(String modelName) {
        LocalDateTime now = LocalDateTime.now();
        return String.join(ConstantSymbol.SEPARATOR, modelBasePath,
                now.getYear() + "", now.getMonthValue() + "", now.getDayOfMonth() + "",
                modelName);
    }
}

package com.siact.module.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.enums.StatusEnum;
import com.siact.common.exception.CustomException;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.model.dto.ModelAssessChartDTO;
import com.siact.module.model.dto.ModelAssessChartDetailDTO;
import com.siact.module.model.dto.ModelInfoDTO;
import com.siact.module.model.dto.ModelOutputSelectRtnDTO;
import com.siact.module.model.entity.ModelInfoEntity;
import com.siact.module.model.entity.ModelPublishInfoEntity;
import com.siact.module.model.feign.AlgorithmFeign;
import com.siact.module.model.mapper.ModelInfoMapper;
import com.siact.module.model.service.ModelInfoService;
import com.siact.module.model.service.ModelPublishInfoService;
import com.siact.module.model.service.ModelPublishRecordService;
import com.siact.module.model.vo.PublishModelVO;
import com.siact.module.predicted.enums.AlgorithmCallStatusEnum;
import com.siact.module.predicted.enums.PredictedTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ModelInfoServiceImpl extends ServiceImpl<ModelInfoMapper, ModelInfoEntity> implements ModelInfoService {

    @Autowired
    private ModelPublishInfoService modelPublishInfoService;

    @Autowired
    private ModelPublishRecordService modelPublishRecordService;

    @Autowired
    private AlgorithmFeign algorithmFeign;

    @Override
    public void saveModelInfo(ModelInfoDTO modelInfoDTO) {
        if (Objects.isNull(modelInfoDTO)) {
            return;
        }

        ModelInfoEntity modelInfoEntity = ConvertUtils.sourceToTarget(modelInfoDTO, ModelInfoEntity.class);

        // 1:先失效之前的模型信息
        invalidModelInfo(modelInfoEntity.getDataCode(), modelInfoEntity.getPredictedTypeCode(), modelInfoEntity.getAlgorithmCode());

        // 2:再新增最新的模型信息
        save(modelInfoEntity);
    }

    /**
     * 失效模型信息
     * 逻辑: 同一个dataCode + predictedTypeCode 只能生效一种算法模型的信息
     * @param dataCode
     * @param predictedTypeCode
     */
    private void invalidModelInfo(String dataCode, String predictedTypeCode, String algorithmCode) {

        LambdaUpdateWrapper<ModelInfoEntity> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(ModelInfoEntity::getDataCode, dataCode);
        queryWrapper.eq(ModelInfoEntity::getPredictedTypeCode, predictedTypeCode);
        queryWrapper.eq(ModelInfoEntity::getAlgorithmCode, algorithmCode);
        queryWrapper.eq(ModelInfoEntity::getValid, StatusEnum.VALID.getCode());

        queryWrapper.set(ModelInfoEntity::getValid, StatusEnum.INVALID.getCode());

        update(null, queryWrapper);
    }

    @Override
    public ModelOutputSelectRtnDTO queryModelByDataCodeGroupByPredictedTypeCodes(String dataCode, List<String> predictedTypeCodeList) {

        List<ModelInfoEntity> modelInfoEntityList = queryModelByDataCodeAndPredictedTypeCodes(Arrays.asList(dataCode), predictedTypeCodeList, AlgorithmCallStatusEnum.SUCCESS.getStatus());

        List<ModelInfoDTO> rtnDTOList = ConvertUtils.sourceToTarget(modelInfoEntityList, ModelInfoDTO.class);

        // 查询最后一次的下发记录,并将下发记录回填到modelInfoDTO当中
        ModelPublishInfoEntity lastPublishInfo = modelPublishInfoService.queryLastPublishInfoByDataCode(dataCode);

        ModelOutputSelectRtnDTO rtnDTO = new ModelOutputSelectRtnDTO();
        if (ObjectUtils.isNotEmpty(lastPublishInfo)) {
            String publishModelInfoIds = lastPublishInfo.getPublishModelInfoIds();
            List<String> publishModelInfoIdList = new ArrayList<>(Arrays.asList(publishModelInfoIds.split(",")));
            String multiStartTime = lastPublishInfo.getMultiStartTime();
            String multiEndTime = lastPublishInfo.getMultiEndTime();
            // 返回多步时间设置数据
            rtnDTO.setMultiStartTime(multiStartTime);
            rtnDTO.setMultiEndTime(multiEndTime);

            // 回填模型选中状态
            for (ModelInfoDTO infoDTO : rtnDTOList) {
                infoDTO.setSelected(publishModelInfoIdList.contains(infoDTO.getId().toString()));
            }

        }

        // 按照type的顺序进行排序返回
        LinkedHashMap<String, List<ModelInfoDTO>> rtnTreeMap = new LinkedHashMap<>();
        for (PredictedTypeEnum typeEnum : PredictedTypeEnum.values()) {
            List<ModelInfoDTO> modelInfoDTOS = rtnDTOList.stream().filter(infoDTO -> infoDTO.getPredictedTypeCode().equals(typeEnum.getCode())).collect(Collectors.toList());
            if (ObjectUtils.isNotEmpty(modelInfoDTOS)) {
                rtnTreeMap.put(typeEnum.getCode(), modelInfoDTOS);
            }
        }

        rtnDTO.setSelectedModelInfo(rtnTreeMap);
        return rtnDTO;
    }

    @Override
    public void publishModel(PublishModelVO publishModelVO) {
        if (publishModelVO.getMultiStartTime().compareTo(publishModelVO.getMultiEndTime()) > 0) {
            throw new CustomException("多步时间设置错误,开始时间不能大于结束时间");
        }

        List<Long> modelIdList = publishModelVO.getModelIdList();
        List<ModelInfoEntity> selectedModelInfoList = baseMapper.selectBatchIds(modelIdList);

        // 新增当前批次的下发记录
        ModelPublishInfoEntity publishInfo = new ModelPublishInfoEntity();
        long publishInfoId = IdWorker.getId(publishInfo);
        publishInfo.setId(publishInfoId);
        publishInfo.setDataCode(publishModelVO.getDataCode());
        String publishModelInfoIds = modelIdList.stream().map(String::valueOf).collect(Collectors.joining(","));
        publishInfo.setPublishModelInfoIds(publishModelInfoIds);
        publishInfo.setMultiStartTime(publishModelVO.getMultiStartTime());
        publishInfo.setMultiEndTime(publishModelVO.getMultiEndTime());
        publishInfo.setCreateTime(new Date());
        modelPublishInfoService.save(publishInfo);
    }

    @Override
    public ModelAssessChartDTO queryModelAssessChart(List<Long> modelIdList) {
        // 获取生效中的model
        List<ModelInfoEntity> modelEntityList = baseMapper.selectBatchIds(modelIdList);
        modelEntityList.sort((o1, o2) -> {
            if (o1.getPredictedTypeCode().compareTo(o2.getPredictedTypeCode()) == 0) {
                return o1.getCreateTime().compareTo(o2.getCreateTime());
            } else {
                return o1.getPredictedTypeCode().compareTo(o2.getPredictedTypeCode());
            }
        });

        List<String> xAxis = new ArrayList<>(Arrays.asList("决定系数", "MSE均方误差", "MAE平均绝对误差", "Accuracy精度"));
        List<ModelAssessChartDetailDTO> dataList = new ArrayList<>();

        for (ModelInfoEntity info : modelEntityList) {
            String modelName = info.getModelName();
            String customModelName = info.getCustomModelName();
            Integer predictedType = info.getPredictedType();
            String predictedTypeCode = info.getPredictedTypeCode();

            ArrayList<Object[]> values = new ArrayList<>();
            // 决定系数
            values.add(new Object[]{"决定系数", info.getDetermination()});
            // MSE均方误差
            values.add(new Object[]{"MSE均方误差", info.getMse()});
            // MAE平均绝对误差
            values.add(new Object[]{"MAE平均绝对误差", info.getMae()});
            // Accuracy精度
            values.add(new Object[]{"Accuracy精度", info.getAccuracy()});

            ModelAssessChartDetailDTO curChartDetail = new ModelAssessChartDetailDTO(modelName, customModelName, predictedType, predictedTypeCode, values);
            dataList.add(curChartDetail);
        }

        return new ModelAssessChartDTO(dataList, xAxis);
    }

    /**
     * 根据 dataCodeList 和 predictedTypeCodeList 查询模型信息
     *
     * @param dataCodeList
     * @param predictedTypeCodeList
     * @param algorithmCallStatus 是否已完成算法回调
     * @return
     */
    public List<ModelInfoEntity> queryModelByDataCodeAndPredictedTypeCodes(List<String> dataCodeList, List<String> predictedTypeCodeList, Integer algorithmCallStatus) {
        if (ObjectUtils.isEmpty(dataCodeList)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ModelInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ModelInfoEntity::getDataCode, dataCodeList);
        queryWrapper.eq(ModelInfoEntity::getValid, StatusEnum.VALID.getCode());
        // 是否已完成算法回调
        queryWrapper.eq(ObjectUtils.isNotEmpty(algorithmCallStatus), ModelInfoEntity::getAlgorithmCallStatus, algorithmCallStatus);
        if (ObjectUtils.isNotEmpty(predictedTypeCodeList)) {
            queryWrapper.in(ModelInfoEntity::getPredictedTypeCode, predictedTypeCodeList);
        }

        return baseMapper.selectList(queryWrapper);
    }
}

package com.siact.module.model.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.enmus.PublishStatusEnum;
import com.siact.module.model.dto.ModelAssessChartDTO;
import com.siact.module.model.dto.ModelInfoDTO;
import com.siact.module.model.entity.ModelInfoEntity;
import com.siact.module.model.entity.ModelPublishRecordEntity;
import com.siact.module.model.mapper.ModelInfoMapper;
import com.siact.module.model.service.ModelInfoService;
import com.siact.module.model.service.ModelPublishRecordService;
import com.siact.module.model.vo.SendModelVO;
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
    private ModelPublishRecordService modelPublishRecordService;

    @Override
    public void saveModelInfo(ModelInfoDTO modelInfoDTO) {
        if (Objects.isNull(modelInfoDTO)) {
            return;
        }

        ModelInfoEntity modelInfoEntity = ConvertUtils.sourceToTarget(modelInfoDTO, ModelInfoEntity.class);
        save(modelInfoEntity);
    }

    @Override
    public Map<String, List<ModelInfoDTO>> queryModelByDataCodeGroupByPredictedTypeCodes(String dataCode, List<String> predictedTypeCodeList) {

        List<ModelInfoEntity> modelInfoEntityList = queryModelByDataCodeAndPredictedTypeCodes(dataCode, predictedTypeCodeList);

        List<ModelInfoDTO> rtnDTOList = ConvertUtils.sourceToTarget(modelInfoEntityList, ModelInfoDTO.class);

        return rtnDTOList.stream().collect(Collectors.groupingBy(ModelInfoDTO::getPredictedTypeCode));
    }

    @Override
    public void publishModel(List<SendModelVO> sendModelVoList) {
        // TODO 1:根据sendModelVo构建算法所需要的下发参数 (ps:是所有模型同一个接口一次下发  还是分开多次下发??)
        Object publishParam = "{}";
        // TODO 2:调用算法的接口

        List<ModelPublishRecordEntity> publishRecordList = new ArrayList<>();
        for (SendModelVO modelVO : sendModelVoList) {
            ModelPublishRecordEntity publishRecord = new ModelPublishRecordEntity();
            publishRecord.setDataCode(modelVO.getDataCode());
            publishRecord.setPredictedType(modelVO.getPredictedType());
            publishRecord.setPredictedTypeCode(modelVO.getPredictedTypeCode());
            publishRecord.setPublishParam(JSON.toJSONString(publishParam));
            publishRecord.setModelInfoId(modelVO.getId());
            publishRecord.setModelCode(modelVO.getModelCode());
            publishRecord.setStatus(PublishStatusEnum.PUBLISHING.getCode());
            publishRecord.setCreateTime(new Date());
            publishRecordList.add(publishRecord);
        }
        // 3: 保存发布记录 (ps:后续通过回调或者mqtt 更新发布记录状态)
        modelPublishRecordService.saveModelPublishRecord(publishRecordList);
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

        ModelAssessChartDTO chartDTO = new ModelAssessChartDTO();

        List<String> xAxis = new ArrayList<>(Arrays.asList("决定系数", "MSE均方误差", "MAE平均绝对误差", "Accuracy精度"));
        List<Object[]> dataList = new ArrayList<>();

        for (String xKey : xAxis) {
            for (ModelInfoEntity info : modelEntityList) {
                String modelName = info.getModelName();
                String customModelName = info.getCustomModelName();
                String modelCode = info.getModelCode();
                // 决定系数
                String determination = info.getDetermination();
                // MSE均方误差
                String mse = info.getMse();
                // MAE平均绝对误差
                String mae = info.getMae();
                // Accuracy精度
                String accuracy = info.getAccuracy();
            }
        }


        return chartDTO;
    }

    /**
     * 根据 dataCode 和 predictedTypeCodeList 查询模型信息
     *
     * @param dataCode
     * @param predictedTypeCodeList
     * @return
     */
    private List<ModelInfoEntity> queryModelByDataCodeAndPredictedTypeCodes(String dataCode, List<String> predictedTypeCodeList) {
        if (ObjectUtils.isEmpty(dataCode)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ModelInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModelInfoEntity::getDataCode, dataCode);
        if (ObjectUtils.isNotEmpty(predictedTypeCodeList)) {
            queryWrapper.in(ModelInfoEntity::getPredictedTypeCode, predictedTypeCodeList);
        }

        return baseMapper.selectList(queryWrapper);
    }
}

package com.siact.module.predicted.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.mqtt.entity.MqttRecordEntity;
import com.siact.module.mqtt.service.MqttRecordService;
import com.siact.module.predicted.dto.PredictedDataDTO;
import com.siact.module.predicted.dto.PredictedStepMqttDTO;
import com.siact.module.predicted.entity.PredictedDataEntity;
import com.siact.module.predicted.enums.PredictedTypeEnum;
import com.siact.module.predicted.mapper.PredictedDataMapper;
import com.siact.module.predicted.service.PredictedDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PredictedDataServiceImpl extends ServiceImpl<PredictedDataMapper, PredictedDataEntity> implements PredictedDataService {

    @Autowired
    private MqttRecordService mqttRecordService;

    @Override
    public void handleMqttMessage(String topic, String message) {

        // 1:处理MQTT消息 只记录报文 (TODO 这里是否要放到线程池 快速返回 我认为不用  数据量达不到)
        Date curDataTime = new Date();
        MqttRecordEntity recordEntity = new MqttRecordEntity();
        recordEntity.setTopic(topic);
        recordEntity.setMessage(message);
        recordEntity.setCreateTime(curDataTime);
        mqttRecordService.save(recordEntity);

        // 2:现将MQTT消息转换成对象
        List<PredictedStepMqttDTO> predictedMqttDTOList = JSON.parseArray(message, PredictedStepMqttDTO.class);

        // 要存储的预测数据
        List<PredictedDataEntity> dataEntityList = new ArrayList<>();
        for (PredictedStepMqttDTO mqttDTO : predictedMqttDTOList) {
            PredictedDataEntity dataEntity = new PredictedDataEntity();

            dataEntity.setDataCode(mqttDTO.getDataCode());
            Integer predictedType = PredictedTypeEnum.getTypeByCode(mqttDTO.getTypeCode());
            if (ObjectUtils.isEmpty(predictedType)) {
                log.error("错误的预测类型:{},跳过此次数据集成,mqttDTO:{}", mqttDTO.getTypeCode(), mqttDTO);
                continue;
            }
            dataEntity.setPredictedType(predictedType);
            dataEntity.setPredictedTypeCode(mqttDTO.getTypeCode());
            dataEntity.setTime(mqttDTO.getTime());
            dataEntity.setItemVal(mqttDTO.getItemVal());
            dataEntity.setUnit(mqttDTO.getUnit());
            dataEntity.setCreateTime(curDataTime);

            dataEntityList.add(dataEntity);
        }

        // 3:更新数据表(同时间点进行覆盖  单步覆盖单步  多步覆盖多步  即 根据typeCode进行 和 time进行覆盖)
        for (PredictedDataEntity entity : dataEntityList) {
            LambdaUpdateWrapper<PredictedDataEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PredictedDataEntity::getDataCode, entity.getDataCode());
            updateWrapper.eq(PredictedDataEntity::getPredictedTypeCode, entity.getPredictedTypeCode());
            updateWrapper.eq(PredictedDataEntity::getTime, entity.getTime());
            saveOrUpdate(entity, updateWrapper);
        }

    }

    /**
     * 根据dataCodes 和 types 获取预测数据 (只有单步和多步的两个类型)
     * 逻辑:查询单步和多步类型的数据(同时间点的不同typeCode(如T20,T40...),取最后更新时间的最后一条数据)
     * @param dataCodeList
     * @param predictedTypeList
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public Map<Integer, List<PredictedDataDTO>> getPredictedDataByTypes(List<String> dataCodeList, List<Integer> predictedTypeList, String startTime, String endTime) {
        // 这里要处理 是根据 typeCode进行分组的
        List<PredictedDataEntity> predictedDataDTOList = baseMapper.getPredictedDataByTypes(dataCodeList, predictedTypeList, startTime, endTime);

        List<PredictedDataDTO> dataDTOList = ConvertUtils.sourceToTarget(predictedDataDTOList, PredictedDataDTO.class);
        Map<Integer, List<PredictedDataDTO>> rtnMap = dataDTOList.stream().collect(Collectors.groupingBy(PredictedDataDTO::getPredictedType));

        return rtnMap;
    }
}

package com.siact.module.predicted.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            LambdaQueryWrapper<PredictedDataEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PredictedDataEntity::getDataCode, entity.getDataCode());
            queryWrapper.eq(PredictedDataEntity::getPredictedTypeCode, entity.getPredictedTypeCode());
            queryWrapper.eq(PredictedDataEntity::getTime, entity.getTime());
            saveOrUpdate(entity, queryWrapper);
        }

    }

    /**
     * 根据dataCodes 和 types 获取预测数据 (只有单步和多步的两个类型)
     * 逻辑:查询单步和多步类型的数据(同时间点的不同typeCode(如T20,T40...),取最后更新时间的最后一条数据)
     *
     * @param dataCodeList
     * @param predictedTypeList
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public Map<Integer, List<PredictedDataDTO>> getPredictedDataByTypes(List<String> dataCodeList, List<Integer> predictedTypeList, String startTime, String endTime) {
//        // 这里要处理 是根据 typeCode进行分组的
//        List<PredictedDataEntity> predictedDataDTOList = baseMapper.getPredictedDataByTypes(dataCodeList, predictedTypeList, startTime, endTime);
//
//        List<PredictedDataDTO> dataDTOList = ConvertUtils.sourceToTarget(predictedDataDTOList, PredictedDataDTO.class);
//        Map<Integer, List<PredictedDataDTO>> rtnMap = dataDTOList.stream().collect(Collectors.groupingBy(PredictedDataDTO::getPredictedType));
//
//        return rtnMap;

        Map<Integer, Map<String, List<PredictedDataDTO>>> resultMap = getPredictedDataByTypesCoverBtStep(dataCodeList, predictedTypeList, startTime, endTime);

        Map<Integer, List<PredictedDataDTO>> rtnMap = new HashMap<>();
        for (Map.Entry<Integer, Map<String, List<PredictedDataDTO>>> entry : resultMap.entrySet()) {
            rtnMap.put(entry.getKey(), entry.getValue().values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
        }
        return rtnMap;
    }

    /**
     * 数据格式
     * Map<type , Map<dataCode, List<timeDataList> > >
     *
     * @param dataCodeList
     * @param predictedTypeList
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public Map<Integer, Map<String, List<PredictedDataDTO>>> getPredictedDataByTypesCoverBtStep(List<String> dataCodeList, List<Integer> predictedTypeList, String startTime, String endTime) {
        // 1:先查出所有predictedType的相关数据
        List<PredictedDataEntity> allTypeDataList = queryPredictedDataByDataCodeAndTypeList(dataCodeList, predictedTypeList, startTime, endTime);

        Map<Integer, List<PredictedDataEntity>> allTypeDataMap = allTypeDataList.stream().collect(Collectors.groupingBy(PredictedDataEntity::getPredictedType));

        // 处理单步数据
        List<PredictedDataEntity> singleTypeDataList = allTypeDataMap.getOrDefault(PredictedTypeEnum.singleType(), new ArrayList<>());
        // 有多个dataCode
        // 根据dataCode分组后 再根据time进行分组
        Map<String, Map<String, PredictedDataEntity>> singleDataCodeTimeMap = singleTypeDataList.stream().collect(Collectors.groupingBy(PredictedDataEntity::getDataCode, Collectors.collectingAndThen(Collectors.toList(),
                list -> list.stream().collect(Collectors.toMap(PredictedDataEntity::getTime, o -> o, (o1, o2) -> {
                    if (o1.getPredictedTypeCode().compareTo(o2.getPredictedTypeCode()) > 0) {
                        // o1大于o2 证明 o1的步长大于o2的步长  需要用o2覆盖o1
                        return o2;
                    }
                    return o1;
                })))));

        Map<String, List<PredictedDataDTO>> singleDataCodeTimeDTOMap = new HashMap<>();
        for (Map.Entry<String, Map<String, PredictedDataEntity>> entry : singleDataCodeTimeMap.entrySet()) {
            String dataCode = entry.getKey();
            ArrayList<PredictedDataEntity> curDataCodeTimeList = new ArrayList<>(entry.getValue().values());

            List<PredictedDataDTO> curDataTimeList = singleDataCodeTimeDTOMap.getOrDefault(dataCode, new ArrayList<>());
            curDataTimeList.addAll(ConvertUtils.sourceToTarget(curDataCodeTimeList, PredictedDataDTO.class));
            singleDataCodeTimeDTOMap.put(dataCode, curDataTimeList);
        }

        // 处理多步数据
        List<PredictedDataEntity> multiTypeDataList = allTypeDataMap.getOrDefault(PredictedTypeEnum.multiType(), new ArrayList<>());
        Map<String, Map<String, PredictedDataEntity>> multiDataCodeTimeEntity = multiTypeDataList.stream().collect(Collectors.groupingBy(PredictedDataEntity::getDataCode, Collectors.collectingAndThen(Collectors.toList(),
                list -> list.stream().collect(Collectors.toMap(PredictedDataEntity::getTime, o -> o)))));

        Map<String, List<PredictedDataDTO>> multiDataCodeTimeDTOMap = new HashMap<>();
        for (Map.Entry<String, Map<String, PredictedDataEntity>> entry : multiDataCodeTimeEntity.entrySet()) {
            String dataCode = entry.getKey();
            ArrayList<PredictedDataEntity> curDataCodeTimeList = new ArrayList<>(entry.getValue().values());

            List<PredictedDataDTO> curDataTimeList = multiDataCodeTimeDTOMap.getOrDefault(dataCode, new ArrayList<>());
            curDataTimeList.addAll(ConvertUtils.sourceToTarget(curDataCodeTimeList, PredictedDataDTO.class));
            multiDataCodeTimeDTOMap.put(dataCode, curDataTimeList);
        }


        Map<Integer, Map<String, List<PredictedDataDTO>>> rtnMap = new HashMap<>();
        rtnMap.put(PredictedTypeEnum.singleType(), singleDataCodeTimeDTOMap);
        rtnMap.put(PredictedTypeEnum.multiType(), multiDataCodeTimeDTOMap);

        return rtnMap;
    }

    @Override
    public List<JSONObject> getAllTypeList() {
        List<JSONObject> rtnList = new ArrayList<>();

        PredictedTypeEnum[] typeEnums = PredictedTypeEnum.values();
        for (PredictedTypeEnum typeEnum : typeEnums) {
            JSONObject typeEnumObj = new JSONObject();
            typeEnumObj.put("type", typeEnum.getType());
            typeEnumObj.put("code", typeEnum.getCode());
            typeEnumObj.put("name", typeEnum.getName());
            typeEnumObj.put("step", typeEnum.getStep());
            rtnList.add(typeEnumObj);
        }
        return rtnList;
    }

    @Override
    public List<PredictedDataEntity> queryDataByTime(List<String> dataCodeList, List<Integer> predictedTypeList, String time) {
        LambdaQueryWrapper<PredictedDataEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ObjectUtils.isNotEmpty(dataCodeList), PredictedDataEntity::getDataCode, dataCodeList);
        queryWrapper.in(ObjectUtils.isNotEmpty(predictedTypeList), PredictedDataEntity::getPredictedType, predictedTypeList);
        queryWrapper.eq(ObjectUtils.isNotEmpty(time), PredictedDataEntity::getTime, time);

        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据dataCode 和 predictedType 查找时间范围内的数据
     *
     * @param dataCodeList
     * @param predictedTypeList
     * @param startTime
     * @param endTime
     * @return
     */
    private List<PredictedDataEntity> queryPredictedDataByDataCodeAndTypeList(List<String> dataCodeList, List<Integer> predictedTypeList, String startTime, String endTime) {
        LambdaQueryWrapper<PredictedDataEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(PredictedDataEntity::getDataCode, dataCodeList);
        queryWrapper.in(PredictedDataEntity::getPredictedType, predictedTypeList);
        queryWrapper.between(PredictedDataEntity::getTime, startTime, endTime);

        return baseMapper.selectList(queryWrapper);
    }
}

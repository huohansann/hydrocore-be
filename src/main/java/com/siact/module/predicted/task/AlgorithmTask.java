package com.siact.module.predicted.task;

import com.alibaba.fastjson2.JSONObject;
import com.siact.common.constant.ConstantTime;
import com.siact.common.utils.TimeUtil;
import com.siact.module.base.service.TplService;
import com.siact.module.model.dto.AlgorithmDataCodeDTO;
import com.siact.module.model.dto.AlgorithmPublishModelParamDTO;
import com.siact.module.model.dto.AlgorithmPublishModelParamDetailDTO;
import com.siact.module.model.dto.ModelConfigParamDTO;
import com.siact.module.model.entity.ModelConfigParamEntity;
import com.siact.module.model.entity.ModelInfoEntity;
import com.siact.module.model.feign.AlgorithmFeign;
import com.siact.module.model.service.ModelConfigParamService;
import com.siact.module.model.service.ModelInfoService;
import com.siact.module.predicted.dto.AlgorithmPredictionDataCodeTplDTO;
import com.siact.module.predicted.enums.PredictedTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
public class AlgorithmTask {

    @Autowired
    private TplService tplService;

    @Autowired
    private ModelInfoService modelInfoService;

    @Autowired
    private ModelConfigParamService modelConfigParamService;

    @Autowired
    private AlgorithmFeign algorithmFeign;


    /**
     * 每分钟调用一次算法  获取预测数据
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void run() {

        String nowTimeStr = TimeUtil.getNowStr(ConstantTime.DATE_TIME_MM_00);

        // 1:读取模型数据当中的预测相关的模型
        List<AlgorithmPredictionDataCodeTplDTO> predictionDataList = tplService.getListByCode("algorithmPredictionDataCode", AlgorithmPredictionDataCodeTplDTO.class);

        List<String> dataCodeList = predictionDataList.stream().map(AlgorithmPredictionDataCodeTplDTO::getDataCode).distinct().collect(Collectors.toList());

        List<ModelInfoEntity> modelInfoEntityList = modelInfoService.queryModelByDataCodeAndPredictedTypeCodes(dataCodeList, null);

        List<ModelConfigParamEntity> validParamList = modelConfigParamService.queryValidParamEntityList(dataCodeList, null);

        Map<Long, ModelConfigParamEntity> paranInfoMap = validParamList.stream().collect(Collectors.toMap(ModelConfigParamEntity::getId, o -> o));

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
            detailParam.setModel_name(modelName);

            String algorithmCode = modelInfoEntity.getAlgorithmCode();
            detailParam.setMethod(algorithmCode);

            Long configParamId = modelInfoEntity.getConfigParamId();

            String dataCode = modelInfoEntity.getDataCode();
            String predictedTypeCode = modelInfoEntity.getPredictedTypeCode();
            configParamId = 1939854806733119490L;
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



        // 2:解析模型  组装param
        LinkedHashMap<String, Object> response = algorithmFeign.inference(modelParamDTO);
        log.info("调用模型预测,入参:{},结果:{}", modelParamDTO, response);


        // 3:解析预测结果


        // TODO 2:调用算法的接口
        // 新增模型下发调用算法的记录
//        List<ModelPublishRecordEntity> publishRecordList = new ArrayList<>();
//        for (ModelInfoEntity modelEntity : selectedModelInfoList) {
//            ModelPublishRecordEntity publishRecord = new ModelPublishRecordEntity();
//            publishRecord.setModelInfoId(modelEntity.getId());
//            publishRecord.setDataCode(modelEntity.getDataCode());
//            publishRecord.setPredictedType(modelEntity.getPredictedType());
//            publishRecord.setPredictedTypeCode(modelEntity.getPredictedTypeCode());
//            publishRecord.setPublishParam(JSON.toJSONString(publishParam));
//            publishRecord.setModelCode(modelEntity.getModelCode());
//            publishRecord.setStatus(PublishStatusEnum.PUBLISHING.getCode());
//            publishRecord.setCreateTime(new Date());
//            publishRecord.setPublishInfoId(publishInfoId);
//            publishRecordList.add(publishRecord);
//        }
//        // 3: 保存发布记录 (ps:后续通过回调或者mqtt 更新发布记录状态)
//        modelPublishRecordService.saveModelPublishRecord(publishRecordList);
    }
}

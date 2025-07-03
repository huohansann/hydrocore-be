package com.siact.module.model.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.constant.ConstantBase;
import com.siact.common.constant.ConstantTime;
import com.siact.common.constant.ConstantUtil;
import com.siact.common.enums.StatusEnum;
import com.siact.common.exception.CustomException;
import com.siact.common.utils.ConvertUtils;
import com.siact.common.utils.TimeUtil;
import com.siact.module.base.service.TplService;
import com.siact.module.enmus.ModelStatusEnum;
import com.siact.module.model.dto.*;
import com.siact.module.model.entity.AlgorithmCallInfoEntity;
import com.siact.module.model.entity.ModelConfigParamEntity;
import com.siact.module.model.entity.ModelInfoEntity;
import com.siact.module.model.feign.AlgorithmFeign;
import com.siact.module.model.mapper.ModelConfigParamMapper;
import com.siact.module.model.service.AlgorithmCallInfoService;
import com.siact.module.model.service.ModelConfigParamService;
import com.siact.module.model.service.ModelInfoService;
import com.siact.module.model.utils.AlgorithmDataCodeUtil;
import com.siact.module.model.vo.ModelConfigParamSaveVO;
import com.siact.module.predicted.enums.AlgorithmCallStatusEnum;
import com.siact.module.predicted.enums.PredictedTypeEnum;
import com.siact.module.process.entity.ProcessLogEntity;
import com.siact.module.process.service.IProcessLogService;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.sec.sevice.DataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ModelConfigParamServiceImpl extends ServiceImpl<ModelConfigParamMapper, ModelConfigParamEntity> implements ModelConfigParamService {

    @Autowired
    private TplService tplService;

    @Autowired
    private ModelInfoService modelInfoService;

    @Autowired
    private AlgorithmDataCodeUtil algorithmDataCodeUtil;

    @Autowired
    private DataService dataService;

    @Autowired
    private IProcessLogService processLogService;

    @Autowired
    private AlgorithmFeign algorithmFeign;

    @Autowired
    private AlgorithmCallInfoService algorithmCallInfoService;

    @Override
    public Map<String, String> getParamTemplate() {
        // 公共设置
        List<ModelConfigParamDetailDTO> publicSetting = tplService.getListByCode("modelSettingPublicParam", ModelConfigParamDetailDTO.class);
        // 算法设置
        List<ModelConfigParamDetailDTO> algorithmSetting = tplService.getListByCode("modelSettingAlgorithmParam", ModelConfigParamDetailDTO.class);

        Map<String, String> rtnMap = new HashMap<>();
        rtnMap.put("publicSetting", JSON.toJSONString(publicSetting));
        rtnMap.put("algorithmSetting", JSON.toJSONString(algorithmSetting));
        return rtnMap;
    }

    @Override
    public ModelConfigParamRtnDTO queryParamByDataCodeAndPredictedTypeCode(String dataCode, String predictedTypeCode) {
        ModelConfigParamEntity configParamEntity = getValidParamEntity(dataCode, predictedTypeCode);
        if (ObjectUtils.isEmpty(configParamEntity)) {
            ModelConfigParamRtnDTO rtnDTO = new ModelConfigParamRtnDTO();
            // 公共设置
            ModelConfigParamDTO publicSetting = tplService.getByCode("modelSettingPublicParam", ModelConfigParamDTO.class);
            // 算法设置
            List<ModelConfigParamDetailDTO> algorithmSetting = tplService.getListByCode("modelSettingAlgorithmParam", ModelConfigParamDetailDTO.class);

            rtnDTO.setPublicSetting(publicSetting);
            rtnDTO.setAlgorithmSetting(algorithmSetting);
            return rtnDTO;
        }

        ModelConfigParamRtnDTO rtnDTO = ConvertUtils.sourceToTarget(configParamEntity, ModelConfigParamRtnDTO.class);
        // 将数据库当中的json数据转化为对象
        rtnDTO.setPublicSetting(JSONObject.parseObject(configParamEntity.getPublicSetting(), ModelConfigParamDTO.class));
        rtnDTO.setAlgorithmSetting(JSONArray.parseArray(configParamEntity.getAlgorithmSetting(), ModelConfigParamDetailDTO.class));

        return rtnDTO;
    }

    /**
     * 查询有效的参数配置
     * @param dataCode
     * @param predictedTypeCode
     * @return
     */
    private ModelConfigParamEntity getValidParamEntity(String dataCode, String predictedTypeCode) {
        // 查询有效的参数配置
        LambdaQueryWrapper<ModelConfigParamEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModelConfigParamEntity::getDataCode, dataCode);
        queryWrapper.eq(ModelConfigParamEntity::getPredictedTypeCode, predictedTypeCode);
        queryWrapper.eq(ModelConfigParamEntity::getValid, StatusEnum.VALID.getCode());

        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<ModelConfigParamEntity> queryValidParamEntityList(List<String> dataCodeList, String predictedTypeCode) {
        // 查询有效的参数配置
        LambdaQueryWrapper<ModelConfigParamEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ModelConfigParamEntity::getDataCode, dataCodeList);
        queryWrapper.eq(ObjectUtils.isNotEmpty(predictedTypeCode), ModelConfigParamEntity::getPredictedTypeCode, predictedTypeCode);
        queryWrapper.eq(ModelConfigParamEntity::getValid, StatusEnum.VALID.getCode());

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateParam(ModelConfigParamSaveVO configParamSaveVo) {
        ModelConfigParamEntity entity = new ModelConfigParamEntity();
        // 新增或修改 设置id
        if (ObjectUtils.isEmpty(configParamSaveVo.getId())) {
            Long uuid = IdWorker.getId(entity);
            entity.setId(uuid);
        } else {
            entity.setId(configParamSaveVo.getId());
        }

        // 失效之前的数据 同一个dataCode  和 predictedTypeCode 不能重复
        invalidStatusByDataCodeAndPredictedTypeCode(configParamSaveVo.getDataCode(), configParamSaveVo.getPredictedTypeCode());

        entity.setDataCode(configParamSaveVo.getDataCode());
        entity.setCustomModelName(configParamSaveVo.getCustomModelName());
        entity.setPredictedTypeCode(configParamSaveVo.getPredictedTypeCode());
        entity.setPredictedType(PredictedTypeEnum.getTypeByCode(configParamSaveVo.getPredictedTypeCode()));
        entity.setPublicSetting(JSON.toJSONString(configParamSaveVo.getPublicSetting()));
        entity.setAlgorithmSetting(JSON.toJSONString(configParamSaveVo.getAlgorithmSetting()));
        entity.setValid(StatusEnum.VALID.getCode());

        saveOrUpdate(entity);

        // 调用算法  生成model
        sendParamForModel(entity);
    }

    /**
     * 失效之前的数据(软删除)
     * @param dataCode
     * @param predictedTypeCode
     */
    private void invalidStatusByDataCodeAndPredictedTypeCode(String dataCode, String predictedTypeCode) {

        LambdaUpdateWrapper<ModelConfigParamEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ModelConfigParamEntity::getDataCode, dataCode);
        updateWrapper.eq(ModelConfigParamEntity::getPredictedTypeCode, predictedTypeCode);
        updateWrapper.eq(ModelConfigParamEntity::getValid, StatusEnum.VALID.getCode());

        updateWrapper.set(ModelConfigParamEntity::getValid, StatusEnum.INVALID.getCode());
        baseMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendParamById(Long id) {
        if (ObjectUtils.isEmpty(id)) {
            log.error("sendParamById. id is null");
            return;
        }

        ModelConfigParamEntity entity = baseMapper.selectById(id);
        if (ObjectUtils.isEmpty(entity)) {
            log.error("sendParamById. entity is null");
            return;
        }

        sendParamForModel(entity);
    }

    /**
     * 根据id下发参数至算法(ps:算法根据参数生成模型数据)
     *
     * @param entity
     */
    private void sendParamForModel(ModelConfigParamEntity entity) {
        long modelInfoId = IdWorker.getId(new ModelInfoEntity());
        // 构建算法生成模型需要的参数
        AlgorithmGenerateModelParamDTO generateModelParamDto = buildGenerateModelParam(entity, modelInfoId);

        log.info("sendParamMap:{}",JSON.toJSONString(generateModelParamDto));

        // 调用算法生成模型
        Long algorithmCallId = callAlgorithmTrainModel(modelInfoId, generateModelParamDto);

        // 解析出算法code
        String algorithmCode = null;
        if (generateModelParamDto != null) {
            algorithmCode = generateModelParamDto.getMethod();
        }
        // 初始化 创建模型信息(ps:其他字段由算法回调后补全)
        ModelInfoDTO modelInfoDTO = new ModelInfoDTO();
        modelInfoDTO.setId(modelInfoId);
        modelInfoDTO.setDataCode(entity.getDataCode());
        modelInfoDTO.setAlgorithmCode(algorithmCode);
        modelInfoDTO.setConfigParamId(entity.getId());
        modelInfoDTO.setPredictedType(entity.getPredictedType());
        modelInfoDTO.setPredictedTypeCode(entity.getPredictedTypeCode());
        modelInfoDTO.setCustomModelName(entity.getCustomModelName());
        modelInfoDTO.setStatus(ModelStatusEnum.RUNNING.getValue());
        modelInfoDTO.setValid(StatusEnum.VALID.getCode());
        // 设置算法相关字段
        modelInfoDTO.setAlgorithmCallId(algorithmCallId);
        modelInfoDTO.setAlgorithmCallStatus(AlgorithmCallStatusEnum.RUNNING.getStatus());

        modelInfoService.saveModelInfo(modelInfoDTO);
    }

    @Nullable
    private Long callAlgorithmTrainModel(long modelInfoId, AlgorithmGenerateModelParamDTO generateModelParamDto) {
        // 保存算法调用记录
        AlgorithmCallInfoEntity algorithmCallInfo = new AlgorithmCallInfoEntity();
        long algorithmCallId = IdWorker.getId(algorithmCallInfo);
        algorithmCallInfo.setId(algorithmCallId);
        algorithmCallInfo.setType("train");
        algorithmCallInfo.setModelId(modelInfoId);
        algorithmCallInfo.setReqTime(TimeUtil.getNow());
        algorithmCallInfo.setReqJson(JSON.toJSONString(generateModelParamDto));
        algorithmCallInfo.setCreateTime(new Date());

        LinkedHashMap<String, Object> projectListByDateRange = null;
        try {
            projectListByDateRange = algorithmFeign.train(generateModelParamDto);
            log.info("调用算法接口返回数据.projectListByDateRange:{}", JSON.toJSONString(projectListByDateRange));
            algorithmCallInfo.setRespTime(TimeUtil.getNow());
            algorithmCallInfo.setRespJson(JSON.toJSONString(projectListByDateRange));
        } catch (Exception e) {
            log.error("调用算法接口异常,入参:{},响应:{}", generateModelParamDto, projectListByDateRange, e);
            algorithmCallInfo.setRespTime(TimeUtil.getNow());
            algorithmCallInfo.setRespJson("出现异常:请求返回" + JSON.toJSONString(projectListByDateRange) + ",异常信息:" + e.getMessage());
            return null;
        } finally {
            algorithmCallInfoService.save(algorithmCallInfo);
        }
        return algorithmCallId;
    }

    /**
     * 构建下发生成模型的参数
     * 逻辑: 将公共配置  和 算法配置  的paramCode和value做对应
     * 返回结果 k:paramCode v:value
     *
     * @param entity
     * @return
     */
    private AlgorithmGenerateModelParamDTO buildGenerateModelParam(ModelConfigParamEntity entity, Long modelInfoId) {
        if (ObjectUtils.isEmpty(entity)) {
            return null;
        }

        AlgorithmGenerateModelParamDTO paramDTO = new AlgorithmGenerateModelParamDTO();

        // 设置生成新模型的id
        paramDTO.setModel_id(modelInfoId + "");

        // 公共配置当中所选中的参数特征名称
        // 处理公共配置 (获取选中的配置)
        String publicSetting = entity.getPublicSetting();
        ModelConfigParamDTO publicParamDto = JSONObject.parseObject(publicSetting, ModelConfigParamDTO.class);
        List<AlgorithmDataCodeDTO> featuresDataCodeDtoList = parsePublicParam(publicParamDto);
        // 设置训练参与的特征列
        List<String> features = featuresDataCodeDtoList.stream().map(AlgorithmDataCodeDTO::getDataCode).collect(Collectors.toList());
        paramDTO.setFeatures(features);

        // 需要计算的目标列  MC1~MC10  对应的算法code
        AlgorithmDataCodeDTO algorithmDataCodeDTO = algorithmDataCodeUtil.getByDataCode(entity.getDataCode());
        if (ObjectUtils.isEmpty(algorithmDataCodeDTO)){
            throw new CustomException("未找到对应的算法数据code:" + entity.getDataCode());
        }
        // 设置需要计算的目标列
        paramDTO.setTarget(algorithmDataCodeDTO.getAlgorithmCode());

        // 处理算法配置
        String algorithmSetting = entity.getAlgorithmSetting();
        List<ModelConfigParamDetailDTO> algorithmParamDtoList = JSONArray.parseArray(algorithmSetting, ModelConfigParamDetailDTO.class);

        ModelConfigParamDetailDTO selectedAlgorithmConfig = algorithmParamDtoList.stream().filter(ModelConfigParamDetailDTO::getSelected).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(selectedAlgorithmConfig)) {
            throw new CustomException("未选中算法," + entity.getPredictedTypeCode());
        }

        // 设置算法类型 如:bp
        paramDTO.setMethod(selectedAlgorithmConfig.getType());

        // 算法参数
        Map<String, Object> methodPar = new HashMap<>();
        for (ModelConfigParamDetailDTO detailDTO : selectedAlgorithmConfig.getParamList()) {
            String paramCode = detailDTO.getParamCode();
            String value = detailDTO.getValue();
            methodPar.put(paramCode, value);
        }
        // 设置算法参数
        paramDTO.setMethod_par(methodPar);

        // 设置工况总数,目前固定为9 TODO 后期需要进一步确认传值
        paramDTO.setWork_code_num(9);

        // 预测类型 单步('single_step')或多步('multiple_step')
        PredictedTypeEnum predictedTypeEnum = PredictedTypeEnum.getEnumByCode(entity.getPredictedTypeCode());
        if (ObjectUtils.isEmpty(predictedTypeEnum)) {
            throw new CustomException("未找到对应的预测类型:" + entity.getPredictedTypeCode());
        }
        // 设置步长类型
        paramDTO.setType(predictedTypeEnum.getAlgorithmCode());
        // 预测步长
        paramDTO.setFuture_number(predictedTypeEnum.getStep());

        // 取值为参数设置中开始结束滑块时间的长度
        List<ModelConfigParamDetailDTO> timeRange = publicParamDto.getRange();
        Map<String, String> timeRangeCodeMap = timeRange.stream().collect(Collectors.toMap(ModelConfigParamDetailDTO::getParamCode, ModelConfigParamDetailDTO::getValue));

        String hisDataStartTimeStr = timeRangeCodeMap.get("hisDataStartTime");
        String hisDataEndTimeStr = timeRangeCodeMap.get("hisDataEndTime");

        if (hisDataStartTimeStr == null || hisDataEndTimeStr == null) {
            throw new CustomException("未找到对应的时间范围:" + entity.getPredictedTypeCode());
        }
        BigDecimal hisDataStartTime = new BigDecimal(hisDataStartTimeStr);
        BigDecimal hisDataEndTime = new BigDecimal(hisDataEndTimeStr);

        // 设置时间范围(页面设置的时间,末-头)
        BigDecimal past_number = hisDataStartTime.subtract(hisDataEndTime);
        paramDTO.setPast_number(past_number.stripTrailingZeros().toPlainString());

        LocalDateTime nowDateTIme = LocalDateTime.now();
        LocalDateTime startTime = TimeUtil.offset(nowDateTIme, hisDataStartTime.intValue(), ChronoUnit.MINUTES);
        LocalDateTime endTime = TimeUtil.offset(nowDateTIme, hisDataEndTime.intValue(), ChronoUnit.MINUTES);

        // 设置算法需要的数据
        List<String> featuresDataCodeList = featuresDataCodeDtoList.stream().map(AlgorithmDataCodeDTO::getDataCode).collect(Collectors.toList());
        Map<String, Map<String, List<BigDecimal>>> dataMap = getAlgorithmDataParam(entity, featuresDataCodeList, startTime, endTime);
        paramDTO.setData(dataMap);

        // 拆分 训练集  测试集  验证集 的数据占比 (目前固定为  0.8  0.1  0.1)
        paramDTO.setData_rate(new ArrayList<>(Arrays.asList(0.8, 0.1, 0.1)));

        return paramDTO;
    }

    @NotNull
    public List<AlgorithmDataCodeDTO> parsePublicParam(ModelConfigParamDTO publicParamDto) {
        List<ModelConfigParamDetailDTO> allParamList = publicParamDto.getParam();

        List<AlgorithmDataCodeDTO> featuresDataCodeDtoList = new ArrayList<>();
        allParamList.stream().map(ModelConfigParamDetailDTO::getParamList).flatMap(List::stream).filter(ModelConfigParamDetailDTO::getSelected).forEach(param -> {
            AlgorithmDataCodeDTO algorithmCodeDto = algorithmDataCodeUtil.getByAlgorithmCode(param.getParamCode());
            if (ObjectUtils.isNotEmpty(algorithmCodeDto)) {
                featuresDataCodeDtoList.add(algorithmCodeDto);
            }
        });
        return featuresDataCodeDtoList;
    }

    @NotNull
    private Map<String, Map<String, List<BigDecimal>>> getAlgorithmDataParam(ModelConfigParamEntity entity, List<String> featuresDataCodeList, LocalDateTime startTime, LocalDateTime endTime) {
        // 查询时间范围内的孪生数据
        IntervalValParamsDto queryParam = new IntervalValParamsDto();
        ArrayList<String> allQueryDataCodeList = new ArrayList<>();
        allQueryDataCodeList.add(entity.getDataCode());
        allQueryDataCodeList.addAll(featuresDataCodeList);
        queryParam.setDataCodes(allQueryDataCodeList);
        queryParam.setStartTime(startTime.format(ConstantUtil.DATE_TIME_FORMATTER));
        queryParam.setEndTime(endTime.format(ConstantUtil.DATE_TIME_FORMATTER));
        queryParam.setTs(1);
        queryParam.setTsUnit(ConstantBase.MIN);// 查询每分钟数据
        queryParam.setCalcType(ConstantBase.LAST);// 查询温度,为瞬时值
        queryParam.setFormatVal(ConstantTime.DATE_TIME);

        List<IntervalDataDto> secDataList = dataService.queryIntervalVal(queryParam);

        // 查询起止时间段内的
        List<ProcessLogEntity> processLogList = processLogService.getByTimeRange(startTime.format(ConstantUtil.DATE_FORMATTER), endTime.format(ConstantUtil.DATE_FORMATTER));

        // 不符合的数据改为null
        List<IntervalDataDto> filterSecDataList = secDataList.stream().map(data -> {
            boolean flag = true;
            for (ProcessLogEntity processLog : processLogList) {
                String dataTime = data.getTime();
                if (dataTime.compareTo(processLog.getStartTime()) >= 0 && dataTime.compareTo(processLog.getEndTime()) <= 0) {
                    // 只要有一个time在换机区间范围内 则不加入
                    flag = false;
                    break;
                }
            }
            return flag ? data : null;
        }).collect(Collectors.toList());

        int index = -1;
        Map<String, Map<String, List<BigDecimal>>> dataMap = new LinkedHashMap<>();
        for (int i = 0; i < filterSecDataList.size(); i++) {
            IntervalDataDto dataDto = filterSecDataList.get(i);
            if (dataDto == null) {
                while (i < filterSecDataList.size()) {
                    if (filterSecDataList.get(i) == null) {
                        i++;
                    }else {
                        i--;// 退回多加的 1
                        break;
                    }
                }
                index++;
            } else {
                if (index == -1){
                    index = 0;
                }
                Map<String, List<BigDecimal>> codeValMap = dataMap.getOrDefault(index+"", new HashMap<>());
                String dataCode = dataDto.getDataCode();
                AlgorithmDataCodeDTO dataCodeDTO = algorithmDataCodeUtil.getByDataCode(dataCode);
                if (dataCodeDTO == null) {
                    continue;
                }
                String algorithmCode = dataCodeDTO.getAlgorithmCode();

                List<BigDecimal> valList = codeValMap.getOrDefault(algorithmCode, new ArrayList<>());
                valList.add(dataDto.getItemVal());
                codeValMap.put(algorithmCode, valList);
                dataMap.put(index+"", codeValMap);
            }
        }
        return dataMap;
    }

}

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
import com.siact.module.process.enums.ReplaceMachineEnum;
import com.siact.module.process.service.IProcessLogService;
import com.siact.module.process.utils.ProcessOneHotEncoderEnum;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.sec.sevice.DataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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

    // 异步线程池
    @Resource(name = "threadIoPoolTaskExecutor")
    private ThreadPoolTaskExecutor threadIoPoolTaskExecutor;

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
     *
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

        // 异步调用算法  生成model
        threadIoPoolTaskExecutor.execute(() -> {
            try {
                sendParamForModel(entity);
            } catch (Exception e) {
                log.error("调用算法生成训练模型,线程池任务执行异常，参数: {}", entity, e);
            }
        });
    }

    /**
     * 失效之前的数据(软删除)
     *
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

        // 异步调用算法  生成model
        threadIoPoolTaskExecutor.execute(() -> {
            try {
                sendParamForModel(entity);
            } catch (Exception e) {
                log.error("调用算法生成训练模型,线程池任务执行异常，参数: {}", entity, e);
            }
        });
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

        log.info("sendParamMap:{}", JSON.toJSONString(generateModelParamDto));

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
        algorithmCallInfoService.save(algorithmCallInfo);

        // 异步调用算法feign接口,并更新回调数据
        threadIoPoolTaskExecutor.execute(() -> {
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
            } finally {
                algorithmCallInfoService.updateById(algorithmCallInfo);
            }
        });
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
        List<String> features = featuresDataCodeDtoList.stream().map(AlgorithmDataCodeDTO::getAlgorithmCode).collect(Collectors.toList());
        paramDTO.setFeatures(features);

        // 需要计算的目标列  MC1~MC10  对应的算法code
        AlgorithmDataCodeDTO algorithmDataCodeDTO = algorithmDataCodeUtil.getByDataCode(entity.getDataCode());
        if (ObjectUtils.isEmpty(algorithmDataCodeDTO)) {
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

        // 设置工况总数,目前固定为12 12种工况
        paramDTO.setWork_code_num(12);

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
        // TODO 这里的时间可能要改成区间 hisDataStartTime.intValue()  hisDataEndTime.intValue()
        LocalDateTime startTime = TimeUtil.offset(nowDateTIme, hisDataStartTime.intValue(), ChronoUnit.MINUTES);
        LocalDateTime endTime = TimeUtil.offset(nowDateTIme, hisDataEndTime.intValue(), ChronoUnit.MINUTES);

        // 设置算法需要的数据
        List<String> featuresDataCodeList = featuresDataCodeDtoList.stream().map(AlgorithmDataCodeDTO::getDataCode).collect(Collectors.toList());
        Map<String, List<Map<String, List<BigDecimal>>>> dataMap = getAlgorithmDataParam(entity, featuresDataCodeList, startTime, endTime);
        log.info("组装的算法数据:{}", dataMap);
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

    private Map<String, List<Map<String, List<BigDecimal>>>> getAlgorithmDataParam(ModelConfigParamEntity entity, List<String> featuresDataCodeList, LocalDateTime startTime, LocalDateTime endTime) {
        String startTimeStr = startTime.format(ConstantUtil.DATE_FORMATTER);
        String endTimeStr = endTime.format(ConstantUtil.DATE_FORMATTER);

        // 1:查询范围内的工况信息(正常时段的)
        List<ProcessLogEntity> processLogList = processLogService.getByTimeRange(
                startTimeStr,
                endTimeStr,
                ReplaceMachineEnum.NORMAL.getValue());
        if (ObjectUtils.isEmpty(processLogList)) {
            log.error("工况信息未初始化为空,不进行处理");
            return null;
        }

        // 2:整理数据 Map<String, Map<String, List<BigDecimal>>> <工况code , <数据code, 数据值List>>
        Map<String, List<Map<String, List<BigDecimal>>>> algorithmDataValMap = new HashMap<>();

        for (ProcessLogEntity curProcess : processLogList) {
            String secStartTime = curProcess.getEndTime();
            // 如果工艺时间 小于 入参的开始时间, 那么以入参的开始时间为准
            if (secStartTime.compareTo(startTimeStr) < 0) {
                secStartTime = startTimeStr;
            }
            String secEndTime = curProcess.getStartTime();
            // 如果工艺时间 大于 入参的结束时间, 那么以入参的结束时间为准
            if (secEndTime.compareTo(endTimeStr) > 0) {
                secEndTime = endTimeStr;
            }

            buildAlgorithmDataValMap(entity, featuresDataCodeList, secStartTime, secEndTime, curProcess, algorithmDataValMap);
        }

        return algorithmDataValMap;
    }

    private void buildAlgorithmDataValMap(ModelConfigParamEntity entity, List<String> featuresDataCodeList, String secStartTime, String secEndTime, ProcessLogEntity curProcess, Map<String, List<Map<String, List<BigDecimal>>>> algorithmDataValMap) {
        // 2:查询孪生时间范围内的数据
        // 查询时间范围内的孪生数据
        IntervalValParamsDto queryParam = new IntervalValParamsDto();
        ArrayList<String> allQueryDataCodeList = new ArrayList<>();
        allQueryDataCodeList.add(entity.getDataCode());
        allQueryDataCodeList.addAll(featuresDataCodeList);
        queryParam.setDataCodes(allQueryDataCodeList);
        queryParam.setStartTime(secStartTime);
        queryParam.setEndTime(secEndTime);
        queryParam.setTs(1);
        queryParam.setTsUnit(ConstantBase.MIN);// 查询每分钟数据
        queryParam.setCalcType(ConstantBase.LAST);// 查询温度,为瞬时值
        queryParam.setFormatVal(ConstantTime.DATE_TIME);

         List<IntervalDataDto> secDataList = dataService.queryIntervalVal(queryParam);

        // 获取当前工况type的one-hot对应的code
        ProcessOneHotEncoderEnum hotEncoderEnum = ProcessOneHotEncoderEnum.getEnumByType(curProcess.getOperatingCode());
        if (hotEncoderEnum == null) {
            log.error("工况类型未定义,不进行处理,operatingCode:{}", curProcess.getOperatingCode());
            return;
        }
        // 算法对应的工艺code
        String algorithmProcessCode = hotEncoderEnum.getAlgorithmProcessCode();

        List<Map<String, List<BigDecimal>>> curAlgorithmDataMapList = algorithmDataValMap.getOrDefault(algorithmProcessCode, new ArrayList<>());

        Map<String, List<BigDecimal>> curProcessAlgorithmDataValMap = new HashMap<>();
        for (IntervalDataDto dataDto : secDataList) {
            AlgorithmDataCodeDTO algorithmDataCodeDTO = algorithmDataCodeUtil.getByDataCode(dataDto.getDataCode());
            if (algorithmDataCodeDTO == null) {
                log.error("算法数据code未定义,不进行处理,dataCode:{}",dataDto.getDataCode());
                continue;
            }
            if (ObjectUtils.isEmpty(dataDto.getItemVal())) {
                log.error("数据值为空,不进行处理,dataCode:{}",dataDto.getDataCode());
                continue;
            }
            // 算法对应的参数code
            String algorithmParamCode = algorithmDataCodeDTO.getAlgorithmCode();
            List<BigDecimal> valList = curProcessAlgorithmDataValMap.getOrDefault(algorithmParamCode, new ArrayList<>());
            valList.add(dataDto.getItemVal());
            curProcessAlgorithmDataValMap.put(algorithmParamCode, valList);
        }

        curAlgorithmDataMapList.add(curProcessAlgorithmDataValMap);

        algorithmDataValMap.put(algorithmProcessCode, curAlgorithmDataMapList);
    }
}

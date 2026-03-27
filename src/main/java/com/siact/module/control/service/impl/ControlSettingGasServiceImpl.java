package com.siact.module.control.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.exception.BizException;
import com.siact.common.utils.ConvertUtils;
import com.siact.common.utils.JacksonUtils;
import com.siact.core.event.domain.GenericEvent;
import com.siact.core.event.notify.EventPublisher;
import com.siact.module.algorithm.dto.IntelliTplSettingDTO;
import com.siact.module.algorithm.dto.IntelliTplSettingDetailDTO;
import com.siact.module.algorithm.entity.IntelligentDataEntity;
import com.siact.module.algorithm.enums.IntelliTypeEnum;
import com.siact.module.algorithm.repository.IntelligentDataRepository;
import com.siact.module.base.service.TplService;
import com.siact.module.base.vo.TplVO;
import com.siact.module.control.convert.ControlSettingGasConvert;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.dto.GasForecastQueryDTO;
import com.siact.module.control.entity.ControlGasRecordEntity;
import com.siact.module.control.entity.ControlSettingGasEntity;
import com.siact.module.control.event.GasRecordSaveEventHandler;
import com.siact.module.control.mapper.ControlSettingGasMapper;
import com.siact.module.control.repository.ControlGasRecordRepository;
import com.siact.module.control.repository.ControlSettingGasRepository;
import com.siact.module.control.service.ControlSettingGasService;
import com.siact.module.control.support.ControlSettingSupport;
import com.siact.module.control.vo.GasForecastDataVO;
import com.siact.module.control.vo.GasForecastDataValueVO;
import com.siact.module.control.vo.GasForecastSeriesVO;
import com.siact.module.control.vo.GasForecastVO;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.sec.sevice.DataService;
import com.siact.sec.utils.IntervalTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ControlSettingGasServiceImpl extends ServiceImpl<ControlSettingGasMapper, ControlSettingGasEntity> implements ControlSettingGasService {
    private final ControlSettingGasRepository repository;
    private final ControlGasRecordRepository recordRepository;
    private final IntelligentDataRepository intelligentDataRepository;
    private final ControlSettingSupport support;
    private final EventPublisher publisher;
    private final ControlSettingGasConvert convert;
    private final DataService dataService;
    private final TplService tplService;

    /**
     * 查询天然气设定值
     */
    @Override
    public List<ControlSettingGasDTO> querySetting() {
        // 获取智控值
        Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> intelliValues = intelligentDataRepository.queryByTypeWithLastTime(
                IntelliTypeEnum.GAS_RUN_VALUE,
                IntelliTypeEnum.GAS_CALC_EXPERT2,
                IntelliTypeEnum.GAS_LAST_SUM,
                IntelliTypeEnum.GAS_DELTAC_EXPERT
        );

        List<ControlSettingGasDTO> result = new ArrayList<>();
        List<ControlSettingGasEntity> settingList = repository.queryValid();
        if (ObjectUtils.isEmpty(settingList)) {
            log.warn("获取天然气控制设定值信息, 配置为空");
            return result;
        }
        Map<String, ControlSettingGasEntity> gasSettingList = settingList.stream().collect(Collectors.toMap(ControlSettingGasEntity::getDataCode, v -> v));

        // 收集 dataCode
        List<String> dataCodeList = settingList.stream().map(ControlSettingGasEntity::getDataCode).distinct().collect(Collectors.toList());

        // 查询模板配置
        TplVO tpl = tplService.selectTplByCode("intelliOutputDataCode");
        IntelliTplSettingDTO intelliTplSettingDTO = JacksonUtils.fromJson(tpl.getTplContent(), IntelliTplSettingDTO.class);
        intelliTplSettingDTO.getDataCodeList().sort(Comparator.comparing(IntelliTplSettingDetailDTO::getName));

        // 根据 dataCode + 短码查询属性 Code
        String xlsShortCode = "XLS"; // 天然气设定流量短码
        List<String> propShortCode = Collections.singletonList(xlsShortCode);

        Map<String, String> secPropCodeMap = support.querySecPropCode(dataCodeList, propShortCode);
        // 查询DCS实时值, 根据 dataCode 查询
        JSONObject propCodeValObj = dataService.queryRealValue(String.join(",", secPropCodeMap.values()));
        // 封装返回结果
        intelliTplSettingDTO.getDataCodeList().forEach(detailDTO -> {
            ControlSettingGasEntity entity = gasSettingList.get(detailDTO.getDataCode());
            ControlSettingGasDTO dto = ObjectUtils.defaultIfNull(convert.toDTO(entity), new ControlSettingGasDTO());
            dto.setNumber(detailDTO.getName());
            dto.setDataCode(detailDTO.getDataCode());

            // 获取 dcs 运行值
            String mapKey = String.join(ConstantSymbol.UNDER_LINE, detailDTO.getDataCode(), xlsShortCode);
            BigDecimal xlsVal = propCodeValObj == null ? null : propCodeValObj.getBigDecimal(secPropCodeMap.get(mapKey));
            Double runningDcsVal = xlsVal == null ? null : xlsVal.doubleValue();
            dto.setRunningDcsVal(runningDcsVal);
            dto.setAlgoDiff(false);

            // 获取智能计算值
            // Map<IntelliTypeEnum, IntelligentDataEntity> intelliValueMap = intelliValues.getOrDefault(detailDTO.getDataCode(), Collections.emptyMap());
            // IntelligentDataEntity intelliRunValue = intelliValueMap.get(IntelliTypeEnum.GAS_RUN_VALUE);
            // IntelligentDataEntity intelliModelValue = intelliValueMap.get(IntelliTypeEnum.GAS_CALC_EXPERT2);
            // if (!Objects.isNull(intelliRunValue) && !Objects.isNull(intelliModelValue)) {
            //     dto.setGasAlgorithmCalcVal(intelliRunValue.getVal().add(intelliModelValue.getVal()).doubleValue());
            //     dto.setAdjustValue(intelliModelValue.getVal());
            // }
            Map<IntelliTypeEnum, IntelligentDataEntity> map = intelliValues.getOrDefault(detailDTO.getDataCode(), Collections.emptyMap());
            IntelligentDataEntity deltaC = map.get(IntelliTypeEnum.GAS_DELTAC_EXPERT);
            IntelligentDataEntity runVal = map.get(IntelliTypeEnum.GAS_LAST_SUM);
            if (!Objects.isNull(runVal) && !Objects.isNull(deltaC)) {
                // dto.setRunningDcsVal(runVal.getVal().doubleValue());
                dto.setGasAlgorithmCalcVal(runVal.getVal().add(deltaC.getVal()).doubleValue());
                dto.setAdjustValue(deltaC.getVal());
            }
            if (Objects.isNull(dto.getAutoState())) dto.setAutoState(false);
            result.add(dto);
        });
        this.setStatusAndRecord(result); // 记录状态并触发保存事件
        return result;
    }

    /* 设置信号灯状态和保存 dcs 天然气记录值 */
    private void setStatusAndRecord(List<ControlSettingGasDTO> list) {
        // 获取上一次天然气运行值
        Map<String, ControlGasRecordEntity> recordMap = recordRepository.queryWithLastTime();
        // 获取上一次智控输出
        Map<String, Map<IntelliTypeEnum, List<IntelligentDataEntity>>> intelliValues = intelligentDataRepository.queryByTypeAndLimit("limit 1, 1", IntelliTypeEnum.GAS_RUN_VALUE, IntelliTypeEnum.GAS_CALC_EXPERT2);
        Map<String, List<IntelligentDataEntity>> collect = intelliValues.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> entry.getValue().values().stream().flatMap(List::stream).collect(Collectors.toList()))
        );
        // 计算上一次智控输出
        Map<String, BigDecimal> lastAlgos = collect.entrySet().stream()
                .filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()) && entry.getValue().size() >= 2 && entry.getValue().stream().allMatch(Objects::nonNull))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().map(IntelligentDataEntity::getVal).reduce(BigDecimal.ZERO, BigDecimal::add)));


        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        List<ControlGasRecordEntity> records = list.stream().map(dto -> {
                    BigDecimal currentDcs = NumberUtils.toScaledBigDecimal(dto.getRunningDcsVal(), 2, RoundingMode.HALF_UP);
                    BigDecimal algo = NumberUtils.toScaledBigDecimal(dto.getGasAlgorithmCalcVal(), 2, RoundingMode.HALF_UP);
                    // dcs 运行值是否与智控值不同
                    boolean isDcsDiffAlgo = !currentDcs.equals(algo);
                    // 智控输出是否和上一次不一样
                    BigDecimal lastAlgo = NumberUtils.toScaledBigDecimal(lastAlgos.get(dto.getDataCode()), 2, RoundingMode.HALF_UP);
                    boolean isAlgoDiff = !lastAlgo.equals(algo);
                    // dcs 运行值是否和上一次不一样
                    ControlGasRecordEntity record = recordMap.get(dto.getDataCode());
                    boolean isDcsDiff = false;
                    if (ObjectUtils.isNotEmpty(record) && ObjectUtils.isNotEmpty(record.getDcs())) {
                        String last = record.getDcs().setScale(2, RoundingMode.HALF_UP).toPlainString();
                        String current = currentDcs.setScale(2, RoundingMode.HALF_UP).toPlainString();
                        isDcsDiff = !last.equals(current);
                    }
                    log.info("dcs 与智控值不同: {}, dcs 与上一次不同: {}, 智控值与上一次不同: {}", isDcsDiffAlgo, isAlgoDiff, isDcsDiff);
                    dto.setDcsDiff(isDcsDiffAlgo && isDcsDiff);
                    dto.setAlgoDiff(isDcsDiffAlgo && isAlgoDiff);

                    return ControlGasRecordEntity.builder().code(dto.getDataCode()).dcs(currentDcs).status(dto.getDcsDiff()).time(currentTime).build();
                }
        ).collect(Collectors.toList());

        publisher.publish(GenericEvent.of(GasRecordSaveEventHandler.EVENT_TYPE, records));
    }

    /**
     * 下发天然气设置
     */
    @Override
    public @Transactional(rollbackFor = BizException.class) Boolean publish(List<ControlSettingGasDTO> list) {
        // ps:手动下发, 不校验下发规则
        List<ControlSettingGasDTO> publishList = list.stream().filter(o -> ObjectUtils.isNotEmpty(o.getGasManualVal())).collect(Collectors.toList());

        // 获取历史数据, 对自动模式的数据, 将调整值设置为原本的数据, 对手动模式的调整值保持
        List<ControlSettingGasEntity> settingList = repository.queryValid();
        for (ControlSettingGasDTO dto : publishList) {
            if (dto.getAutoState()) {
                // 自动模式的设定值设置为原数据的设定值
                settingList.stream().filter(gas -> gas.getDataCode().equals(dto.getDataCode())).findFirst().ifPresent(
                        entity -> dto.setGasManualVal(entity.getGasManualVal().doubleValue())
                );
            }
        }
        List<String> publishGasDataCodeList = publishList.stream().map(ControlSettingGasDTO::getDataCode).collect(Collectors.toList());

        // 1保存控制设定值
        // 1.1:先删除旧数据
        repository.deleteByDataCode(publishGasDataCodeList);
        // 1.2:再新增
        repository.save(publishList);

        // 手动下发 TODO 目前暂无下发逻辑对接,暂时返回成功
        return true;
    }

    @Override
    public GasForecastVO forecast(GasForecastQueryDTO query) {
        List<String> dataCodes = query.getDataCodes();
        List<String> names = query.getNames();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        String tsUnit = query.getTsUnit();
        Integer ts = query.getTs();
        String formatVal = query.getFormatVal();
        String calcType = query.getCalcType();

        // 获取当前时间（秒归零）
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00"));

        // 判断时间区间
        boolean hasDcsData = startTime.compareTo(now) < 0; // 开始时间 < 当前时间
        boolean hasForecastData = endTime.compareTo(now) > 0; // 结束时间 > 当前时间

        // DCS 数据查询区间：startTime ~ min(endTime, now)
        String dcsEndTime = hasForecastData ? now : endTime;

        // 1. 查询 DCS 历史数据
        List<IntervalDataDto> dcsDataList = Collections.emptyList();
        if (hasDcsData) {
            IntervalValParamsDto dcsParams = ConvertUtils.sourceToTarget(query, IntervalValParamsDto.class);
            dcsParams.setEndTime(dcsEndTime);
            dcsDataList = dataService.queryIntervalVal(dcsParams);
        }

        // 按 dataCode 分组 DCS 数据
        Map<String, List<IntervalDataDto>> dcsDataMap = dcsDataList.stream()
                .collect(Collectors.groupingBy(IntervalDataDto::getDataCode));

        // 2. 查询智能计算数据（GAS_DELTAC_EXPERT + GAS_LAST_SUM）
        // Forecast 数据查询区间：now ~ endTime
        Map<String, Map<IntelliTypeEnum, List<IntelligentDataEntity>>> intelliDataMap = Collections.emptyMap();
        if (hasForecastData) {
            intelliDataMap = intelligentDataRepository.queryByTypeAndTimeRange(
                    dataCodes,
                    Arrays.asList(IntelliTypeEnum.GAS_DELTAC_EXPERT, IntelliTypeEnum.GAS_LAST_SUM),
                    now,
                    endTime
            );
        }

        // 3. 生成时间轴
        List<String> xdata = IntervalTimeUtil.getIntervalTimeList(startTime, endTime, tsUnit, ts, formatVal);

        // 4. 组装 series 数据
        List<GasForecastSeriesVO> seriesList = new ArrayList<>();
        for (int i = 0; i < dataCodes.size(); i++) {
            String dataCode = dataCodes.get(i);
            String name = CollectionUtils.isNotEmpty(names) && names.size() > i ? names.get(i) : dataCode;

            // DCS 数据
            List<Object[]> dcsValues = new ArrayList<>();
            List<IntervalDataDto> dcsList = dcsDataMap.getOrDefault(dataCode, Collections.emptyList());
            for (IntervalDataDto dto : dcsList) {
                String formattedTime = IntervalTimeUtil.dateFormat(dto.getTime(), formatVal);
                dcsValues.add(new Object[]{formattedTime, dto.getItemVal()});
            }
            GasForecastDataValueVO dcsValueVO = hasDcsData ? new GasForecastDataValueVO("运行值", dcsValues) : null;

            // 预测数据 = GAS_DELTAC_EXPERT + GAS_LAST_SUM
            List<Object[]> forecastValues = new ArrayList<>();
            if (hasForecastData) {
                Map<IntelliTypeEnum, List<IntelligentDataEntity>> typeDataMap = intelliDataMap.getOrDefault(dataCode, Collections.emptyMap());
                List<IntelligentDataEntity> deltaCList = typeDataMap.getOrDefault(IntelliTypeEnum.GAS_DELTAC_EXPERT, Collections.emptyList());
                List<IntelligentDataEntity> lastSumList = typeDataMap.getOrDefault(IntelliTypeEnum.GAS_LAST_SUM, Collections.emptyList());

                // 按 time 分组，计算预测值
                Map<String, BigDecimal> deltaCByTime = deltaCList.stream()
                        .collect(Collectors.toMap(IntelligentDataEntity::getTime, IntelligentDataEntity::getVal, (v1, v2) -> v1));
                Map<String, BigDecimal> lastSumByTime = lastSumList.stream()
                        .collect(Collectors.toMap(IntelligentDataEntity::getTime, IntelligentDataEntity::getVal, (v1, v2) -> v1));

                // 合并时间点，计算预测值
                Set<String> allTimes = new TreeSet<>();
                allTimes.addAll(deltaCByTime.keySet());
                allTimes.addAll(lastSumByTime.keySet());
                for (String time : allTimes) {
                    BigDecimal deltaC = deltaCByTime.getOrDefault(time, BigDecimal.ZERO);
                    BigDecimal lastSum = lastSumByTime.getOrDefault(time, BigDecimal.ZERO);
                    String formattedTime = IntervalTimeUtil.dateFormat(time, formatVal);
                    forecastValues.add(new Object[]{formattedTime, deltaC.add(lastSum)});
                }
            }
            GasForecastDataValueVO forecastValueVO = hasForecastData ? new GasForecastDataValueVO("预测值", forecastValues) : null;

            // 构建 GasForecastDataVO
            GasForecastDataVO dataVO = GasForecastDataVO.builder()
                    .dcs(dcsValueVO)
                    .forecast(forecastValueVO)
                    .build();

            // 构建 GasForecastSeriesVO
            GasForecastSeriesVO seriesVO = GasForecastSeriesVO.builder()
                    .dataCode(dataCode)
                    .name(name)
                    .data(dataVO)
                    .build();
            seriesList.add(seriesVO);
        }

        return GasForecastVO.builder()
                .xdata(xdata)
                .series(seriesList)
                .build();
    }

    @Override
    public List<Map<String, String>> queryForecastConfig() {
        TplVO tpl = tplService.selectTplByCode("intelliOutputDataCode");
        IntelliTplSettingDTO intelliTplSettingDTO = JacksonUtils.fromJson(tpl.getTplContent(), IntelliTplSettingDTO.class);
        intelliTplSettingDTO.getDataCodeList().sort(Comparator.comparing(IntelliTplSettingDetailDTO::getName));

        return intelliTplSettingDTO.getDataCodeList().stream()
                .filter(detail -> Boolean.TRUE.equals(detail.getActive()))
                .map(detail -> {
                    Map<String, String> item = new HashMap<>();
                    item.put("label", detail.getName());
                    item.put("value", detail.getDataCode());
                    return item;
                })
                .collect(Collectors.toList());
    }
}

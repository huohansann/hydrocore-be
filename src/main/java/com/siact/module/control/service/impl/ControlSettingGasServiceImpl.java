package com.siact.module.control.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.siact.module.control.vo.GasForecastDataVO;
import com.siact.module.control.vo.GasForecastDataValueVO;
import com.siact.module.control.vo.GasForecastSeriesVO;
import com.siact.module.control.vo.GasForecastVO;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.sec.sevice.DataService;
import com.siact.sec.utils.IntervalTimeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
    private final EventPublisher publisher;
    private final ControlSettingGasConvert convert;
    private final DataService dataService;
    private final TplService tplService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 测试模式开关：true 时从 JSON 文件读取测试数据，false 时执行正常业务逻辑 */
    @Value("${forecast.test-mode.enabled:false}")
    private boolean testModeEnabled;

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

        // 查询模板配置
        TplVO tpl = tplService.selectTplByCode("intelliOutputDataCode");
        IntelliTplSettingDTO intelliTplSettingDTO = JacksonUtils.fromJson(tpl.getTplContent(), IntelliTplSettingDTO.class);
        intelliTplSettingDTO.getDataCodeList().sort(Comparator.comparing(IntelliTplSettingDetailDTO::getName));

        // 查询 DCS 实时值
        List<String> tplDataCodes = intelliTplSettingDTO.getDataCodeList().stream()
                .map(IntelliTplSettingDetailDTO::getDataCode)
                .collect(Collectors.toList());
        JSONObject dcsRealValues = dataService.queryRealValue(String.join(",", tplDataCodes));

        // 获取DCS总气量
        BigDecimal finalTotalDcsVal = getTotalDcsValue(intelliTplSettingDTO, dcsRealValues, testModeEnabled);
        // 获取智控总气量
        BigDecimal finalTotalGasVal = getTotalGasValue(intelliValues);

        // 封装返回结果
        intelliTplSettingDTO.getDataCodeList().forEach(detailDTO -> {
            ControlSettingGasEntity entity = gasSettingList.get(detailDTO.getDataCode());
            ControlSettingGasDTO dto = ObjectUtils.defaultIfNull(convert.toDTO(entity), new ControlSettingGasDTO());
            dto.setNumber(detailDTO.getName());
            dto.setDataCode(detailDTO.getDataCode());

            // 获取 DCS 运行值
            BigDecimal dcsVal = dcsRealValues == null ? null : dcsRealValues.getBigDecimal(detailDTO.getDataCode());

            // 增加测试数据
            if(testModeEnabled && !dto.getNumber().equals("总气量")) {
                dcsVal = new BigDecimal("15.1");
            } else if(testModeEnabled) {
                dcsVal = new BigDecimal(5720);
            }

            // 计算DCS运行值
            Double dcsValDouble = dcsAlgorithm(dcsVal, finalTotalDcsVal, detailDTO.getIsMaster());
            // 计算智控计算值
            // 目前智控计算逻辑同DCS,DCS为: DCS总值为数值,DCS当前值为百分比, 总值 * 百分比; 智控为: 智控总值为数值, 智控总值为数值 * DCS当前值
            Double gasValDouble = dcsAlgorithm(dcsVal, finalTotalGasVal, detailDTO.getIsMaster());

            dto.setGasAlgorithmCalcVal(gasValDouble);
            dto.setRunningDcsVal(dcsValDouble);
            dto.setAlgoDiff(false);

            // 获取智能计算值
            Map<IntelliTypeEnum, IntelligentDataEntity> map = intelliValues.getOrDefault(detailDTO.getDataCode(), Collections.emptyMap());
            IntelligentDataEntity deltaC = map.get(IntelliTypeEnum.GAS_DELTAC_EXPERT);
            IntelligentDataEntity runVal = map.get(IntelliTypeEnum.GAS_LAST_SUM);
            if (!Objects.isNull(runVal) && !Objects.isNull(deltaC)) {
//                 dto.setRunningDcsVal(runVal.getVal().doubleValue());
//                dto.setGasAlgorithmCalcVal(runVal.getVal().add(deltaC.getVal()).doubleValue());
                dto.setAdjustValue(deltaC.getVal());
            }
            if (Objects.isNull(dto.getAutoState())) dto.setAutoState(false);
            result.add(dto);
        });
        this.setStatusAndRecord(result); // 记录状态并触发保存事件
        return result;
    }

    /**
     * @author: HouBo
     * @CreateTime: 2026/3/31 16:08
     * @Description: DCS运行值算法
     * 目前的规则: 总气量为数据, 如: 5720; 当前气量为百分比, 如: 15.1, 则: 15.1% * 5720 = 871.8
     */
    private Double dcsAlgorithm(BigDecimal dcsVal, BigDecimal totalDcsVal, boolean isMaster) {
        // 找不到总气量，不参与计算
        if (totalDcsVal == null || totalDcsVal.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        // 找不到当前气量，不参与计算
        if(dcsVal == null) {
            return null;
        }
        // 当前为总气量, 无需计算
        if(isMaster) {
            return totalDcsVal.doubleValue();
        }

        // 将百分比 dcsVal 除以 100，得到实际的小数比例
        BigDecimal percentage = dcsVal.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        // 使用 totalDcsVal 乘以计算出的比例
        return totalDcsVal.multiply(percentage).doubleValue();
    }

    /**
     * @author: HouBo
     * @CreateTime: 2026/4/1 9:57
     * @Description: 获取DCS总气量
     * @param intelliTplSettingDTO 配置模板
     * @param dcsRealValues        DCS实时数据源
     * @param testModeEnabled      是否开启测试模式
     * @return 总气量 BigDecimal 值
     */
    private BigDecimal getTotalDcsValue(IntelliTplSettingDTO intelliTplSettingDTO,
                                        JSONObject dcsRealValues,
                                        boolean testModeEnabled) {
        // 1. 测试模式直接返回固定值
        if (testModeEnabled) {
            return new BigDecimal("5720");
        }

        // 2. 从配置列表中筛选出 master 为 true 的明细
        IntelliTplSettingDetailDTO masterDetail = intelliTplSettingDTO.getDataCodeList().stream()
                .filter(detail -> detail != null && Boolean.TRUE.equals(detail.getIsMaster()))
                .findFirst()
                .orElse(null);

        BigDecimal totalDcsVal;
        if (masterDetail == null) {
            totalDcsVal = BigDecimal.ZERO;
            log.warn("窑炉控制：未能找到DCS总气量");
        } else {
            if (dcsRealValues == null) {
                totalDcsVal = null;
            } else {
                totalDcsVal = dcsRealValues.getBigDecimal(masterDetail.getDataCode());
            }
        }

        return totalDcsVal;
    }

    /**
     * @author: HouBo
     * @CreateTime: 2026/4/1 10:17
     * @Description: 获取智控总气量
     * @return 智控总气量 BigDecimal 值
     */
    private BigDecimal getTotalGasValue(Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> intelliValues) {
        if (intelliValues == null || intelliValues.isEmpty()) {
            log.warn("窑炉控制：未能找到智控总气量");
            return BigDecimal.ZERO;
        }
        // 从嵌套 Map 中提取所有实体，并过滤出需要的两种类型进行求和
        return intelliValues.values().stream()
                .flatMap(map -> map.values().stream()) // 展开内层 Map 的所有实体
                .filter(entity -> entity != null && entity.getVal() != null && (
                        IntelliTypeEnum.GAS_DELTAC_EXPERT.equals(entity.getIntelliType()) ||
                                IntelliTypeEnum.GAS_LAST_SUM.equals(entity.getIntelliType())
                ))
                .map(IntelligentDataEntity::getVal) // 提取 val (BigDecimal)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 求和
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
        // 测试模式：从 JSON 文件读取测试数据
        if (testModeEnabled) {
            log.info("测试模式开启，从 JSON 文件读取测试数据");
            try {
                ClassPathResource resource = new ClassPathResource("testJson/forecast/forecast.json");
                InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                String jsonContent = FileCopyUtils.copyToString(reader);
                return objectMapper.readValue(jsonContent, GasForecastVO.class);
            } catch (Exception e) {
                log.error("读取测试 JSON 文件失败", e);
                // 读取失败时降级为正常模式
                log.warn("降级为正常模式执行查询");
            }
        }

        // 正常模式：执行原有业务逻辑
        List<String> dataCodes = query.getDataCodes();
        List<String> names = query.getNames();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        String tsUnit = query.getTsUnit();
        Integer ts = query.getTs();
        String formatVal = query.getFormatVal();

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

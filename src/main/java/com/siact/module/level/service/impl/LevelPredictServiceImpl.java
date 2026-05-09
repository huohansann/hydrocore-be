package com.siact.module.level.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.siact.common.utils.ConvertUtils;
import com.siact.common.constant.ConstantTime;
import com.siact.module.level.entity.LevelAlgorithmResultEntity;
import com.siact.module.level.entity.LevelControlConfigEntity;
import com.siact.module.level.query.LevelPredictCurveQuery;
import com.siact.module.level.repository.LevelControlConfigRepository;
import com.siact.module.level.service.LevelAlgorithmResultService;
import com.siact.module.level.service.LevelPredictService;
import com.siact.module.level.vo.LevelCurveDataVO;
import com.siact.module.level.vo.LevelPredictCurveSeriesVO;
import com.siact.module.level.vo.LevelPredictCurveVO;
import com.siact.module.level.vo.LevelRealtimeVO;
import com.siact.module.system.constants.SysConfigCodeConstants;
import com.siact.module.system.dto.SysConfigDTO;
import com.siact.module.system.service.SysConfigService;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.sec.utils.IntervalTimeUtil;
import com.siact.tdengine.service.TaosDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class LevelPredictServiceImpl implements LevelPredictService {

    private final TaosDataService taosDataService;
    private final SysConfigService sysConfigService;
    private final LevelControlConfigRepository configRepository;
    private final LevelAlgorithmResultService algorithmResultService;

    @SuppressWarnings("unchecked")
    private Map<String, String> getDataCodes() {
        SysConfigDTO config = sysConfigService.getByCode(SysConfigCodeConstants.LEVEL_CONTROL_DATACODES);
        if (config != null && config.getData() instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) config.getData();
            Map<String, String> result = new HashMap<>();
            if (dataMap.get("level") != null) {
                result.put("level", String.valueOf(dataMap.get("level")));
            }
            if (dataMap.get("opening") != null) {
                result.put("opening", String.valueOf(dataMap.get("opening")));
            }
            return result;
        }
        return Collections.emptyMap();
    }

    @Override
    public LevelRealtimeVO getRealtimeData() {
        Map<String, String> codes = getDataCodes();
        String levelCode = codes.get("level");
        String openingCode = codes.get("opening");

        LevelRealtimeVO vo = new LevelRealtimeVO();

        // TDengine: 一次查询获取液位+开度实时值
        if (levelCode != null || openingCode != null) {
            List<String> codeList = new ArrayList<>();
            if (levelCode != null) codeList.add(levelCode);
            if (openingCode != null) codeList.add(openingCode);
            JSONObject realtimeValues = taosDataService.queryRealValue(String.join(",", codeList));

            if (realtimeValues != null) {
                if (levelCode != null && realtimeValues.containsKey(levelCode)) {
                    vo.setLevel(realtimeValues.getBigDecimal(levelCode));
                }
                if (openingCode != null && realtimeValues.containsKey(openingCode)) {
                    vo.setOpening(realtimeValues.getBigDecimal(openingCode));
                }
            }
        }

        // MySQL: 模式
        if (levelCode != null) {
            LevelControlConfigEntity configEntity = configRepository.getByDataCode(levelCode);
            if (configEntity != null) {
                vo.setMode(configEntity.getMode());
            }
        }

        // MySQL: 液位状态 + 趋势 + 推荐开度
        LevelAlgorithmResultEntity algoResult = algorithmResultService.getResult(levelCode);
        if (algoResult != null) {
            vo.setLevelStatus(algoResult.getLevelStatus());
            vo.setLevelTrend(algoResult.getLevelTrend());
            vo.setRecommendedOpening(algoResult.getRecommendedOpening());
        }

        return vo;
    }

    @Override
    public LevelPredictCurveVO queryPredictCurve(LevelPredictCurveQuery query) {
        Map<String, String> codes = getDataCodes();
        String levelCode = codes.get("level");
        String openingCode = codes.get("opening");

        // 构建查询参数, 同时查询液位和开度
        List<String> dataCodeList = new ArrayList<>();
        Map<String, String> codeNameMap = new LinkedHashMap<>();
        if (levelCode != null) {
            dataCodeList.add(levelCode);
            codeNameMap.put(levelCode, "液位");
        }
        if (openingCode != null) {
            dataCodeList.add(openingCode);
            codeNameMap.put(openingCode, "开度");
        }

        IntervalValParamsDto paramsDto = ConvertUtils.sourceToTarget(query, IntervalValParamsDto.class);
        paramsDto.setEndTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME_MM_00)));
        paramsDto.setDataCodes(dataCodeList);
        if (paramsDto.getCalcType() == null) {
            paramsDto.setCalcType("AVG");
        }
        if (paramsDto.getFormatVal() == null) {
            paramsDto.setFormatVal("HH:mm");
        }

        List<IntervalDataDto> dataList = taosDataService.queryIntervalVal(paramsDto);

        // 按 dataCode 分组
        Map<String, List<IntervalDataDto>> groupedData = dataList.stream()
                .collect(Collectors.groupingBy(IntervalDataDto::getDataCode));

        // 构建时间轴
        List<String> xdata = IntervalTimeUtil.getIntervalTimeList(query.getStartTime(), query.getEndTime(), query.getTsUnit(), query.getTs(), query.getFormatVal());

        // 为每个 dataCode 构建 series
        List<LevelPredictCurveSeriesVO> seriesList = new ArrayList<>();
        // Random random = new Random();
        for (Map.Entry<String, String> entry : codeNameMap.entrySet()) {
            String dataCode = entry.getKey();
            String name = entry.getValue();
            List<IntervalDataDto> codeData = groupedData.getOrDefault(dataCode, Collections.emptyList());

            List<Object[]> actualValues = codeData.stream()
                    .map(d -> new Object[]{d.getTime(), d.getItemVal()})
                    .collect(Collectors.toList());

            // 测试数据: 开发环境测试使用
            // if (actualValues.isEmpty()) {
            //     double base = "液位".equals(name) ? 1.5 : 45.0;
            //     double range = "液位".equals(name) ? 0.5 : 15.0;
            //     for (String time : xdata) {
            //         actualValues.add(new Object[]{time, BigDecimal.valueOf(base + random.nextDouble() * range - range / 2).setScale(2, BigDecimal.ROUND_HALF_UP)});
            //     }
            // }

            Map<String, LevelCurveDataVO> dataMap = new LinkedHashMap<>();
            dataMap.put("actual", new LevelCurveDataVO(name + "实际值", actualValues));

            seriesList.add(LevelPredictCurveSeriesVO.builder()
                    .dataCode(dataCode)
                    .name(name)
                    .data(dataMap)
                    .build());
        }

        return LevelPredictCurveVO.builder()
                .xdata(xdata)
                .series(seriesList)
                .build();
    }
}

package com.siact.module.forecast.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.siact.api.common.api.vo.common.InfoListQueryVo;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.constant.ConstantTime;
import com.siact.common.utils.ConvertUtils;
import com.siact.common.utils.TimeUtil;
import com.siact.module.base.dto.BasicDataDTO;
import com.siact.module.base.dto.ColumnChartDTO;
import com.siact.module.base.dto.ControlIntervalConfigHisChartDataDTO;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.service.TplService;
import com.siact.module.base.vo.TplVO;
import com.siact.module.forecast.dto.ForecastKilnParamsDTO;
import com.siact.module.forecast.dto.PredictionDataShowTplDTO;
import com.siact.module.forecast.query.TempForecastQuery;
import com.siact.module.forecast.service.ForecastKilnService;
import com.siact.module.forecast.support.ForecastSupport;
import com.siact.module.forecast.vo.*;
import com.siact.module.predicted.dto.PredictedDataDTO;
import com.siact.module.predicted.entity.PredictedDataEntity;
import com.siact.module.predicted.enums.PredictedTypeEnum;
import com.siact.module.predicted.repository.PredictedDataRepository;
import com.siact.module.predicted.service.PredictedDataService;
import com.siact.sec.dto.CommonChartParamsDto;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.sec.dto.PropInsDTO;
import com.siact.sec.sevice.DataService;
import com.siact.sec.utils.CommonHandle;
import com.siact.sec.utils.IntervalTimeUtil;
import com.siact.sec.vo.CommonChartParamsVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : zhangwentao, kzuo
 * @date : 2025-05-26 10:33
 * @description :
 */
@Slf4j
@Service
public class ForecastKilnServiceImpl implements ForecastKilnService {
    private @Resource PredictedDataRepository predictedDataRepository;
    private @Resource DataService dataService;
    private @Resource TplService tplService;
    private @Resource PredictedDataService predictedDataService;
    private @Resource ControlIntervalConfigService controlIntervalConfigService;
    private @Resource ForecastSupport support;


    /**
     * 通过tplcode获取菜单信息
     *
     * @param tplcode
     * @return
     */
    @Override
    public List<ForecastKilnMenuVO> queryForecastKilnMenu(String tplcode) {
        // 获取模板信息
        TplVO tplVO = tplService.selectTplByCode(tplcode);
        if (tplVO == null) {
            throw new RuntimeException("模板不存在");
        }
        JSONArray params = JSONObject.parseArray(tplVO.getTplContent());
        return params != null ? params.toJavaList(ForecastKilnMenuVO.class) : null;
    }

    /**
     * 获取窑炉预测信息
     */
    @Override
    public List<LineChartVO> queryForecastInfo(ForecastKilnParamsDTO projectPropVO) {
        // 解析参数
        // 获取当前时间：yyyy-MM-dd HH:mm:ss // 由于要查询点位数据,因此需要将秒位归0
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00"));
        // 查询历史数据
        CommonChartParamsVo queryHisParamVo = ConvertUtils.sourceToTarget(projectPropVO, CommonChartParamsVo.class);
        // 历史数据截止到当前时刻
        queryHisParamVo.setEndTime(now);
        List<IntervalDataDto> intervalDataDtos = getHistoryIntervalDataVal(queryHisParamVo);

        // 从intervalDataDtos当中解析出来每个dataCode,time最新的一条数据 k:dataCode,v:IntervalDataDto
        Map<String, IntervalDataDto> latestHistoryDataMap = new HashMap<>();
        for (IntervalDataDto data : intervalDataDtos) {
            latestHistoryDataMap.merge(data.getDataCode(), data,
                    (oldVal, newVal) -> newVal.getTime().compareTo(oldVal.getTime()) > 0 ? newVal : oldVal);
        }

        // 封装数据
        CommonChartParamsDto acturalParamsDto = ConvertUtils.sourceToTarget(queryHisParamVo, CommonChartParamsDto.class);
        // 组装历史数据
        ColumnChartDTO historyData = CommonHandle.buildColumnChartDTO(acturalParamsDto, intervalDataDtos);

        ColumnChartDTO singlePredictionData = null;
        ColumnChartDTO multiPredictionData = null;
        // 查询预测数据
        CommonChartParamsVo queryForecastParams = ConvertUtils.sourceToTarget(projectPropVO, CommonChartParamsVo.class);
        // 预测数据从当前时刻算起
        if (now.compareTo(projectPropVO.getEndTime()) <= 0) {

            // 25.09.19 逻辑修改,由于要将实际数据和预测数据连接起来,因此预测数据的时间往后移动一分钟查询,后续补充实际值的最后一个点位,latestHistoryDataMap
            String predictionStartTime = TimeUtil.getCalcTime(now, 1, "MIN");
            queryForecastParams.setStartTime(predictionStartTime);

            // 获取预测数据
            Map<Integer, List<PredictedDataDTO>> predictedData = predictedDataService.getPredictedDataByTypes(projectPropVO.getDataCodes(), Arrays.asList(1, 2), queryForecastParams.getStartTime(), queryForecastParams.getEndTime());
            List<IntervalDataDto> singlePredictionDataList = ConvertUtils.sourceToTarget(predictedData.get(1), IntervalDataDto.class);
            List<IntervalDataDto> multiPredictionDataList = ConvertUtils.sourceToTarget(predictedData.get(2), IntervalDataDto.class);
            // 组装预测数据
            CommonChartParamsDto commonChartParamsDto = ConvertUtils.sourceToTarget(queryForecastParams, CommonChartParamsDto.class);
            // 组装单步预测数据
            singlePredictionData = CommonHandle.buildColumnChartDTO(commonChartParamsDto, singlePredictionDataList);
            // 组装多步预测数据
            multiPredictionData = CommonHandle.buildColumnChartDTO(commonChartParamsDto, multiPredictionDataList);
        }
        // 获取总的时间轴
        List<String> timeList = IntervalTimeUtil.getIntervalTimeList(projectPropVO.getStartTime(), projectPropVO.getEndTime(), projectPropVO.getTsUnit(), projectPropVO.getTs(), projectPropVO.getFormatVal());
        // 组装结果
        return buildLineChartVO(projectPropVO, historyData, latestHistoryDataMap, singlePredictionData, multiPredictionData, timeList);
    }

    @Override
    public List<LineChartVO> queryKilnForecastInfo(ForecastKilnParamsDTO projectPropVO) {
        // 解析参数
        CommonChartParamsVo params = ConvertUtils.sourceToTarget(projectPropVO, CommonChartParamsVo.class);

        // 修改温度预测界面时间线开始时间, 修改为当前时刻
        params.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME_MM_00)));
        // 查询获取预测数据
        Map<Integer, List<PredictedDataDTO>> predictedData = predictedDataService.getPredictedDataByTypes(projectPropVO.getDataCodes(), Arrays.asList(1, 2), params.getStartTime(), params.getEndTime());
        List<IntervalDataDto> singlePredictionDataList = ConvertUtils.sourceToTarget(predictedData.get(1), IntervalDataDto.class);
        List<IntervalDataDto> multiPredictionDataList = ConvertUtils.sourceToTarget(predictedData.get(2), IntervalDataDto.class);

        // 组装预测数据
        CommonChartParamsDto commonChartParamsDto = ConvertUtils.sourceToTarget(params, CommonChartParamsDto.class);
        // 组装单步预测数据
        ColumnChartDTO singlePredictionData = CommonHandle.buildColumnChartDTO(commonChartParamsDto, singlePredictionDataList);
        // 组装多步预测数据
        ColumnChartDTO multiPredictionData = CommonHandle.buildColumnChartDTO(commonChartParamsDto, multiPredictionDataList);
        // 获取总的时间轴
        List<String> timeList = IntervalTimeUtil.getIntervalTimeList(projectPropVO.getStartTime(), projectPropVO.getEndTime(), projectPropVO.getTsUnit(), projectPropVO.getTs(), projectPropVO.getFormatVal());
        // 组装结果
        return buildKilnForecastInfoVO(projectPropVO, null, singlePredictionData, multiPredictionData, timeList);
    }


    /**
     * 组装结果
     */
    private List<LineChartVO> buildLineChartVO(ForecastKilnParamsDTO projectPropVO, ColumnChartDTO historyData, Map<String, IntervalDataDto> latestHistoryDataMap, ColumnChartDTO singlePredictionData, ColumnChartDTO multiPredictionData, List<String> timeList) {
        // 组装结果
        // 获取 dataCode
        List<String> dataCodeList = projectPropVO.getDataCodes();
        // 获取 dataCode 对应的属性名称
        List<String> dataNameList = projectPropVO.getNames();

        // 获取参数属性
        // 获取单步预测属性信息
        List<BasicDataDTO> acturalDataList = historyData != null && historyData.getData() != null ? historyData.getData() : Collections.emptyList();
        Map<String, List<Object[]>> acturalDataMap = acturalDataList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 获取单步预测属性信息
        List<BasicDataDTO> singlePredictionDataList = singlePredictionData != null && singlePredictionData.getData() != null ? singlePredictionData.getData() : Collections.emptyList();
        // 单步预测数据 k:dataCode v:时间轴数据
        Map<String, List<Object[]>> singlePredictionDataMap = singlePredictionDataList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 获取多步预测属性信息
        List<BasicDataDTO> multiBasicDataDTOList = multiPredictionData != null && multiPredictionData.getData() != null ? multiPredictionData.getData() : Collections.emptyList();
        // 多步预测数据 k:dataCode v:时间轴数据
        Map<String, List<Object[]>> multiPredictionDataMap = multiBasicDataDTOList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 25.09.19 追加逻辑预测值的第一个点取的是运行值的最后一个点位的数据值(目的:折线图展示时需要将实际值和预测值的点位连接起来)
        // 根据latestHistoryDataMap,向singlePredictionDataMap当中追加第一条数据
        for (Map.Entry<String, IntervalDataDto> entry : latestHistoryDataMap.entrySet()) {
            String dataCode = entry.getKey();
            IntervalDataDto latestData = entry.getValue();
            // 处理单步预测数据
            List<Object[]> singleData = singlePredictionDataMap.get(dataCode);
            if (ObjectUtils.isNotEmpty(latestData) && ObjectUtils.isNotEmpty(latestData.getItemVal()) && ObjectUtils.isNotEmpty(singleData) && !ConstantSymbol.SHORT_LINE.equals(singleData.get(0)[1])) {
                /*
                // 追加第一条数据
                singleData.add(0, new Object[]{latestData.getTime(), latestData.getItemVal()});
                */
                // 2025.12.10 单步预测波动较大, 对整体数据进行偏移, 偏移量为运行值最后一个和预测值第一个的差值
                BigDecimal singleFirstValue = new BigDecimal((String) singleData.get(0)[1]);
                BigDecimal subtract = latestData.getItemVal().subtract(singleFirstValue);
                singleData.forEach(v -> {
                    if (!ConstantSymbol.SHORT_LINE.equals(v[1])) v[1] = (new BigDecimal((String) v[1])).add(subtract);
                });
            }

            // 处理多步预测数据
            List<Object[]> multiData = multiPredictionDataMap.get(dataCode);
            if (ObjectUtils.isNotEmpty(multiData)) {
                // 追加第一条数据
                multiData.add(0, new Object[]{latestData.getTime(), latestData.getItemVal()});
            }
        }

        // 获取上下控制值 上下告警值  温度设定线
        // k: dataCode v: 各设定值chart格式数据
        Map<String, ControlIntervalConfigHisChartDataDTO> dataCodeConfigChartMap =
                controlIntervalConfigService.queryHistoryConfigChart(dataCodeList, projectPropVO.getStartTime(), projectPropVO.getEndTime(), projectPropVO.getTs(), projectPropVO.getTsUnit(), projectPropVO.getFormatVal());

        List<PredictionDataShowTplDTO> dataShowList = tplService.getListByCode("kilnPredictionDataShow", PredictionDataShowTplDTO.class);
        Map<String, PredictionDataShowTplDTO> dataShowMap =
                dataShowList.stream().collect(Collectors.toMap(PredictionDataShowTplDTO::getDataCode,
                        o -> o, (v1, v2) -> v1));


        List<LineChartVO> lineChartVOList = new ArrayList<>();
        for (int i = 0; i < dataCodeList.size(); i++) {
            String dataCode = dataCodeList.get(i);
            LineChartVO lineChartVO = new LineChartVO();
            String dataName = dataNameList != null && dataNameList.size() > i ? dataNameList.get(i) : null;
            lineChartVO.setName(StringUtils.isNotBlank(dataName) ? dataName + "趋势" : null);
            lineChartVO.setDataCode(dataCode);
            LineChartDataVO lineChartDataVO = new LineChartDataVO();
            lineChartDataVO.setXData(timeList);

            List<Object[]> actualChartData = acturalDataMap.get(dataCode);

            ControlIntervalConfigHisChartDataDTO configHisChartDTO = dataCodeConfigChartMap.get(dataCode);

            // 处理需要返回的数据
            SeriesDataVO seriesDataVO = handleRtnData(dataShowMap, dataCode, actualChartData, singlePredictionDataMap, multiPredictionDataMap, configHisChartDTO);

            lineChartDataVO.setSeriesData(seriesDataVO);
            lineChartVO.setData(lineChartDataVO);
            // 设置当前dataCode 的上下控制值 上下告警值  温度设定线
            lineChartDataVO.setMaxUpControlVal(configHisChartDTO.getMaxUpControlVal());
            lineChartDataVO.setMinLowControlVal(configHisChartDTO.getMinLowControlVal());
            lineChartDataVO.setMaxUpAlarmVal(configHisChartDTO.getMaxUpAlarmVal());
            lineChartDataVO.setMinLowAlarmVal(configHisChartDTO.getMinLowAlarmVal());
            lineChartDataVO.setMaxTemperatureSetVal(configHisChartDTO.getMaxTemperatureSetVal());
            lineChartDataVO.setMinTemperatureSetVal(configHisChartDTO.getMinTemperatureSetVal());

            lineChartVOList.add(lineChartVO);
        }
        return lineChartVOList;
    }

//    private List<LineChartVO> buildLineChartVO(
//            ForecastKilnParamsDTO projectPropVO,
//            ColumnChartDTO historyData,
//            Map<String, IntervalDataDto> latestHistoryDataMap,
//            ColumnChartDTO singlePredictionData,
//            ColumnChartDTO multiPredictionData,
//            List<String> timeList) {
//        // 组装结果
//        // 获取dataCode
//        List<String> dataCodeList = projectPropVO.getDataCodes();
//        // 获取dataCode对应的属性名称
//        List<String> dataNameList = projectPropVO.getNames();
//
//        // 获取参数属性
//        // 获取单步预测属性信息
//        List<BasicDataDTO> acturalDataList = historyData != null && historyData.getData() != null ? historyData.getData() : Collections.emptyList();
//        Map<String, List<Object[]>> acturalDataMap = acturalDataList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));
//
//        // 获取单步预测属性信息
//        List<BasicDataDTO> singlePredictionDataList = singlePredictionData != null && singlePredictionData.getData() != null ? singlePredictionData.getData() : Collections.emptyList();
//        // 单步预测数据 k:dataCode v:时间轴数据
//        Map<String, List<Object[]>> singlePredictionDataMap = singlePredictionDataList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));
//
//        // 获取多步预测属性信息
//        List<BasicDataDTO> multiBasicDataDTOList = multiPredictionData != null && multiPredictionData.getData() != null ? multiPredictionData.getData() : Collections.emptyList();
//        // 多步预测数据 k:dataCode v:时间轴数据
//        Map<String, List<Object[]>> multiPredictionDataMap = multiBasicDataDTOList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));
//
//        // 25.09.19 追加逻辑  预测值的第一个点 取的是 运行值 的最后一个点位的数据值   (目的:折线图展示时需要将实际值和预测值的点位连接起来)
//        // 根据latestHistoryDataMap,向singlePredictionDataMap当中追加第一条数据
//        for (Map.Entry<String, IntervalDataDto> entry : latestHistoryDataMap.entrySet()) {
//            String dataCode = entry.getKey();
//            IntervalDataDto latestData = entry.getValue();
//            // 处理单步预测数据
//            List<Object[]> singleData = singlePredictionDataMap.get(dataCode);
//            if (ObjectUtils.isNotEmpty(singleData)) {
//                // 追加第一条数据
//                singleData.add(0, new Object[]{latestData.getTime(), latestData.getItemVal()});
//            }
//            // 处理多步预测数据
//            List<Object[]> multiData = multiPredictionDataMap.get(dataCode);
//            if (ObjectUtils.isNotEmpty(multiData)) {
//                // 追加第一条数据
//                multiData.add(0, new Object[]{latestData.getTime(), latestData.getItemVal()});
//            }
//        }
//
//
//        // 获取上下控制值 上下告警值  温度设定线
//        List<ControlIntervalConfigDTO> contorlConfigList = controlIntervalConfigService.selectListByDataCodeList(dataCodeList);
//        Map<String, ControlIntervalConfigDTO> dataCodeConfigMap = contorlConfigList.stream().filter(data -> dataCodeList.contains(data.getDataCode())).collect(Collectors.toMap(ControlIntervalConfigDTO::getDataCode, o -> o, (v1, v2) -> v1));
//
//        List<PredictionDataShowTplDTO> dataShowList = tplService.getListByCode("kilnPredictionDataShow", PredictionDataShowTplDTO.class);
//        Map<String, PredictionDataShowTplDTO> dataShowMap =
//                dataShowList.stream().collect(Collectors.toMap(PredictionDataShowTplDTO::getDataCode,
//                        o -> o, (v1, v2) -> v1));
//
//
//        List<LineChartVO> lineChartVOList = new ArrayList<>();
//        for (int i = 0; i < dataCodeList.size(); i++) {
//            String dataCode = dataCodeList.get(i);
//            LineChartVO lineChartVO = new LineChartVO();
//            String dataName = dataNameList != null && dataNameList.size() > i ? dataNameList.get(i) : null;
//            lineChartVO.setName(StringUtils.isNotBlank(dataName) ? dataName + "趋势" : null);
//            lineChartVO.setDataCode(dataCode);
//            LineChartDataVO lineChartDataVO = new LineChartDataVO();
//            lineChartDataVO.setXData(timeList);
//
//            SeriesDataVO seriesDataVO = new SeriesDataVO();
//            List<Object[]> actualChartData = acturalDataMap.get(dataCode);
//            // 处理需要返回的数据
//            handleRtnData(dataShowMap, dataCode, seriesDataVO, actualChartData, dataName, singlePredictionDataMap, multiPredictionDataMap, dataCodeConfigMap, lineChartDataVO);
//
//            lineChartDataVO.setSeriesData(seriesDataVO);
//
//            lineChartVO.setData(lineChartDataVO);
//            lineChartVOList.add(lineChartVO);
//        }
//        return lineChartVOList;
//    }

    /**
     * 组装结果
     *
     * @param historyData
     * @param singlePredictionData
     * @param multiPredictionData
     * @param timeList
     * @return
     */
    private List<LineChartVO> buildKilnForecastInfoVO(ForecastKilnParamsDTO projectPropVO, ColumnChartDTO historyData, ColumnChartDTO singlePredictionData, ColumnChartDTO multiPredictionData, List<String> timeList) {
        // 组装结果
        // 获取dataCode
        List<String> dataCodeList = projectPropVO.getDataCodes();
        // 获取dataCode对应的属性名称
        List<String> dataNameList = projectPropVO.getNames();

        // 获取单步预测属性信息
        List<BasicDataDTO> singlePredictionDataList = singlePredictionData.getData();
        Map<String, List<Object[]>> singlePredictionDataMap = singlePredictionDataList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 获取多步预测属性信息
        List<BasicDataDTO> multiBasicDataDTOList = multiPredictionData.getData();
        Map<String, List<Object[]>> multiPredictionDataMap = multiBasicDataDTOList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 获取上下控制值 上下告警值  温度设定线
        // k: dataCode v: 各设定值chart格式数据
        Map<String, ControlIntervalConfigHisChartDataDTO> dataCodeConfigChartMap = controlIntervalConfigService.queryHistoryConfigChart(dataCodeList, projectPropVO.getStartTime(), projectPropVO.getEndTime(), projectPropVO.getTs(), projectPropVO.getTsUnit(), projectPropVO.getFormatVal());

        List<PredictionDataShowTplDTO> dataShowList = tplService.getListByCode("kilnPredictionDataShow", PredictionDataShowTplDTO.class);
        Map<String, PredictionDataShowTplDTO> dataShowMap = dataShowList.stream().collect(Collectors.toMap(PredictionDataShowTplDTO::getDataCode, o -> o, (v1, v2) -> v1));

        List<LineChartVO> lineChartVOList = new ArrayList<>();
        for (int i = 0; i < dataCodeList.size(); i++) {
            String dataCode = dataCodeList.get(i);
            LineChartVO lineChartVO = new LineChartVO();
            String dataName = dataNameList != null && dataNameList.size() > i ? dataNameList.get(i) : null;
            lineChartVO.setName(dataName);
            lineChartVO.setDataCode(dataCode);
            LineChartDataVO lineChartDataVO = new LineChartDataVO();
            lineChartDataVO.setXData(timeList);
            List<Object[]> actualChartData = new ArrayList<>();

            ControlIntervalConfigHisChartDataDTO configHisChartDTO = dataCodeConfigChartMap.get(dataCode);

            // 处理需要返回的数据
            SeriesDataVO seriesDataVO = handleRtnData(dataShowMap, dataCode, actualChartData, singlePredictionDataMap, multiPredictionDataMap, configHisChartDTO);
            lineChartDataVO.setSeriesData(seriesDataVO);
            lineChartVO.setData(lineChartDataVO);

            // 设置当前dataCode 的上下控制值 上下告警值  温度设定线
            lineChartDataVO.setMaxUpControlVal(configHisChartDTO.getMaxUpControlVal());
            lineChartDataVO.setMinLowControlVal(configHisChartDTO.getMinLowControlVal());
            lineChartDataVO.setMaxUpAlarmVal(configHisChartDTO.getMaxUpAlarmVal());
            lineChartDataVO.setMinLowAlarmVal(configHisChartDTO.getMinLowAlarmVal());
            lineChartDataVO.setMaxTemperatureSetVal(configHisChartDTO.getMaxTemperatureSetVal());
            lineChartDataVO.setMinTemperatureSetVal(configHisChartDTO.getMinTemperatureSetVal());

            lineChartVOList.add(lineChartVO);
        }
        return lineChartVOList;
    }

    /**
     * 根据配置,处理要返回的数据
     */
    private SeriesDataVO handleRtnData(Map<String, PredictionDataShowTplDTO> dataShowMap, String dataCode, List<Object[]> actualChartData, Map<String, List<Object[]>> singlePredictionDataMap, Map<String, List<Object[]>> multiPredictionDataMap, ControlIntervalConfigHisChartDataDTO configHisChartDTO) {
        SeriesDataVO seriesDataVO = new SeriesDataVO();

        PredictionDataShowTplDTO dataShowDto = dataShowMap.get(dataCode);
        if (ObjectUtils.isNotEmpty(dataShowDto)) {
            seriesDataVO.setActual(parseAttributeInfo(dataShowDto.getShowActual() ? actualChartData : null, "运行值"));
            seriesDataVO.setSingleForecast(parseAttributeInfo(dataShowDto.getShowSingleForecast() ? singlePredictionDataMap.get(dataCode) : null, "单步预测值"));
            seriesDataVO.setMultiForecast(parseAttributeInfo(dataShowDto.getShowMultiForecast() ? multiPredictionDataMap.get(dataCode) : null, "多步预测值"));

            if (ObjectUtils.isNotEmpty(configHisChartDTO)) {
                seriesDataVO.setUpControl(dataShowDto.getShowUpControl() ? new AttributeInfoVO("上波动限", configHisChartDTO.getUpControlChart()) : null);
                seriesDataVO.setLowControl(dataShowDto.getShowLowControl() ? new AttributeInfoVO("下波动限", configHisChartDTO.getLowControlChart()) : null);
                seriesDataVO.setUpAlarm(dataShowDto.getShowUpAlarm() ? new AttributeInfoVO("上告警限", configHisChartDTO.getUpAlarmChart()) : null);
                seriesDataVO.setLowAlarm(dataShowDto.getShowLowAlarm() ? new AttributeInfoVO("下告警限", configHisChartDTO.getLowAlarmChart()) : null);
                seriesDataVO.setTemperatureSet(dataShowDto.getShowTemperatureSet() ? new AttributeInfoVO("温度设定值", configHisChartDTO.getTemperatureSetChart()) : null);
            }
        }
        return seriesDataVO;
    }

//    private void handleRtnData(Map<String, PredictionDataShowTplDTO> dataShowMap, String dataCode,
//                               SeriesDataVO seriesDataVO, List<Object[]> actualChartData, String dataName,
//                               Map<String, List<Object[]>> singlePredictionDataMap,
//                               Map<String, List<Object[]>> multiPredictionDataMap,
//                               Map<String, ControlIntervalConfigDTO> dataCodeConfigMap,
//                               LineChartDataVO lineChartDataVO) {
//        PredictionDataShowTplDTO dataShowDto = dataShowMap.get(dataCode);
//        if (ObjectUtils.isNotEmpty(dataShowDto)) {

    /// /            seriesDataVO.setActual(parseAttributeInfo(dataShowDto.getShowActual() ? actualChartData : null,
    /// /                    StringUtils.isNotBlank(dataName) ? "实际" + dataName : "实际"));
    /// /            seriesDataVO.setSingleForecast(parseAttributeInfo(dataShowDto.getShowSingleForecast() ?
    /// /                    singlePredictionDataMap.get(dataCode) : null, StringUtils.isNotBlank(dataName) ?
    /// /                    "单步预测" + dataName : "单步预测"));
    /// /            seriesDataVO.setMultiForecast(parseAttributeInfo(dataShowDto.getShowMultiForecast() ?
    /// /                    multiPredictionDataMap.get(dataCode) : null, StringUtils.isNotBlank(dataName) ?
    /// /                    "多步预测" + dataName : "多步预测"));
//            seriesDataVO.setActual(parseAttributeInfo(dataShowDto.getShowActual() ? actualChartData : null, "运行值"));
//            seriesDataVO.setSingleForecast(parseAttributeInfo(dataShowDto.getShowSingleForecast() ?
//                    singlePredictionDataMap.get(dataCode) : null, "单步预测值"));
//            seriesDataVO.setMultiForecast(parseAttributeInfo(dataShowDto.getShowMultiForecast() ?
//                    multiPredictionDataMap.get(dataCode) : null, "多步预测值"));
//            ControlIntervalConfigDTO configDTO = dataCodeConfigMap.get(dataCode);
//            if (ObjectUtils.isNotEmpty(configDTO)) {
//                lineChartDataVO.setUpControl(dataShowDto.getShowUpControl() ? configDTO.getUpControl() : null);
//                lineChartDataVO.setLowControl(dataShowDto.getShowLowControl() ? configDTO.getLowControl() : null);
//                lineChartDataVO.setUpAlarm(dataShowDto.getShowUpAlarm() ? configDTO.getUpAlarm() : null);
//                lineChartDataVO.setLowAlarm(dataShowDto.getShowLowAlarm() ? configDTO.getLowAlarm() : null);
//                lineChartDataVO.setTemperatureSet(dataShowDto.getShowTemperatureSet() ?
//                        configDTO.getTemperatureSet() : null);
//            }
//        }
//    }
    private AttributeInfoVO parseAttributeInfo(List<Object[]> data, String dataName) {
        AttributeInfoVO attributeInfoVO = new AttributeInfoVO();
        attributeInfoVO.setName(dataName);
        attributeInfoVO.setValue(data);
        return attributeInfoVO;
    }


    private KilnForecastLineChartVO buildKilnForecastLineChart(CommonChartParamsDto commonChartParamsDto, ColumnChartDTO singlePredictionData, ColumnChartDTO multiPredictionData, List<String> timeList) {
        // 获取属性信息Code
        List<String> dataCodeList = commonChartParamsDto.getDataCodes();
        // 获取参数属性
        InfoListQueryVo infoListQueryVo = new InfoListQueryVo();
        infoListQueryVo.setDataCodes(new HashSet<>(dataCodeList));
        // 获取单步预测属性信息
        List<BasicDataDTO> singlePredictionDataList = singlePredictionData.getData();
        Map<String, List<Object[]>> singlePredictionDataMap = singlePredictionDataList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 获取多步预测属性信息
        List<BasicDataDTO> multiBasicDataDTOList = multiPredictionData.getData();
        Map<String, List<Object[]>> multiPredictionDataMap = multiBasicDataDTOList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        List<KilnForecastDetailVO> data = new ArrayList<>(dataCodeList.size());
        dataCodeList.stream().forEach(dataCode -> {
            KilnForecastDetailVO basicDataDTO = new KilnForecastDetailVO();
            basicDataDTO.setTimeList(timeList);
            basicDataDTO.setDataCode(dataCode);
            basicDataDTO.setSingleStepForecastValueList(singlePredictionDataMap.get(dataCode));
            basicDataDTO.setMultiStepForecastValueList(multiPredictionDataMap.get(dataCode));
            data.add(basicDataDTO);
        });
        KilnForecastLineChartVO kilnForecastLineChartVO = new KilnForecastLineChartVO();
        kilnForecastLineChartVO.setData(data);
        kilnForecastLineChartVO.setXAxis(timeList);
        return kilnForecastLineChartVO;
    }

    /**
     * @param historyData          历史数据
     * @param singlePredictionData 单步预测数据
     * @param multiPredictionData  多步预测数据
     * @param timeList
     * @return
     * @desc: 组装结果
     */
    private KilnLineChartVO buildForecastKilnResult(ColumnChartDTO historyData, ColumnChartDTO singlePredictionData, ColumnChartDTO multiPredictionData, List<String> timeList) {
        // 获取单步预测属性信息
        List<BasicDataDTO> signleData = singlePredictionData.getData();
        Map<String, List<Object[]>> signleDataMap = signleData.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 获取多步预测属性信息
        List<BasicDataDTO> multidata = multiPredictionData.getData();
        Map<String, List<Object[]>> multidataDataMap = multidata.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        List<BasicDataDTO> historyBaseList = historyData.getData();
        List<KilnDetailVO> forecastKilnDetailVOList = new ArrayList<>(historyBaseList.size());
        historyBaseList.forEach(basicDataDTO -> {
            KilnDetailVO forecastKilnDetailVO = ConvertUtils.sourceToTarget(basicDataDTO, KilnDetailVO.class);
            forecastKilnDetailVO.setActualValueList(basicDataDTO.getData());
            forecastKilnDetailVO.setSingleStepForecastValueList(signleDataMap.get(basicDataDTO.getDataCode()));
            forecastKilnDetailVO.setMultiStepForecastValueList(multidataDataMap.get(basicDataDTO.getDataCode()));
            forecastKilnDetailVOList.add(forecastKilnDetailVO);
        });
        // 组装结果
        KilnLineChartVO forecastKilnLineChartVO = new KilnLineChartVO();
        forecastKilnLineChartVO.setData(forecastKilnDetailVOList);
        forecastKilnLineChartVO.setXAxis(timeList);
        return forecastKilnLineChartVO;
    }

    /**
     * @param parmsVo
     * @return
     * @desc: 获取历史数据
     */
    public List<IntervalDataDto> getHistoryIntervalDataVal(CommonChartParamsVo parmsVo) {
        CommonChartParamsVo vo = ConvertUtils.sourceToTarget(parmsVo, CommonChartParamsVo.class);
        log.info("查询柱状图、折线图等图表数据(量), params:{}", com.alibaba.fastjson2.JSON.toJSONString(vo));
        // 是否进行短码替换
        if (CollectionUtils.isNotEmpty(vo.getPropModelCodes())) {
            List<PropInsDTO> propInsDtoList = dataService.queryPropCodeByInsCodeAndShortCode(String.join(ConstantSymbol.COMMA, vo.getDataCodes()), String.join(ConstantSymbol.COMMA, vo.getPropModelCodes()));
            vo.setDataCodes(propInsDtoList.stream().map(PropInsDTO::getPropCode).collect(Collectors.toList()));
        }
        // 查询数据
        return dataService.queryIntervalVal(com.siact.sec.utils.ConvertUtils.sourceToTarget(vo, IntervalValParamsDto.class));
    }


    /**
     * 根据查询参数获取窑炉温度历史数据和单步/多步预测数据
     *
     * @param query 查询参数
     * @return 返回温度预测结果
     */

    @Override
    public TempForecastVO queryTemperature(TempForecastQuery query) {
        // 获取当前时间设置为查询结束时间, 点位数据需将秒进行归 0
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern(ConstantTime.DATE_TIME_MM_00));

        // 获取查询的点位编码
        List<String> dataCodes = query.getDataCodes();

        // 封装查询参数
        IntervalValParamsDto dto = ConvertUtils.sourceToTarget(query, IntervalValParamsDto.class);
        dto.setEndTime(now);

        // 查询历史数据
        List<IntervalDataDto> intervalDataDtos = dataService.queryIntervalVal(dto);
        // 构建历史数据集
        Map<String, List<Object[]>> historyData = support.buildForecastValueMap(intervalDataDtos, ConvertUtils.sourceToTarget(dto, CommonChartParamsDto.class));

        Map<String, List<Object[]>> single = Collections.emptyMap();
        Map<String, List<Object[]>> multi = Collections.emptyMap();
        // 查询预测数据, 从当前时间开始
        if (now.compareTo(query.getEndTime()) <= 0) {
            String startTime = TimeUtil.getCalcTime(now, 1, "MIN");
            Map<Integer, List<PredictedDataEntity>> predictedDatas = predictedDataRepository.queryByTypeCode(
                    dataCodes,
                    Arrays.asList(PredictedTypeEnum.getEnumByStep(query.getSingleStepDuration(), 1), PredictedTypeEnum.getEnumByStep(query.getMultiStepDuration(), 2)),
                    startTime, // 间隔一个预测点位, 使用运行值的最后一个数据进行补充, 保证曲线平滑绘制
                    query.getEndTime()
            );

            CommonChartParamsDto chartParamDto = ConvertUtils.sourceToTarget(query, CommonChartParamsDto.class);
            chartParamDto.setStartTime(startTime);

            single = support.buildForecastValueMap(ConvertUtils.sourceToTarget(predictedDatas.get(1), IntervalDataDto.class), chartParamDto);
            multi = support.buildForecastValueMap(ConvertUtils.sourceToTarget(predictedDatas.get(2), IntervalDataDto.class), chartParamDto);

            /* 对预测数据添加历史最后一条数据, 保证曲线衔接平滑 */
            // 处理数据, 获取每个 dataCode time 最新的一条数据
            Map<String, IntervalDataDto> latestHistoryData = intervalDataDtos.stream().collect(Collectors.toMap(
                    IntervalDataDto::getDataCode,
                    Function.identity(),
                    BinaryOperator.maxBy(Comparator.comparing(IntervalDataDto::getTime))
            ));

            // 添加数据
            for (Map.Entry<String, IntervalDataDto> entry : latestHistoryData.entrySet()) {
                String dataCode = entry.getKey();
                IntervalDataDto lastData = entry.getValue();
                // 单步预测数据
                List<Object[]> singlePredictData = single.get(dataCode);
                if (ObjectUtils.isNotEmpty(singlePredictData)) singlePredictData.add(0, new Object[]{lastData.getTime(), lastData.getItemVal()});
                // 多步预测数据
                List<Object[]> multiPredictData = multi.get(dataCode);
                if (ObjectUtils.isNotEmpty(multiPredictData)) multiPredictData.add(0, new Object[]{lastData.getTime(), lastData.getItemVal()});
            }
        }
        // 获取配置项
        List<PredictionDataShowTplDTO> dataShowTplDTOList = tplService.getListByCode("kilnPredictionDataShow", PredictionDataShowTplDTO.class);
        Map<String, PredictionDataShowTplDTO> dataShowTplDTOMap = dataShowTplDTOList.stream().collect(Collectors.toMap(
                PredictionDataShowTplDTO::getDataCode,
                o -> o,
                (v1, v2) -> v1)
        );
        // 获取上下控制限/告警限, 以及温度设定值
        Map<String, ControlIntervalConfigHisChartDataDTO> controlConfigMaps = controlIntervalConfigService.queryHistoryConfigChart(
                dataCodes,
                query.getStartTime(),
                query.getEndTime(),
                query.getTs(),
                query.getTsUnit(),
                query.getFormatVal()
        );

        /* 封装返回结果 */
        // 1. 获取总的时间轴
        List<String> xdata = IntervalTimeUtil.getIntervalTimeList(query.getStartTime(), query.getEndTime(), query.getTsUnit(), query.getTs(), query.getFormatVal());
        // 2. 构建历史/预测温度数据对象
        List<TempForecastInfoVO> series = new ArrayList<>();

        // 获取名称
        List<String> names = query.getNames();

        for (int i = 0; i < dataCodes.size(); i++) {
            String dataCode = dataCodes.get(i); // 编码
            String dataName = CollectionUtils.isNotEmpty(names) ? names.get(i) : null; // 名称
            ControlIntervalConfigHisChartDataDTO hisChartDataDTO = controlConfigMaps.get(dataCode); // 获取最值

            // 根据配置, 构建数据对象
            PredictionDataShowTplDTO tpl = dataShowTplDTOMap.get(dataCode);
            HashMap<String, TempForecastInfoValueVO> tempForecastInfoValueVOData = new HashMap<>();
            if (ObjectUtils.isNotEmpty(tpl)) {
                tempForecastInfoValueVOData.put("dcs", TempForecastInfoValueVO.createIfMatch(tpl.getShowActual(), "运行值", historyData.get(dataCode)));
                if (MapUtils.isNotEmpty(single)) {
                    tempForecastInfoValueVOData.put("single", TempForecastInfoValueVO.createIfMatch(tpl.getShowSingleForecast(), "单步预测值", single.get(dataCode)));
                }
                if (MapUtils.isNotEmpty(multi)) {
                    tempForecastInfoValueVOData.put("multi", TempForecastInfoValueVO.createIfMatch(tpl.getShowMultiForecast(), "多步预测值", multi.get(dataCode)));
                }
                tempForecastInfoValueVOData.put("upControl", TempForecastInfoValueVO.createIfMatch(tpl.getShowUpControl(), "上波动限", hisChartDataDTO.getUpControlChart()));
                tempForecastInfoValueVOData.put("lowControl", TempForecastInfoValueVO.createIfMatch(tpl.getShowLowControl(), "下波动限", hisChartDataDTO.getLowControlChart()));
                tempForecastInfoValueVOData.put("upAlarm", TempForecastInfoValueVO.createIfMatch(tpl.getShowUpAlarm(), "上告警限", hisChartDataDTO.getUpAlarmChart()));
                tempForecastInfoValueVOData.put("lowAlarm", TempForecastInfoValueVO.createIfMatch(tpl.getShowLowAlarm(), "下告警限", hisChartDataDTO.getLowAlarmChart()));
                tempForecastInfoValueVOData.put("temperatureSet", TempForecastInfoValueVO.createIfMatch(tpl.getShowTemperatureSet(), "温度设定值", hisChartDataDTO.getTemperatureSetChart()));
            }

            TempForecastInfoVO tempForecastInfoVO = TempForecastInfoVO.builder()
                    .dataCode(dataCode)
                    .name(StringUtils.isNotBlank(dataName) ? dataName + "趋势" : null)
                    .maxUpControlVal(NumberUtils.createBigDecimal(hisChartDataDTO.getMaxUpControlVal()))
                    .minLowControlVal(NumberUtils.createBigDecimal(hisChartDataDTO.getMinLowControlVal()))
                    .maxUpAlarmVal(NumberUtils.createBigDecimal(hisChartDataDTO.getMaxUpAlarmVal()))
                    .minLowAlarmVal(NumberUtils.createBigDecimal(hisChartDataDTO.getMinLowAlarmVal()))
                    .maxTemperatureSetVal(NumberUtils.createBigDecimal(hisChartDataDTO.getMaxTemperatureSetVal()))
                    .minTemperatureSetVal(NumberUtils.createBigDecimal(hisChartDataDTO.getMinTemperatureSetVal()))
                    .data(tempForecastInfoValueVOData)
                    .build();


            // 添加数据
            series.add(tempForecastInfoVO);
        }

        // 返回结果
        return TempForecastVO.builder().xdata(xdata).series(series).build();
    }

}

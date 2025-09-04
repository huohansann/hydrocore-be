package com.siact.module.forecast.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.siact.api.common.api.vo.common.InfoListQueryVo;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.base.dto.BasicDataDTO;
import com.siact.module.base.dto.ColumnChartDTO;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.service.ControlIntervalConfigService;
import com.siact.module.base.service.TplService;
import com.siact.module.base.vo.TplVO;
import com.siact.module.forecast.dto.ForecastKilnParamsDTO;
import com.siact.module.forecast.dto.PredictionDataShowTplDTO;
import com.siact.module.forecast.service.ForecastKilnService;
import com.siact.module.forecast.vo.*;
import com.siact.module.predicted.dto.PredictedDataDTO;
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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-26 10:33
 */
@Slf4j
@Service
public class ForecastKilnServiceImpl implements ForecastKilnService {

    @Autowired
    private DataService dataService;

    @Autowired
    private TplService tplService;

    @Autowired
    private PredictedDataService predictedDataService;

    @Autowired
    private ControlIntervalConfigService controlIntervalConfigService;


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
     *
     * @param projectPropVO
     * @return
     */
    @Override
    public List<LineChartVO> queryForecastInfo(ForecastKilnParamsDTO projectPropVO) {
        // 解析参数
        // 获取当前时间：yyyy-MM-dd HH:mm:ss
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 查询历史数据
        CommonChartParamsVo queryHisParamVo = ConvertUtils.sourceToTarget(projectPropVO, CommonChartParamsVo.class);
        // 历史数据截止到当前时刻
        queryHisParamVo.setEndTime(now);
        List<IntervalDataDto> intervalDataDtos = getHistoryIntervalDataVal(queryHisParamVo);

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
            queryForecastParams.setStartTime(now);
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
        return buildLineChartVO(projectPropVO, historyData, singlePredictionData, multiPredictionData, timeList);
    }

    @Override
    public List<LineChartVO> queryKilnForecastInfo(ForecastKilnParamsDTO projectPropVO) {
        // 解析参数
        CommonChartParamsVo params = ConvertUtils.sourceToTarget(projectPropVO, CommonChartParamsVo.class);
        // 查询预测数据
        // 获取预测数据
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
     *
     * @param historyData
     * @param singlePredictionData
     * @param multiPredictionData
     * @param timeList
     * @return
     */
    private List<LineChartVO> buildLineChartVO(ForecastKilnParamsDTO projectPropVO, ColumnChartDTO historyData, ColumnChartDTO singlePredictionData, ColumnChartDTO multiPredictionData, List<String> timeList) {
        // 组装结果
        // 获取dataCode
        List<String> dataCodeList = projectPropVO.getDataCodes();
        // 获取dataCode对应的属性名称
        List<String> dataNameList = projectPropVO.getNames();

        // 获取参数属性
        // 获取单步预测属性信息
        List<BasicDataDTO> acturalDataList = historyData != null && historyData.getData() != null ? historyData.getData() : Collections.emptyList();
        Map<String, List<Object[]>> acturalDataMap = acturalDataList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 获取单步预测属性信息
        List<BasicDataDTO> singlePredictionDataList = singlePredictionData != null && singlePredictionData.getData() != null ? singlePredictionData.getData() : Collections.emptyList();
        Map<String, List<Object[]>> singlePredictionDataMap = singlePredictionDataList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 获取多步预测属性信息
        List<BasicDataDTO> multiBasicDataDTOList = multiPredictionData != null && multiPredictionData.getData() != null ? multiPredictionData.getData() : Collections.emptyList();
        Map<String, List<Object[]>> multiPredictionDataMap = multiBasicDataDTOList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));


        // 获取上下控制值 上下告警值  温度设定线
        List<ControlIntervalConfigDTO> contorlConfigList = controlIntervalConfigService.selectListByDataCodeList(dataCodeList);
        Map<String, ControlIntervalConfigDTO> dataCodeConfigMap = contorlConfigList.stream().filter(data -> dataCodeList.contains(data.getDataCode())).collect(Collectors.toMap(ControlIntervalConfigDTO::getDataCode, o -> o, (v1, v2) -> v1));

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

            SeriesDataVO seriesDataVO = new SeriesDataVO();
            List<Object[]> actualChartData = acturalDataMap.get(dataCode);
            // 处理需要返回的数据
            handleRtnData(dataShowMap, dataCode, seriesDataVO, actualChartData, dataName, singlePredictionDataMap, multiPredictionDataMap, dataCodeConfigMap, lineChartDataVO);

            lineChartDataVO.setSeriesData(seriesDataVO);

            lineChartVO.setData(lineChartDataVO);
            lineChartVOList.add(lineChartVO);
        }
        return lineChartVOList;
    }

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

        // 获取参数属性
        // 获取单步预测属性信息
        List<BasicDataDTO> acturalDataList = historyData != null && historyData.getData() != null ? historyData.getData() : Collections.emptyList();
        Map<String, List<Object[]>> acturalDataMap = acturalDataList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 获取单步预测属性信息
        List<BasicDataDTO> singlePredictionDataList = singlePredictionData.getData();
        Map<String, List<Object[]>> singlePredictionDataMap = singlePredictionDataList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 获取多步预测属性信息
        List<BasicDataDTO> multiBasicDataDTOList = multiPredictionData.getData();
        Map<String, List<Object[]>> multiPredictionDataMap = multiBasicDataDTOList.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 获取上下控制值 上下告警值  温度设定线
        List<ControlIntervalConfigDTO> contorlConfigList = controlIntervalConfigService.selectListByDataCodeList(dataCodeList);
        Map<String, ControlIntervalConfigDTO> dataCodeConfigMap = contorlConfigList.stream().collect(Collectors.toMap(ControlIntervalConfigDTO::getDataCode, o -> o, (v1, v2) -> v1));
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
            SeriesDataVO seriesDataVO = new SeriesDataVO();
            List<Object[]> actualChartData = new  ArrayList<>();
            // 处理需要返回的数据
            handleRtnData(dataShowMap, dataCode, seriesDataVO, actualChartData, dataName, singlePredictionDataMap, multiPredictionDataMap, dataCodeConfigMap, lineChartDataVO);
            lineChartDataVO.setSeriesData(seriesDataVO);

            lineChartVO.setData(lineChartDataVO);
            lineChartVOList.add(lineChartVO);
        }
        return lineChartVOList;
    }

    /**
     * 根据配置,处理要返回的数据
     * @param dataShowMap
     * @param dataCode
     * @param seriesDataVO
     * @param actualChartData
     * @param dataName
     * @param singlePredictionDataMap
     * @param multiPredictionDataMap
     * @param dataCodeConfigMap
     * @param lineChartDataVO
     */
    private void handleRtnData(Map<String, PredictionDataShowTplDTO> dataShowMap, String dataCode,
                               SeriesDataVO seriesDataVO, List<Object[]> actualChartData, String dataName,
                               Map<String, List<Object[]>> singlePredictionDataMap,
                               Map<String, List<Object[]>> multiPredictionDataMap,
                               Map<String, ControlIntervalConfigDTO> dataCodeConfigMap,
                               LineChartDataVO lineChartDataVO) {
        PredictionDataShowTplDTO dataShowDto = dataShowMap.get(dataCode);
        if (ObjectUtils.isNotEmpty(dataShowDto)) {
//            seriesDataVO.setActual(parseAttributeInfo(dataShowDto.getShowActual() ? actualChartData : null,
//                    StringUtils.isNotBlank(dataName) ? "实际" + dataName : "实际"));
//            seriesDataVO.setSingleForecast(parseAttributeInfo(dataShowDto.getShowSingleForecast() ?
//                    singlePredictionDataMap.get(dataCode) : null, StringUtils.isNotBlank(dataName) ?
//                    "单步预测" + dataName : "单步预测"));
//            seriesDataVO.setMultiForecast(parseAttributeInfo(dataShowDto.getShowMultiForecast() ?
//                    multiPredictionDataMap.get(dataCode) : null, StringUtils.isNotBlank(dataName) ?
//                    "多步预测" + dataName : "多步预测"));
            seriesDataVO.setActual(parseAttributeInfo(dataShowDto.getShowActual() ? actualChartData : null, "运行值"));
            seriesDataVO.setSingleForecast(parseAttributeInfo(dataShowDto.getShowSingleForecast() ?
                    singlePredictionDataMap.get(dataCode) : null, "单步预测值"));
            seriesDataVO.setMultiForecast(parseAttributeInfo(dataShowDto.getShowMultiForecast() ?
                    multiPredictionDataMap.get(dataCode) : null, "多步预测值"));
            ControlIntervalConfigDTO configDTO = dataCodeConfigMap.get(dataCode);
            if (ObjectUtils.isNotEmpty(configDTO)) {
                lineChartDataVO.setUpControl(dataShowDto.getShowUpControl() ? configDTO.getUpControl() : null);
                lineChartDataVO.setLowControl(dataShowDto.getShowLowControl() ? configDTO.getLowControl() : null);
                lineChartDataVO.setUpAlarm(dataShowDto.getShowUpAlarm() ? configDTO.getUpAlarm() : null);
                lineChartDataVO.setLowAlarm(dataShowDto.getShowLowAlarm() ? configDTO.getLowAlarm() : null);
                lineChartDataVO.setTemperatureSet(dataShowDto.getShowTemperatureSet() ?
                        configDTO.getTemperatureSet() : null);
            }
        }
    }


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

}
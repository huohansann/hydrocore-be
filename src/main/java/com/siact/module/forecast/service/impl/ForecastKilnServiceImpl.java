package com.siact.module.forecast.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.base.dto.BasicDataDTO;
import com.siact.module.base.dto.ColumnChartDTO;
import com.siact.module.base.service.TplService;
import com.siact.module.base.vo.TplVO;
import com.siact.module.forecast.dto.AttributeParametersDTO;
import com.siact.module.forecast.dto.ForecastKilnParamsDTO;
import com.siact.module.forecast.dto.PredictionTplDTO;
import com.siact.module.forecast.service.ForecastKilnService;
import com.siact.module.forecast.vo.ForecastKilnDetailVO;
import com.siact.module.forecast.vo.ForecastKilnLineChartVO;
import com.siact.module.forecast.vo.ForecastKilnMenuVO;
import com.siact.module.predicted.dto.PredictedDataDTO;
import com.siact.module.predicted.service.PredictedDataService;
import com.siact.sec.dto.CommonChartParamsDto;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.sec.dto.PropInsDTO;
import com.siact.sec.sevice.DataService;
import com.siact.sec.utils.CommonHandle;
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

    @Override
    public ForecastKilnLineChartVO queryForecastInfo(ForecastKilnParamsDTO projectPropVO) {
        // 解析参数
        CommonChartParamsVo params = ConvertUtils.sourceToTarget(projectPropVO, CommonChartParamsVo.class);
        // 获取当前时间：yyyy-MM-dd HH:mm:ss
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 查询历史数据
        CommonChartParamsVo params01 = ConvertUtils.sourceToTarget(params, CommonChartParamsVo.class);
        // 历史数据截止到当前时刻
        params01.setEndTime(now);
        List<IntervalDataDto> intervalDataDtos = getHistoryIntervalDataVal(params01);
        // 查询预测数据
        // 预测数据从当前时刻算起
        params.setStartTime(now);
        // 获取预测数据
        Map<Integer, List<PredictedDataDTO>> predictedData = predictedDataService.getPredictedData(Arrays.asList(1, 2), params.getStartTime(), params.getEndTime());
        List<IntervalDataDto> singlePredictionDataList = ConvertUtils.sourceToTarget(predictedData.get(1), IntervalDataDto.class);
        List<IntervalDataDto> multiPredictionDataList = ConvertUtils.sourceToTarget(predictedData.get(2), IntervalDataDto.class);

        // 封装数据
        CommonChartParamsDto acturalParamsDto = ConvertUtils.sourceToTarget(params01, CommonChartParamsDto.class);
        // 组装历史数据
        ColumnChartDTO historyData = CommonHandle.buildColumnChartDTO(acturalParamsDto, intervalDataDtos);

        // 组装预测数据
        CommonChartParamsDto commonChartParamsDto = ConvertUtils.sourceToTarget(params, CommonChartParamsDto.class);
        // 组装单步预测数据
        ColumnChartDTO singlePredictionData = CommonHandle.buildColumnChartDTO(commonChartParamsDto, singlePredictionDataList);
        // 组装多步预测数据
        ColumnChartDTO multiPredictionData = CommonHandle.buildColumnChartDTO(commonChartParamsDto, multiPredictionDataList);
        // 组装结果
        return buildForecastKilnResult(historyData, singlePredictionData, multiPredictionData);
    }

    /**
     * @param historyData          历史数据
     * @param singlePredictionData 单步预测数据
     * @param multiPredictionData  多步预测数据
     * @return
     * @desc: 组装结果
     */
    private ForecastKilnLineChartVO buildForecastKilnResult(ColumnChartDTO historyData, ColumnChartDTO singlePredictionData, ColumnChartDTO multiPredictionData) {
        // 获取单步预测属性信息
        List<BasicDataDTO> signleData = singlePredictionData.getData();
        Map<String, List<Object[]>> signleDataMap = signleData.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        // 获取多步预测属性信息
        List<BasicDataDTO> multidata = multiPredictionData.getData();
        Map<String, List<Object[]>> multidataDataMap = multidata.stream().collect(Collectors.toMap(BasicDataDTO::getDataCode, BasicDataDTO::getData, (v1, v2) -> v1));

        List<BasicDataDTO> historyBaseList = historyData.getData();
        List<ForecastKilnDetailVO> forecastKilnDetailVOList = new ArrayList<>(historyBaseList.size());
        historyBaseList.forEach(basicDataDTO -> {
            ForecastKilnDetailVO forecastKilnDetailVO = ConvertUtils.sourceToTarget(basicDataDTO, ForecastKilnDetailVO.class);
            forecastKilnDetailVO.setActualValueList(basicDataDTO.getData());
            forecastKilnDetailVO.setSingleStepForecastValueList(signleDataMap.get(basicDataDTO.getDataCode()));
            forecastKilnDetailVO.setMultiStepForecastValueList(multidataDataMap.get(basicDataDTO.getDataCode()));
            forecastKilnDetailVOList.add(forecastKilnDetailVO);
        });
        // 组装结果
        ForecastKilnLineChartVO forecastKilnLineChartVO = new ForecastKilnLineChartVO();
        forecastKilnLineChartVO.setData(forecastKilnDetailVOList);
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
     * @param vo
     * @return
     * @desc: 获取预测数据
     */
    public Map<Integer, List<IntervalDataDto>> getForecastIntervalDataVal(CommonChartParamsVo vo) {
        log.info("查询柱状图、折线图等图表数据(量), params:{}", com.alibaba.fastjson2.JSON.toJSONString(vo));
        // TODO 查询数据:此处需要替换成预测数据
        Map<Integer, List<PredictedDataDTO>> predictedData = predictedDataService.getPredictedData(Arrays.asList(1, 2), vo.getStartTime(), vo.getEndTime());
        List<IntervalDataDto> historyIntervalDataVal1 = ConvertUtils.sourceToTarget(predictedData.get(1), IntervalDataDto.class);
        List<IntervalDataDto> historyIntervalDataVal2 = ConvertUtils.sourceToTarget(predictedData.get(2), IntervalDataDto.class);
        Map<Integer, List<IntervalDataDto>> resultMap = new HashMap<>(2);
        resultMap.put(1, historyIntervalDataVal1);
        resultMap.put(2, historyIntervalDataVal2);
        return resultMap;
    }
}
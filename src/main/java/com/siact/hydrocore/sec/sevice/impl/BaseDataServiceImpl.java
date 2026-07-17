package com.siact.hydrocore.sec.sevice.impl;

import com.alibaba.fastjson.JSONObject;
import com.siact.hydrocore.common.exception.CustomException;
import com.siact.hydrocore.common.utils.ConvertUtils;
import com.siact.hydrocore.common.utils.ParamConversionUtil;
import com.siact.hydrocore.module.base.dto.ColumnChartDTO;
import com.siact.hydrocore.sec.dto.AttributeBetweenValVO;
import com.siact.hydrocore.sec.dto.AttributeIntervalValParamsDto;
import com.siact.hydrocore.sec.dto.CommonChartParamsDto;
import com.siact.hydrocore.sec.dto.CumulativeDataDTO;
import com.siact.hydrocore.sec.dto.IntervalDataDto;
import com.siact.hydrocore.sec.dto.IntervalValParamsDto;
import com.siact.hydrocore.sec.sevice.BaseDataService;
import com.siact.hydrocore.sec.sevice.DataService;
import com.siact.hydrocore.sec.utils.CommonHandle;
import com.siact.hydrocore.sec.vo.CloumChartParmsVO;
import com.siact.hydrocore.sec.vo.CumulativeDataVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-04-15 16:46
 */
@Slf4j
@Service
public class BaseDataServiceImpl implements BaseDataService {

    @Autowired
    DataService dataService;

    /**
     * @return
     * @desc: 获取属性的柱状图信息
     */
    @Override
    public ColumnChartDTO getColumnChartInfo(CloumChartParmsVO projectPropVO) {
        IntervalValParamsDto params = ParamConversionUtil.parseAttributeParams(projectPropVO);
        // 查询数据
        List<IntervalDataDto> intervalDataDtos = dataService.queryIntervalVal(params);
        // 封装数据
        CommonChartParamsDto commonChartParamsDto = ConvertUtils.sourceToTarget(params, CommonChartParamsDto.class);
        return CommonHandle.getColumnChartDTO(commonChartParamsDto, intervalDataDtos);
    }

    /**
     * @param vo
     * @return
     * @desc: 查询属性区间值
     */
    @Override
    public JSONObject queryBetweenVal(AttributeBetweenValVO vo) {
        IntervalValParamsDto params = ParamConversionUtil.parseAttributeParams(vo);
        return dataService.queryBetweenVal(params);
    }


    /**
     * @param dataCode
     * @return
     * @desc: 查询实时数据
     */
    @Override
    public JSONObject queryRealTimeInfo(List<String> dataCode) {
        if (CollectionUtils.isEmpty(dataCode)) {
            throw new CustomException("属性code不能为空");
        }
        return dataService.queryRealValue(dataCode.stream().collect(Collectors.joining(",")));
    }

    /**
     * @param dto
     * @return
     * @desc: 获取属性的区间值
     */
    @Override
    public List<IntervalDataDto> queryAttributeIntervalVal(AttributeIntervalValParamsDto dto) {
        IntervalValParamsDto params = ParamConversionUtil.parseAttributeParams(dto);
        // params.setDataCodes(Arrays.asList(dto.getDataCode().split(ConstantSymbol.COMMA)));
        // 查询数据
        List<IntervalDataDto> intervalDataDtos = dataService.queryIntervalVal(params);
        return intervalDataDtos;
    }

    /**
     * 系统累计数据(同步-环比
     *
     * @param vo
     * @return
     */
    @Override
    public List<CumulativeDataDTO> queryCumulativeData(CumulativeDataVO vo) {
        IntervalValParamsDto params = ParamConversionUtil.parseAttributeParams(vo);
        vo.setStartTime(params.getStartTime());
        vo.setEndTime(params.getEndTime());
        return dataService.queryCumulativeData(vo);
    }

    /**
     * @param vo
     * @return
     * @desc: 解析属性参数
     */
    @Override
    public IntervalValParamsDto parseAttributeParams(IntervalValParamsDto vo) {
        // 参数装换
        IntervalValParamsDto paramsDto = ParamConversionUtil.parseAttributeParams(vo);
        return paramsDto;
    }
}
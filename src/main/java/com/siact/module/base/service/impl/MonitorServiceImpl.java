package com.siact.module.base.service.impl;

import com.siact.common.constant.ConstantBase;
import com.siact.common.constant.ConstantDataApi;
import com.siact.common.constant.ConstantNum;
import com.siact.common.utils.TimeUtil;
import com.siact.module.base.dto.Load;
import com.siact.module.base.service.MonitorService;
import com.siact.sec.dto.IntervalDataDto;
import com.siact.sec.dto.IntervalValParamsDto;
import com.siact.sec.sevice.DataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruce_Hmz
 * @date 2025/4/27
 */
@Service
public class MonitorServiceImpl implements MonitorService {

    @Resource
    private DataService dataService;

    @Override
    public Load queryLoadRate(String dataCode) {
        IntervalValParamsDto paramsDto = new IntervalValParamsDto();
        // 默认取当天的
        String LocalDate = TimeUtil.dateToStrDate(new Date());
        paramsDto.setStartTime(LocalDate.concat(ConstantBase.STIME_SUFFIX));
        paramsDto.setEndTime(LocalDate.concat(ConstantBase.ETIME_SUFFIX));
        paramsDto.setDataCodes(Collections.singletonList(dataCode));
        paramsDto.setCalcType(ConstantDataApi.CALC_LAST);
        paramsDto.setTs(ConstantNum.NUMBER_FIFTEEN);
        paramsDto.setTsUnit(ConstantDataApi.TS_MIN);
        List<IntervalDataDto> intervalDataList = dataService.queryIntervalVal(paramsDto);
        List<Double> loadValues = intervalDataList.stream().map(item -> {
            double value = item.getItemVal() != null ? item.getItemVal().doubleValue() : 0d;
            return  value * 100; // 转成百分比
        }).collect(Collectors.toList());
        Load load = new Load();
        calculateLoadFactors(load, loadValues);
        return load;
    }


    private void calculateLoadFactors(Load load, List<Double> loadRateList) {
        int heavyLoadNum = 0, lightLoadNum = 0, overLoadNum = 0;
        for (Double loadRateValue : loadRateList) {
            if (loadRateValue >= ConstantNum.NUMBER_TEN && loadRateValue <= ConstantNum.NUMBER_THIRTY) {
                lightLoadNum++;
            } else if (loadRateValue >= ConstantNum.NUMBER_EIGHTY_FIVE && loadRateValue <= ConstantNum.NUMBER_HUNDRED) {
                heavyLoadNum++;
            } else if (loadRateValue > ConstantNum.NUMBER_HUNDRED) {
                overLoadNum++;
            }
        }
        double overLoad = overLoadNum != 0 ? overLoadNum * ConstantNum.NUMBER_0_25 : 0;
        double heavyLoad = heavyLoadNum != 0 ? heavyLoadNum * ConstantNum.NUMBER_0_25 : 0;
        double lightLoad = lightLoadNum != 0 ? lightLoadNum * ConstantNum.NUMBER_0_25 : 0;
        load.setHeavyLoad(heavyLoad);
        load.setLightLoad(lightLoad);
        load.setOverLoad(overLoad);
        load.setHeavyLoadNum(heavyLoadNum);
        load.setLightLoadNum(lightLoadNum);
        load.setOverLoadNum(overLoadNum);
    }
}

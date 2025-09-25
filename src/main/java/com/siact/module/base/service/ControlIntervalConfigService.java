package com.siact.module.base.service;

import com.siact.module.base.dto.ControlIntervalConfigChartDTO;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.dto.ControlIntervalConfigHisChartDataDTO;
import com.siact.module.base.vo.ControlIntervalConfigVO;

import java.util.List;
import java.util.Map;

public interface ControlIntervalConfigService {
    List<ControlIntervalConfigDTO> selectListByCondition(ControlIntervalConfigVO configVO);

    List<ControlIntervalConfigDTO> selectListByDataCodeList(List<String> dataCodeList);

//    void add(ControlIntervalConfigDTO configDTO);

    void updateConfig(List<ControlIntervalConfigDTO> configDTOs);

    ControlIntervalConfigDTO get(ControlIntervalConfigVO configVO);

//    JSONObject selectListByConditionNew(ControlIntervalConfigVO configVO);

//    void updateAndSaveHis(ControlIntervalConfigDTO configDTO);

    /**
     * 查询区间限值呈现图表
     * @param configVO
     * @return
     */
    ControlIntervalConfigChartDTO chart(ControlIntervalConfigVO configVO);

    /**
     * 查询历史数据
     *
     * @param dataCodeList
     * @return k:dataCode,v:ControlIntervalConfigHisChartDataDTO(图表类型数据)
     */
    Map<String, ControlIntervalConfigHisChartDataDTO> queryHistoryConfigChart(List<String> dataCodeList,
                                                                              String startTime, String endTime,
                                                                              Integer ts, String tsUnit, String formatVal);


}

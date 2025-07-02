package com.siact.module.base.service;

import com.alibaba.fastjson2.JSONObject;
import com.siact.module.base.dto.ControlIntervalConfigChartDTO;
import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.vo.ControlIntervalConfigVO;

import java.util.List;

public interface ControlIntervalConfigService {
    List<ControlIntervalConfigDTO> selectListByCondition(ControlIntervalConfigVO configVO);

    List<ControlIntervalConfigDTO> selectListByDataCodeList(List<String> dataCodeList);

    void add(ControlIntervalConfigDTO configDTO);

    void updateConfig(ControlIntervalConfigDTO configDTO);

    ControlIntervalConfigDTO get(ControlIntervalConfigVO configVO);

    JSONObject selectListByConditionNew(ControlIntervalConfigVO configVO);

    void updateAndSaveHis(ControlIntervalConfigDTO configDTO);

    /**
     * 查询区间限值呈现图表
     * @param configVO
     * @return
     */
    ControlIntervalConfigChartDTO chart(ControlIntervalConfigVO configVO);
}

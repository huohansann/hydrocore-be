package com.siact.module.base.service;

import com.siact.module.base.dto.ControlIntervalConfigDTO;
import com.siact.module.base.vo.ControlIntervalConfigVO;

import java.util.List;

public interface ControlIntervalConfigService {
    List<ControlIntervalConfigDTO> selectListByCondition(ControlIntervalConfigVO configVO);

    List<ControlIntervalConfigDTO> selectListByDataCodeList(List<String> dataCodeList);

    void add(ControlIntervalConfigDTO configDTO);

    void updateConfig(ControlIntervalConfigDTO configDTO);

    ControlIntervalConfigDTO get(ControlIntervalConfigVO configVO);

}

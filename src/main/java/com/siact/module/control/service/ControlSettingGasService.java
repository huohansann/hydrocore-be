package com.siact.module.control.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.dto.GasForecastQueryDTO;
import com.siact.module.control.entity.ControlSettingGasEntity;
import com.siact.module.control.vo.GasForecastVO;

import java.util.List;
import java.util.Map;

public interface ControlSettingGasService extends IService<ControlSettingGasEntity> {

    /**
     * 查询天然气设定值
     */
    List<ControlSettingGasDTO> querySetting();

    /**
     * 下发天然气设置
     */
    Boolean publish(List<ControlSettingGasDTO> list);

    GasForecastVO forecast(GasForecastQueryDTO query);

    /**
     * 查询天然气预测点位配置项
     */
    List<Map<String, String>> queryForecastConfig();
}

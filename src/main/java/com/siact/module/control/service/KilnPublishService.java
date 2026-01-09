package com.siact.module.control.service;

import com.siact.common.R;
import com.siact.module.control.dto.ControlSettingWindDTO;

import java.util.List;

public interface KilnPublishService {

    /**
     * 获取助燃风控制设定值
     */
    List<ControlSettingWindDTO> getKilnWindControlSetting();

    /**
     * 下发助燃风控制设定值(手动下发)
     *
     */
    Boolean publishWind(List<ControlSettingWindDTO> list);

    /**
     * 自动下发
     *
     */
    R gasAutoPublish();
}

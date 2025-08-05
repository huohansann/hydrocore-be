package com.siact.module.control.service;

import com.siact.common.R;
import com.siact.module.base.dto.KilnInfoDistributeDTO;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.dto.ControlSettingWindDTO;

import java.util.List;

public interface KilnPublishService {

    /**
     * 手动下发
     *
     * @param list
     * @return
     */
    R publish(List<KilnInfoDistributeDTO> list);

    /**
     * 自动下发
     *
     * @return
     */
    R autoPublish();

    /**
     * 获取天然气控制设定值
     *
     * @return
     */
    List<ControlSettingGasDTO> getKilnGasControlSetting();

    /**
     * 获取助燃风控制设定值
     *
     * @return
     */
    List<ControlSettingWindDTO> getKilnWindControlSetting();

    /**
     * 下发天然气控制设定值(手动下发)
     * @param list
     * @return
     */
    Boolean publishGas(List<ControlSettingGasDTO> list);

    /**
     * 下发助燃风控制设定值(手动下发)
     * @param list
     * @return
     */
    Boolean publishWind(List<ControlSettingWindDTO> list);
}

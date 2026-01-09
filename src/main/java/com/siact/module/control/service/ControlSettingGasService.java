package com.siact.module.control.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.entity.ControlSettingGasEntity;

import java.util.List;

public interface ControlSettingGasService extends IService<ControlSettingGasEntity> {

    /**
     * 查询天然气设定值
     */
    List<ControlSettingGasDTO> querySetting();

    /**
     * 下发天然气设置
     */
    Boolean publish(List<ControlSettingGasDTO> list);
}

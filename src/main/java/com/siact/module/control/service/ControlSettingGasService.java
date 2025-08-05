package com.siact.module.control.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.entity.ControlSettingGasEntity;

import java.util.List;

public interface ControlSettingGasService extends IService<ControlSettingGasEntity> {

    /**
     * 获取有效的 天然气控制设定值
     * @return
     */
    List<ControlSettingGasEntity> getValidList();

    /**
     * 根据dataCode删除数据(逻辑删除)
     * @param publishGasDataCodeList
     */
    void deleteByDataCode(List<String> publishGasDataCodeList);

    /**
     * 保存天然气控制设定值
     * @param publishGasSettingList
     */
    void saveGasSetting(List<ControlSettingGasDTO> publishGasSettingList);
}

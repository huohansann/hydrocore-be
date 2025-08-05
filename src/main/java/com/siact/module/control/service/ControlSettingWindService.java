package com.siact.module.control.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.control.dto.ControlSettingWindDTO;
import com.siact.module.control.entity.ControlSettingWindEntity;

import java.util.List;

public interface ControlSettingWindService extends IService<ControlSettingWindEntity> {

    /**
     * 获取有效的助燃风设定值数据
     * @return
     */
    List<ControlSettingWindEntity> getValidList();

    /**
     * 删除指定dataCode的助燃风设定值数据
     * @param publishWindDataCodeList
     */
    void deleteByDataCode(List<String> publishWindDataCodeList);

    /**
     * 保存助燃风设定值数据
     * @param publishWindSettingList
     */
    void saveWindSetting(List<ControlSettingWindDTO> publishWindSettingList);
}

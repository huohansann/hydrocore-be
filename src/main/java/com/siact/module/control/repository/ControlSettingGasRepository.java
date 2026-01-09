package com.siact.module.control.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.entity.ControlSettingGasEntity;

import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-04 14:06
 * @className : ControlSettingGasRepository
 * @description : 天然气控制数据持久层
 */
public interface ControlSettingGasRepository extends BaseRepository<ControlSettingGasEntity> {
    /**
     * 获取有效的天然气设置数据
     */
    List<ControlSettingGasEntity> queryValid();

    /**
     * 根据 dataCode 删除天然气设置数据(逻辑删除)
     */
    Boolean deleteByDataCode(List<String> dataCodes);

    /**
     * 批量保存天然气设置
     */
    Boolean save(List<ControlSettingGasDTO> publishList);
}

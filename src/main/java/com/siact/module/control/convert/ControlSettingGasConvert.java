package com.siact.module.control.convert;

import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.entity.ControlSettingGasEntity;
import org.mapstruct.Mapper;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-07 9:02
 * @className : ControlSettingGasConvert
 * @description : 天然气控制设置对象转换器
 */
@Mapper(componentModel = "spring")
public interface ControlSettingGasConvert {
    ControlSettingGasDTO toDTO(ControlSettingGasEntity entity);

    ControlSettingGasEntity toEntity(ControlSettingGasDTO dto);
}

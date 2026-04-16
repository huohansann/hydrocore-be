package com.siact.module.device.convert;

import com.siact.module.device.command.DeviceMappingCommand;
import com.siact.module.device.dto.DeviceMappingQueryDTO;
import com.siact.module.device.entity.DeviceMappingEntity;
import com.siact.module.device.query.DeviceMappingQuery;
import com.siact.module.device.vo.DeviceMappingVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeviceMappingConvert {

    DeviceMappingQueryDTO toQueryDTO(DeviceMappingQuery query);

    DeviceMappingVO toVO(DeviceMappingEntity entity);

    List<DeviceMappingVO> toVOList(List<DeviceMappingEntity> entities);

    DeviceMappingEntity toEntity(DeviceMappingCommand command);

    DeviceMappingEntity toUpdateEntity(DeviceMappingCommand command);
}

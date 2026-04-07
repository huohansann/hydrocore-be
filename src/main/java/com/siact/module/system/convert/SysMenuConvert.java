package com.siact.module.system.convert;

import com.siact.module.system.command.SysMenuCreateCommand;
import com.siact.module.system.command.SysMenuUpdateCommand;
import com.siact.module.system.dto.SysMenuQueryDTO;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.query.SysMenuQuery;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysMenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SysMenuConvert {

    SysMenuQueryDTO toQueryDTO(SysMenuQuery query);

    SysMenuVO toVO(SysMenuEntity entity);

    List<SysMenuVO> toVOList(List<SysMenuEntity> entities);

    SysMenuTreeVO toTreeVO(SysMenuEntity entity);

    SysMenuEntity toEntity(SysMenuCreateCommand command);

    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysMenuEntity toEntity(SysMenuUpdateCommand command);
}

package com.siact.hydrocore.module.system.convert;

import com.siact.hydrocore.module.system.command.SysMenuCreateCommand;
import com.siact.hydrocore.module.system.command.SysMenuUpdateCommand;
import com.siact.hydrocore.module.system.dto.SysMenuQueryDTO;
import com.siact.hydrocore.module.system.entity.SysMenuEntity;
import com.siact.hydrocore.module.system.query.SysMenuQuery;
import com.siact.hydrocore.module.system.vo.SysMenuTreeVO;
import com.siact.hydrocore.module.system.vo.SysMenuVO;
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

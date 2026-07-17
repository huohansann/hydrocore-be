package com.siact.hydrocore.module.system.convert;

import com.siact.hydrocore.module.system.command.SysRoleCreateCommand;
import com.siact.hydrocore.module.system.command.SysRoleUpdateCommand;
import com.siact.hydrocore.module.system.dto.SysRoleQueryDTO;
import com.siact.hydrocore.module.system.entity.SysRoleEntity;
import com.siact.hydrocore.module.system.query.SysRoleQuery;
import com.siact.hydrocore.module.system.vo.SysRoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SysRoleConvert {

    SysRoleQueryDTO toQueryDTO(SysRoleQuery query);

    SysRoleVO toVO(SysRoleEntity entity);

    List<SysRoleVO> toVOList(List<SysRoleEntity> entities);

    SysRoleEntity toEntity(SysRoleCreateCommand command);

    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysRoleEntity toEntity(SysRoleUpdateCommand command);
}

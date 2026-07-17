package com.siact.hydrocore.module.system.convert;

import com.siact.hydrocore.module.system.command.SysUserCreateCommand;
import com.siact.hydrocore.module.system.command.SysUserUpdateCommand;
import com.siact.hydrocore.module.system.dto.SysUserQueryDTO;
import com.siact.hydrocore.module.system.entity.SysUserEntity;
import com.siact.hydrocore.module.system.query.SysUserQuery;
import com.siact.hydrocore.module.system.vo.SysUserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SysUserConvert {

    SysUserQueryDTO toQueryDTO(SysUserQuery query);

    SysUserVO toVO(SysUserEntity entity);

    List<SysUserVO> toVOList(List<SysUserEntity> entities);

    SysUserEntity toEntity(SysUserCreateCommand command);

    @Mapping(target = "account", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysUserEntity toEntity(SysUserUpdateCommand command);
}

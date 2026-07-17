package com.siact.hydrocore.module.system.convert;

import com.siact.hydrocore.module.system.command.SysOrganizationCreateCommand;
import com.siact.hydrocore.module.system.command.SysOrganizationUpdateCommand;
import com.siact.hydrocore.module.system.dto.SysOrganizationQueryDTO;
import com.siact.hydrocore.module.system.entity.SysOrganizationEntity;
import com.siact.hydrocore.module.system.query.SysOrganizationQuery;
import com.siact.hydrocore.module.system.vo.SysOrganizationTreeVO;
import com.siact.hydrocore.module.system.vo.SysOrganizationVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SysOrganizationConvert {

    SysOrganizationQueryDTO toQueryDTO(SysOrganizationQuery query);

    SysOrganizationVO toVO(SysOrganizationEntity entity);

    List<SysOrganizationVO> toVOList(List<SysOrganizationEntity> entities);

    SysOrganizationTreeVO toTreeVO(SysOrganizationEntity entity);

    SysOrganizationEntity toEntity(SysOrganizationCreateCommand command);

    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SysOrganizationEntity toEntity(SysOrganizationUpdateCommand command);
}

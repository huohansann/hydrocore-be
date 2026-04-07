package com.siact.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysOrganizationCreateCommand;
import com.siact.module.system.command.SysOrganizationUpdateCommand;
import com.siact.module.system.entity.SysOrganizationEntity;
import com.siact.module.system.query.SysOrganizationQuery;
import com.siact.module.system.vo.SysOrganizationTreeVO;
import com.siact.module.system.vo.SysOrganizationVO;

import java.util.List;

public interface SysOrganizationService extends IService<SysOrganizationEntity> {
    PageVO<SysOrganizationVO> list(SysOrganizationQuery query);

    List<SysOrganizationTreeVO> tree();

    Boolean create(SysOrganizationCreateCommand command);

    Boolean update(SysOrganizationUpdateCommand command);

    Boolean delete(Long id);
}

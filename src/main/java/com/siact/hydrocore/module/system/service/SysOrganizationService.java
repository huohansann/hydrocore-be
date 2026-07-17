package com.siact.hydrocore.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.system.command.SysOrganizationCreateCommand;
import com.siact.hydrocore.module.system.command.SysOrganizationUpdateCommand;
import com.siact.hydrocore.module.system.entity.SysOrganizationEntity;
import com.siact.hydrocore.module.system.query.SysOrganizationQuery;
import com.siact.hydrocore.module.system.vo.SysOrganizationTreeVO;
import com.siact.hydrocore.module.system.vo.SysOrganizationVO;

import java.util.List;

public interface SysOrganizationService extends IService<SysOrganizationEntity> {
    PageVO<SysOrganizationVO> list(SysOrganizationQuery query);

    List<SysOrganizationTreeVO> tree();

    Boolean create(SysOrganizationCreateCommand command);

    Boolean update(SysOrganizationUpdateCommand command);

    Boolean delete(Long id);
}

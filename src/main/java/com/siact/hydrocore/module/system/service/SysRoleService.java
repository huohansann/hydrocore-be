package com.siact.hydrocore.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.system.command.SysRoleCreateCommand;
import com.siact.hydrocore.module.system.command.SysRoleUpdateCommand;
import com.siact.hydrocore.module.system.entity.SysRoleEntity;
import com.siact.hydrocore.module.system.query.SysRoleQuery;
import com.siact.hydrocore.module.system.vo.SysRoleVO;

import java.util.List;

public interface SysRoleService extends IService<SysRoleEntity> {
    PageVO<SysRoleVO> list(SysRoleQuery query);

    Boolean create(SysRoleCreateCommand command);

    Boolean update(SysRoleUpdateCommand command);

    Boolean delete(Long id);

    void assignMenus(Long roleId, List<Long> menuIds);

    List<Long> getMenuIds(Long roleId);
}

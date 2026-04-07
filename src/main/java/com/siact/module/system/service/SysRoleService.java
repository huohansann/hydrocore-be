package com.siact.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysRoleCreateCommand;
import com.siact.module.system.command.SysRoleUpdateCommand;
import com.siact.module.system.entity.SysRoleEntity;
import com.siact.module.system.query.SysRoleQuery;
import com.siact.module.system.vo.SysRoleVO;

import java.util.List;

public interface SysRoleService extends IService<SysRoleEntity> {
    PageVO<SysRoleVO> list(SysRoleQuery query);

    Boolean create(SysRoleCreateCommand command);

    Boolean update(SysRoleUpdateCommand command);

    Boolean delete(Long id);

    void assignMenus(Long roleId, List<Long> menuIds);

    List<Long> getMenuIds(Long roleId);
}

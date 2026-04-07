package com.siact.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.ResetPasswordCommand;
import com.siact.module.system.command.SysUserCreateCommand;
import com.siact.module.system.command.SysUserUpdateCommand;
import com.siact.module.system.entity.SysUserEntity;
import com.siact.module.system.query.SysUserQuery;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysUserVO;

import java.util.List;

public interface SysUserService extends IService<SysUserEntity> {
    PageVO<SysUserVO> list(SysUserQuery query);

    Boolean create(SysUserCreateCommand command);

    Boolean update(SysUserUpdateCommand command);

    Boolean delete(Long id);

    Boolean resetPassword(Long id, ResetPasswordCommand command);

    void assignRoles(Long userId, List<Long> roleIds);

    List<Long> getRoleIds(Long userId);

    List<SysMenuTreeVO> getUserMenus(Long userId);
}

package com.siact.hydrocore.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.system.command.SysMenuCreateCommand;
import com.siact.hydrocore.module.system.command.SysMenuUpdateCommand;
import com.siact.hydrocore.module.system.entity.SysMenuEntity;
import com.siact.hydrocore.module.system.query.SysMenuQuery;
import com.siact.hydrocore.module.system.vo.SysMenuTreeVO;
import com.siact.hydrocore.module.system.vo.SysMenuVO;

import java.util.List;

public interface SysMenuService extends IService<SysMenuEntity> {
    PageVO<SysMenuVO> list(SysMenuQuery query);

    List<SysMenuTreeVO> tree();

    Boolean create(SysMenuCreateCommand command);

    Boolean update(SysMenuUpdateCommand command);

    Boolean delete(Long id);
}

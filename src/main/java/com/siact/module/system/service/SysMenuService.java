package com.siact.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysMenuCreateCommand;
import com.siact.module.system.command.SysMenuUpdateCommand;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.query.SysMenuQuery;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysMenuVO;

import java.util.List;

public interface SysMenuService extends IService<SysMenuEntity> {
    PageVO<SysMenuVO> list(SysMenuQuery query);

    List<SysMenuTreeVO> tree();

    Boolean create(SysMenuCreateCommand command);

    Boolean update(SysMenuUpdateCommand command);

    Boolean delete(Long id);
}

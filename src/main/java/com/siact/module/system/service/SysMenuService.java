package com.siact.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.common.vo.PageVO;
import com.siact.module.system.command.SysMenuCreateCommand;
import com.siact.module.system.command.SysMenuDeleteCommand;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.query.SysMenuQuery;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysMenuVO;

import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-17 9:37
 * @className : SysMenuService
 * @description : 系统菜单业务类
 */
public interface SysMenuService extends IService<SysMenuEntity> {
    PageVO<SysMenuVO> list(SysMenuQuery query);

    List<SysMenuTreeVO> tree();

    Boolean create(SysMenuCreateCommand command);

    Boolean delete(List<SysMenuDeleteCommand> commands);
}

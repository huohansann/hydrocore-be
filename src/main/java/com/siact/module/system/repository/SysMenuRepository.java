package com.siact.module.system.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysMenuQueryDTO;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.enums.MenuDeleteType;

import java.util.List;
import java.util.Map;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-19 10:30
 * @className : SysMenuRepository
 * @description : 系统菜单数据持久层
 */
public interface SysMenuRepository {
    Page<SysMenuEntity> queryList(SysMenuQueryDTO queryDTO, Page<SysMenuEntity> page);

    List<SysMenuEntity> queryByIds(List<Long> ids);

    Boolean delete(Map<MenuDeleteType, List<String>> delMap);
}

package com.siact.hydrocore.module.system.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.hydrocore.module.system.dto.SysMenuQueryDTO;
import com.siact.hydrocore.module.system.entity.SysMenuEntity;

import java.util.List;

public interface SysMenuRepository {
    Page<SysMenuEntity> queryList(SysMenuQueryDTO queryDTO, Page<SysMenuEntity> page);

    List<SysMenuEntity> queryAllForTree();

    List<SysMenuEntity> queryByParentId(Long parentId);

    List<SysMenuEntity> queryByIds(List<Long> ids);
}

package com.siact.module.system.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysRoleQueryDTO;
import com.siact.module.system.entity.SysRoleEntity;

public interface SysRoleRepository {
    Page<SysRoleEntity> queryList(SysRoleQueryDTO queryDTO, Page<SysRoleEntity> page);

    boolean existsByRoleCode(String roleCode);

    boolean existsByRoleCodeExcludeId(String roleCode, Long excludeId);
}

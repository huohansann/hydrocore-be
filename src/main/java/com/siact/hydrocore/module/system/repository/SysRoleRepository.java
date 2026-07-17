package com.siact.hydrocore.module.system.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.hydrocore.module.system.dto.SysRoleQueryDTO;
import com.siact.hydrocore.module.system.entity.SysRoleEntity;

public interface SysRoleRepository {
    Page<SysRoleEntity> queryList(SysRoleQueryDTO queryDTO, Page<SysRoleEntity> page);

    boolean existsByRoleCode(String roleCode);

    boolean existsByRoleCodeExcludeId(String roleCode, Long excludeId);
}

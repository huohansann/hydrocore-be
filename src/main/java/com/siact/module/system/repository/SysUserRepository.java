package com.siact.module.system.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysUserQueryDTO;
import com.siact.module.system.entity.SysUserEntity;

public interface SysUserRepository {
    Page<SysUserEntity> queryList(SysUserQueryDTO queryDTO, Page<SysUserEntity> page);

    boolean existsByAccount(String account);
}

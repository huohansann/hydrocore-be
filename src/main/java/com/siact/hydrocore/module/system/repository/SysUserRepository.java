package com.siact.hydrocore.module.system.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.hydrocore.module.system.dto.SysUserQueryDTO;
import com.siact.hydrocore.module.system.entity.SysUserEntity;

public interface SysUserRepository {
    Page<SysUserEntity> queryList(SysUserQueryDTO queryDTO, Page<SysUserEntity> page);

    boolean existsByAccount(String account);

    SysUserEntity findByAccount(String account);
}

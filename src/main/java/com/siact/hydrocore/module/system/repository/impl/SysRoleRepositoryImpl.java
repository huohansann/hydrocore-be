package com.siact.hydrocore.module.system.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.hydrocore.module.system.dto.SysRoleQueryDTO;
import com.siact.hydrocore.module.system.entity.SysRoleEntity;
import com.siact.hydrocore.module.system.mapper.SysRoleMapper;
import com.siact.hydrocore.module.system.repository.SysRoleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class SysRoleRepositoryImpl implements SysRoleRepository {
    private final SysRoleMapper mapper;

    @Override
    public Page<SysRoleEntity> queryList(SysRoleQueryDTO queryDTO, Page<SysRoleEntity> page) {
        return mapper.selectPage(page, Wrappers.<SysRoleEntity>lambdaQuery()
                .like(StringUtils.isNotBlank(queryDTO.getRoleName()), SysRoleEntity::getRoleName, queryDTO.getRoleName())
                .eq(queryDTO.getStatus() != null, SysRoleEntity::getStatus, queryDTO.getStatus())
                .orderByAsc(SysRoleEntity::getSort));
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        return mapper.selectCount(Wrappers.<SysRoleEntity>lambdaQuery()
                .eq(SysRoleEntity::getRoleCode, roleCode)) > 0;
    }

    @Override
    public boolean existsByRoleCodeExcludeId(String roleCode, Long excludeId) {
        return mapper.selectCount(Wrappers.<SysRoleEntity>lambdaQuery()
                .eq(SysRoleEntity::getRoleCode, roleCode)
                .ne(SysRoleEntity::getId, excludeId)) > 0;
    }
}

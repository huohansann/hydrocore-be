package com.siact.module.system.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysUserQueryDTO;
import com.siact.module.system.entity.SysUserEntity;
import com.siact.module.system.mapper.SysUserMapper;
import com.siact.module.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class SysUserRepositoryImpl implements SysUserRepository {
    private final SysUserMapper mapper;

    @Override
    public Page<SysUserEntity> queryList(SysUserQueryDTO queryDTO, Page<SysUserEntity> page) {
        return mapper.selectPage(page, Wrappers.<SysUserEntity>lambdaQuery()
                .like(StringUtils.isNotBlank(queryDTO.getAccount()), SysUserEntity::getAccount, queryDTO.getAccount())
                .like(StringUtils.isNotBlank(queryDTO.getUsername()), SysUserEntity::getUsername, queryDTO.getUsername())
                .eq(queryDTO.getOrgId() != null, SysUserEntity::getOrgId, queryDTO.getOrgId())
                .eq(queryDTO.getStatus() != null, SysUserEntity::getStatus, queryDTO.getStatus())
                .orderByDesc(SysUserEntity::getCreateTime));
    }

    @Override
    public boolean existsByAccount(String account) {
        return mapper.selectCount(Wrappers.<SysUserEntity>lambdaQuery()
                .eq(SysUserEntity::getAccount, account)) > 0;
    }
}

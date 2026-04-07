package com.siact.module.system.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysOrganizationQueryDTO;
import com.siact.module.system.entity.SysOrganizationEntity;
import com.siact.module.system.mapper.SysOrganizationMapper;
import com.siact.module.system.repository.SysOrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class SysOrganizationRepositoryImpl implements SysOrganizationRepository {
    private final SysOrganizationMapper mapper;

    @Override
    public Page<SysOrganizationEntity> queryList(SysOrganizationQueryDTO queryDTO, Page<SysOrganizationEntity> page) {
        return mapper.selectPage(page, Wrappers.<SysOrganizationEntity>lambdaQuery()
                .like(StringUtils.isNotBlank(queryDTO.getOrgName()), SysOrganizationEntity::getOrgName, queryDTO.getOrgName())
                .eq(queryDTO.getStatus() != null, SysOrganizationEntity::getStatus, queryDTO.getStatus())
                .orderByAsc(SysOrganizationEntity::getSort));
    }

    @Override
    public List<SysOrganizationEntity> queryAllForTree() {
        return mapper.queryAllForTree();
    }

    @Override
    public List<SysOrganizationEntity> queryByParentId(Long parentId) {
        return mapper.selectList(Wrappers.<SysOrganizationEntity>lambdaQuery()
                .eq(SysOrganizationEntity::getParentId, parentId));
    }

    @Override
    public boolean existsByOrgCode(String orgCode) {
        return mapper.selectCount(Wrappers.<SysOrganizationEntity>lambdaQuery()
                .eq(SysOrganizationEntity::getOrgCode, orgCode)) > 0;
    }

    @Override
    public boolean existsByOrgCodeExcludeId(String orgCode, Long excludeId) {
        return mapper.selectCount(Wrappers.<SysOrganizationEntity>lambdaQuery()
                .eq(SysOrganizationEntity::getOrgCode, orgCode)
                .ne(SysOrganizationEntity::getId, excludeId)) > 0;
    }
}

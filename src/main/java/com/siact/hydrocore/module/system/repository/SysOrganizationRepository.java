package com.siact.hydrocore.module.system.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.hydrocore.module.system.dto.SysOrganizationQueryDTO;
import com.siact.hydrocore.module.system.entity.SysOrganizationEntity;

import java.util.List;

public interface SysOrganizationRepository {
    Page<SysOrganizationEntity> queryList(SysOrganizationQueryDTO queryDTO, Page<SysOrganizationEntity> page);

    List<SysOrganizationEntity> queryAllForTree();

    List<SysOrganizationEntity> queryByParentId(Long parentId);

    boolean existsByOrgCode(String orgCode);

    boolean existsByOrgCodeExcludeId(String orgCode, Long excludeId);
}

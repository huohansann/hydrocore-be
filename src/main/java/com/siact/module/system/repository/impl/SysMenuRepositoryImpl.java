package com.siact.module.system.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysMenuQueryDTO;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.mapper.SysMenuMapper;
import com.siact.module.system.repository.SysMenuRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class SysMenuRepositoryImpl implements SysMenuRepository {
    private final SysMenuMapper mapper;

    @Override
    public Page<SysMenuEntity> queryList(SysMenuQueryDTO queryDTO, Page<SysMenuEntity> page) {
        return mapper.selectPage(page, buildWrapper(queryDTO));
    }

    @Override
    public List<SysMenuEntity> queryAllForTree() {
        return mapper.queryAllForTree();
    }

    @Override
    public List<SysMenuEntity> queryByParentId(Long parentId) {
        return mapper.selectList(Wrappers.<SysMenuEntity>lambdaQuery()
                .eq(SysMenuEntity::getParentId, parentId));
    }

    @Override
    public List<SysMenuEntity> queryByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return mapper.selectList(Wrappers.<SysMenuEntity>lambdaQuery()
                .in(SysMenuEntity::getId, ids));
    }

    private LambdaQueryWrapper<SysMenuEntity> buildWrapper(SysMenuQueryDTO queryDTO) {
        return Wrappers.<SysMenuEntity>lambdaQuery()
                .eq(queryDTO.getParentId() != null, SysMenuEntity::getParentId, queryDTO.getParentId())
                .like(StringUtils.isNotBlank(queryDTO.getMenuName()), SysMenuEntity::getMenuName, queryDTO.getMenuName())
                .eq(queryDTO.getStatus() != null, SysMenuEntity::getStatus, queryDTO.getStatus())
                .orderByAsc(SysMenuEntity::getSort);
    }
}

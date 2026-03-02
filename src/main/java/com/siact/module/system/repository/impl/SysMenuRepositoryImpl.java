package com.siact.module.system.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.system.dto.SysMenuQueryDTO;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.enums.MenuDeleteType;
import com.siact.module.system.mapper.SysMenuMapper;
import com.siact.module.system.repository.SysMenuRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-19 10:31
 * @className : SysMenuRepositoryImpl
 * @description : 系统菜单数据持久层实现
 */
@RequiredArgsConstructor
@Repository
public class SysMenuRepositoryImpl implements SysMenuRepository {
    private final SysMenuMapper mapper;

    @Override
    public Page<SysMenuEntity> queryList(SysMenuQueryDTO queryDTO, Page<SysMenuEntity> page) {
        return mapper.selectPage(page, buildWrapper(queryDTO));
    }

    @Override
    public List<SysMenuEntity> queryByIds(List<Long> ids) {
        LambdaQueryWrapper<SysMenuEntity> wrapper = Wrappers.<SysMenuEntity>lambdaQuery().in(CollectionUtils.isNotEmpty(ids), SysMenuEntity::getId, ids);
        return mapper.selectList(wrapper);
    }

    @Override
    public Boolean delete(Map<MenuDeleteType, List<String>> delMap) {
        LambdaQueryWrapper<SysMenuEntity> wrapper = Wrappers.lambdaQuery();

        wrapper.and(w -> {
            delMap.forEach((key, values) -> {
                if (key == MenuDeleteType.ID) w.or().in(SysMenuEntity::getId, values);
                if (key == MenuDeleteType.CODE) w.or().in(SysMenuEntity::getCode, values);
                if (key == MenuDeleteType.PARENT_ID) w.or().in(SysMenuEntity::getParentId, values);
            });
        });

        return mapper.delete(wrapper) > 0;
    }

    private LambdaQueryWrapper<SysMenuEntity> buildWrapper(SysMenuQueryDTO queryDTO) {
        return Wrappers.<SysMenuEntity>lambdaQuery()
                .eq(ObjectUtils.isNotEmpty(queryDTO.getParentId()), SysMenuEntity::getParentId, queryDTO.getParentId())
                .eq(StringUtils.isNotBlank(queryDTO.getCode()), SysMenuEntity::getCode, queryDTO.getCode())
                .like(StringUtils.isNotBlank(queryDTO.getLabel()), SysMenuEntity::getLabel, queryDTO.getLabel())
                .orderByAsc(SysMenuEntity::getSort);
    }
}

package com.siact.module.permission.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.permission.dto.OrganizationDTO;
import com.siact.module.permission.dto.PageDTO;
import com.siact.module.permission.vo.PageVO;
import com.siact.module.permission.entity.OrganizationEntity;
import com.siact.module.permission.mapper.OrganizationMapper;
import com.siact.module.permission.service.OrganizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织Service实现类
 *
 * @author example
 */
@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, OrganizationEntity> implements OrganizationService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrganization(OrganizationDTO request) {
        OrganizationEntity organization = new OrganizationEntity();
        BeanUtil.copyProperties(request, organization);
        
        // 设置祖级列表
        String ancestors = "0";
        if (request.getParentId() != null && request.getParentId() > 0) {
            OrganizationEntity parent = getById(request.getParentId());
            if (parent != null) {
                ancestors = parent.getAncestors() + "," + parent.getId();
            }
        }
        organization.setAncestors(ancestors);
        
        // 默认值设置
        if (organization.getSort() == null) {
            organization.setSort(0);
        }
        if (organization.getStatus() == null) {
            organization.setStatus(true);
        }
        save(organization);
        return organization.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrganization(OrganizationDTO request) {
        if (request.getId() == null) {
            return false;
        }
        
        OrganizationEntity organization = getById(request.getId());
        if (organization == null) {
            return false;
        }
        
        Long oldParentId = organization.getParentId();
        Long newParentId = request.getParentId();
        
        BeanUtil.copyProperties(request, organization);
        
        // 如果父级ID变化，需要更新祖级列表
        if (newParentId != null && !newParentId.equals(oldParentId)) {
            String ancestors = "0";
            if (newParentId > 0) {
                OrganizationEntity parent = getById(newParentId);
                if (parent != null) {
                    ancestors = parent.getAncestors() + "," + parent.getId();
                }
            }
            organization.setAncestors(ancestors);
            
            // 更新所有子组织的祖级列表
            updateChildrenAncestors(organization);
        }
        
        return updateById(organization);
    }
    
    /**
     * 更新子组织的祖级列表
     *
     * @param organization 组织对象
     */
    private void updateChildrenAncestors(OrganizationEntity organization) {
        List<OrganizationEntity> children = list(new LambdaQueryWrapper<OrganizationEntity>()
                .eq(OrganizationEntity::getParentId, organization.getId()));
        
        if (CollUtil.isNotEmpty(children)) {
            for (OrganizationEntity child : children) {
                child.setAncestors(organization.getAncestors() + "," + organization.getId());
                updateById(child);
                
                // 递归更新子组织的祖级列表
                updateChildrenAncestors(child);
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrganization(Long id) {
        // 检查是否有子组织
        long count = count(new LambdaQueryWrapper<OrganizationEntity>()
                .eq(OrganizationEntity::getParentId, id));
        if (count > 0) {
            return false;
        }
        
        return removeById(id);
    }
    
    @Override
    public List<OrganizationEntity> getOrganizationTree() {
        List<OrganizationEntity> allOrgs = list(new LambdaQueryWrapper<OrganizationEntity>().orderByAsc(OrganizationEntity::getSort));
        
        // 将列表转为树形结构
        return buildTree(allOrgs);
    }
    
    /**
     * 构建组织树
     *
     * @param allOrgs 所有组织列表
     * @return 树形结构组织列表
     */
    private List<OrganizationEntity> buildTree(List<OrganizationEntity> allOrgs) {
        if (CollUtil.isEmpty(allOrgs)) {
            return new ArrayList<>();
        }
        
        // 按父ID分组
        Map<Long, List<OrganizationEntity>> parentIdMap = allOrgs.stream()
                .collect(Collectors.groupingBy(OrganizationEntity::getParentId));
        
        // 获取顶级组织
        List<OrganizationEntity> rootOrgs = parentIdMap.getOrDefault(0L, new ArrayList<>());
        
        // 递归设置子组织
        rootOrgs.forEach(org -> setChildren(org, parentIdMap));
        
        return rootOrgs;
    }
    
    /**
     * 设置子组织
     *
     * @param org 当前组织
     * @param parentIdMap 按父ID分组的组织Map
     */
    private void setChildren(OrganizationEntity org, Map<Long, List<OrganizationEntity>> parentIdMap) {
        List<OrganizationEntity> children = parentIdMap.getOrDefault(org.getId(), new ArrayList<>());
        org.setChildren(children);
        
        // 递归设置子组织的子组织
        children.forEach(child -> setChildren(child, parentIdMap));
    }
    
    @Override
    public PageVO<OrganizationEntity> pageOrganization(PageDTO request) {
        Page<OrganizationEntity> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<OrganizationEntity> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键字查询
        if (StrUtil.isNotBlank(request.getKeyword())) {
            queryWrapper.like(OrganizationEntity::getName, request.getKeyword())
                    .or().like(OrganizationEntity::getCode, request.getKeyword());
        }
        
        // 排序
        queryWrapper.orderByAsc(OrganizationEntity::getSort);
        
        IPage<OrganizationEntity> result = page(page, queryWrapper);
        
        return PageVO.build(result);
    }
    
    @Override
    public OrganizationEntity getOrganizationById(Long id) {
        return getById(id);
    }
} 
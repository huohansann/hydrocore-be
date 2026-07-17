package com.siact.hydrocore.module.system.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.system.command.ResetPasswordCommand;
import com.siact.hydrocore.module.system.command.SysUserCreateCommand;
import com.siact.hydrocore.module.system.command.SysUserUpdateCommand;
import com.siact.hydrocore.module.system.convert.SysMenuConvert;
import com.siact.hydrocore.module.system.convert.SysUserConvert;
import com.siact.hydrocore.module.system.dto.SysUserQueryDTO;
import com.siact.hydrocore.module.system.entity.SysMenuEntity;
import com.siact.hydrocore.module.system.entity.SysOrganizationEntity;
import com.siact.hydrocore.module.system.entity.SysUserEntity;
import com.siact.hydrocore.module.system.mapper.SysMenuMapper;
import com.siact.hydrocore.module.system.mapper.SysOrganizationMapper;
import com.siact.hydrocore.module.system.mapper.SysRoleMenuMapper;
import com.siact.hydrocore.module.system.mapper.SysUserMapper;
import com.siact.hydrocore.module.system.mapper.SysUserRoleMapper;
import com.siact.hydrocore.module.system.query.SysUserQuery;
import com.siact.hydrocore.module.system.repository.SysUserRepository;
import com.siact.hydrocore.module.system.service.SysUserService;
import com.siact.hydrocore.module.system.vo.SysMenuTreeVO;
import com.siact.hydrocore.module.system.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements SysUserService {
    private final SysUserConvert convert;
    private final SysMenuConvert menuConvert;
    private final SysUserRepository repository;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final SysOrganizationMapper organizationMapper;

    @Override
    public PageVO<SysUserVO> list(SysUserQuery query) {
        SysUserQueryDTO queryDTO = convert.toQueryDTO(query);
        Page<SysUserEntity> page = repository.queryList(queryDTO, Page.of(query.getPage(), query.getPageSize()));
        List<SysUserVO> voList = convert.toVOList(page.getRecords());

        // 批量填充组织名称
        Set<Long> orgIds = voList.stream()
                .map(SysUserVO::getOrgId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!orgIds.isEmpty()) {
            Map<Long, String> orgNameMap = organizationMapper.selectBatchIds(orgIds).stream()
                    .collect(Collectors.toMap(SysOrganizationEntity::getId, SysOrganizationEntity::getOrgName));
            voList.forEach(vo -> {
                if (vo.getOrgId() != null) {
                    vo.setOrgName(orgNameMap.get(vo.getOrgId()));
                }
            });
        }

        return PageVO.<SysUserVO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(voList)
                .build();
    }

    @Override
    public Boolean create(SysUserCreateCommand command) {
        if (repository.existsByAccount(command.getAccount())) {
            throw new RuntimeException("用户名已存在");
        }
        SysUserEntity entity = convert.toEntity(command);
        entity.setPassword(BCrypt.hashpw(command.getPassword()));
        return this.save(entity);
    }

    @Override
    public Boolean update(SysUserUpdateCommand command) {
        SysUserEntity entity = convert.toEntity(command);
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        userRoleMapper.deleteByUserId(id);
        return this.removeById(id);
    }

    @Override
    public Boolean resetPassword(Long id, ResetPasswordCommand command) {
        SysUserEntity entity = new SysUserEntity();
        entity.setId(id);
        entity.setPassword(BCrypt.hashpw(command.getNewPassword()));
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            userRoleMapper.batchInsert(userId, roleIds);
        }
    }

    @Override
    public List<Long> getRoleIds(Long userId) {
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public List<SysMenuTreeVO> getUserMenus(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleIds(roleIds);
        if (CollectionUtils.isEmpty(menuIds)) {
            return Collections.emptyList();
        }

        List<SysMenuEntity> menus = menuMapper.selectBatchIds(menuIds);
        if (CollectionUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }

        Map<Long, SysMenuTreeVO> treeMap = menus.stream()
                .map(menuConvert::toTreeVO)
                .collect(Collectors.toMap(SysMenuTreeVO::getId, Function.identity()));

        List<SysMenuTreeVO> roots = new ArrayList<>();
        List<SysMenuTreeVO> sorted = treeMap.values().stream()
                .sorted(Comparator.comparingLong(SysMenuTreeVO::getParentId)
                        .thenComparingInt(SysMenuTreeVO::getSort))
                .collect(Collectors.toList());

        for (SysMenuTreeVO node : sorted) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0L || !treeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                SysMenuTreeVO parent = treeMap.get(parentId);
                if (CollectionUtils.isEmpty(parent.getChildren())) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        }
        return roots;
    }
}

package com.siact.module.permission.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.permission.dto.PageDTO;
import com.siact.module.permission.dto.RoleDTO;
import com.siact.module.permission.vo.PageVO;
import com.siact.module.permission.entity.RoleEntity;
import com.siact.module.permission.entity.RoleMenuEntity;
import com.siact.module.permission.mapper.RoleMapper;
import com.siact.module.permission.mapper.RoleMenuMapper;
import com.siact.module.permission.service.RoleService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色Service实现类
 *
 * @author example
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, RoleEntity> implements RoleService {
    
    @Resource
    private RoleMenuMapper roleMenuMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRole(RoleDTO request) {
        RoleEntity role = new RoleEntity();
        BeanUtil.copyProperties(request, role);
        
        // 默认值设置
        if (role.getSort() == null) {
            role.setSort(0);
        }
        if (role.getStatus() == null) {
            role.setStatus(true);
        }
        
        boolean result = save(role);
        
        // 保存角色菜单关联
        if (result && CollUtil.isNotEmpty(request.getMenuIds())) {
            saveRoleMenus(role.getId(), request.getMenuIds());
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(RoleDTO request) {
        if (request.getId() == null) {
            return false;
        }
        
        RoleEntity role = getById(request.getId());
        if (role == null) {
            return false;
        }
        
        BeanUtil.copyProperties(request, role);
        
        boolean result = updateById(role);
        
        // 更新角色菜单关联
        if (result && request.getMenuIds() != null) {
            // 先删除旧的关联
            roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenuEntity>()
                    .eq(RoleMenuEntity::getRoleId, role.getId()));
            
            // 保存新的关联
            if (CollUtil.isNotEmpty(request.getMenuIds())) {
                saveRoleMenus(role.getId(), request.getMenuIds());
            }
        }
        
        return result;
    }
    
    /**
     * 保存角色菜单关联
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     */
    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        if (CollUtil.isEmpty(menuIds)) {
            return;
        }
        
        // 批量插入
        roleMenuMapper.batchInsert(roleId, menuIds);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        // 删除角色菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenuEntity>()
                .eq(RoleMenuEntity::getRoleId, id));
        
        // 删除角色
        return removeById(id);
    }
    
    @Override
    public PageVO<RoleEntity> pageRole(PageDTO request) {
        Page<RoleEntity> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<RoleEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(request.getRoleName())) {
            queryWrapper.like(RoleEntity::getName, request.getRoleName());
        }

        // 关键字查询
        if (StrUtil.isNotBlank(request.getKeyword())) {
            queryWrapper.like(RoleEntity::getCode, request.getKeyword())
                    .or().like(RoleEntity::getDescription, request.getKeyword());
        }
        
        // 排序
        queryWrapper.orderByAsc(RoleEntity::getSort);

        queryWrapper.eq(RoleEntity::getDeleted, 0);
        IPage<RoleEntity> result = page(page, queryWrapper);

        // 权限赋值
        List<RoleEntity> records = result.getRecords();
        // 获取角色关联的菜单ID列表
        LambdaQueryWrapper<RoleMenuEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(CollectionUtils.isNotEmpty(records),RoleMenuEntity::getRoleId, records.stream().map(RoleEntity::getId).collect(Collectors.toList()));
        List<RoleMenuEntity> roleMenuEntities = roleMenuMapper.selectList(lambdaQueryWrapper);
        Map<Long,List<Long>> roleMenuMap = CollectionUtils.isNotEmpty(roleMenuEntities)?roleMenuEntities.stream().collect(Collectors.groupingBy(RoleMenuEntity::getRoleId, Collectors.mapping(RoleMenuEntity::getMenuId, Collectors.toList()))):null;
        if (roleMenuMap!=null) {
            records.forEach(role->{
                role.setMenuIds(roleMenuMap.get(role.getId()));
            });
        }
        return PageVO.build(result);
    }
    
    @Override
    public RoleEntity getRoleById(Long id) {
        RoleEntity role = getById(id);
        if (role != null) {
            // 查询角色关联的菜单ID列表
            List<RoleMenuEntity> roleMenus = roleMenuMapper.selectList(new LambdaQueryWrapper<RoleMenuEntity>()
                    .eq(RoleMenuEntity::getRoleId, id));
            
            if (CollUtil.isNotEmpty(roleMenus)) {
                List<Long> menuIds = roleMenus.stream()
                        .map(RoleMenuEntity::getMenuId)
                        .collect(Collectors.toList());
                role.setMenuIds(menuIds);
            }
        }
        return role;
    }
    
    @Override
    public List<RoleEntity> listAllRoles() {
        return list(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getStatus, true)
                .orderByAsc(RoleEntity::getSort));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null) {
            return false;
        }
        
        // 先删除旧的关联
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenuEntity>()
                .eq(RoleMenuEntity::getRoleId, roleId));
        
        // 保存新的关联
        if (CollUtil.isNotEmpty(menuIds)) {
            saveRoleMenus(roleId, menuIds);
        }
        
        return true;
    }
} 
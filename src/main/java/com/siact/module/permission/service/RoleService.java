package com.siact.module.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.permission.dto.PageDTO;
import com.siact.module.permission.dto.RoleDTO;
import com.siact.module.permission.vo.PageVO;
import com.siact.module.permission.entity.RoleEntity;

import java.util.List;

/**
 * 角色Service接口
 *
 * @author example
 */
public interface RoleService extends IService<RoleEntity> {
    
    /**
     * 保存角色
     *
     * @param request 角色请求DTO
     * @return 是否成功
     */
    boolean saveRole(RoleDTO request);
    
    /**
     * 更新角色
     *
     * @param request 角色请求DTO
     * @return 是否成功
     */
    boolean updateRole(RoleDTO request);
    
    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long id);
    
    /**
     * 分页查询角色
     *
     * @param request 分页请求DTO
     * @return 分页结果
     */
    PageVO<RoleEntity> pageRole(PageDTO request);
    
    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    RoleEntity getRoleById(Long id);
    
    /**
     * 获取所有角色列表
     *
     * @return 角色列表
     */
    List<RoleEntity> listAllRoles();
    
    /**
     * 分配菜单权限
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean assignMenus(Long roleId, List<Long> menuIds);
} 
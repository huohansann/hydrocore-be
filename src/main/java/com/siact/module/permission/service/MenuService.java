package com.siact.module.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.permission.dto.MenuDTO;
import com.siact.module.permission.dto.PageDTO;
import com.siact.module.permission.vo.PageVO;
import com.siact.module.permission.entity.MenuEntity;
import com.siact.module.permission.vo.MenuVO;

import java.util.List;

/**
 * 菜单Service接口
 *
 * @author example
 */
public interface MenuService extends IService<MenuEntity> {
    
    /**
     * 保存菜单
     *
     * @param request 菜单请求DTO
     * @return 是否成功
     */
    boolean saveMenu(MenuDTO request);
    
    /**
     * 更新菜单
     *
     * @param request 菜单请求DTO
     * @return 是否成功
     */
    boolean updateMenu(MenuDTO request);
    
    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return 是否成功
     */
    boolean deleteMenu(Long id);
    
    /**
     * 获取菜单树
     *
     * @return 菜单树列表
     */
    List<MenuVO> getMenuTree(Long parentId, Integer modelShow);
    
    /**
     * 分页查询菜单
     *
     * @param request 分页请求DTO
     * @return 分页结果
     */
    PageVO<MenuEntity> pageMenu(PageDTO request);
    
    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    MenuEntity getMenuById(Long id);
    
    /**
     * 根据角色ID列表获取菜单列表
     *
     * @param roleIds 角色ID列表
     * @return 菜单列表
     */
    List<MenuVO> getMenusByRoleIds(List<Long> roleIds);

    List<Long> getMenuIdsByRoleIds(List<Long> roleIds);


    /**
     * @author: HouBo
     * @CreateTime: 2026/3/31 10:13
     * @Description: 根据角色ID列表获取对应的菜单实体列表
     */
    List<MenuEntity> getMenusEntityByRoleIds(List<Long> roleIds);
}
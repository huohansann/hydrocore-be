package com.siact.module.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.permission.entity.MenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单Mapper接口
 *
 * @author example
 */
@Mapper
public interface MenuMapper extends BaseMapper<MenuEntity> {
    
    /**
     * 根据角色ID列表查询菜单ID列表
     *
     * @param roleIds 角色ID列表
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleIds(@Param("roleIds") List<Long> roleIds);
    
    /**
     * 根据角色ID列表查询菜单列表
     *
     * @param roleIds 角色ID列表
     * @return 菜单列表
     */
    List<MenuEntity> selectMenusByRoleIds(@Param("roleIds") List<Long> roleIds);
} 
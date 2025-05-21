package com.siact.module.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.permission.entity.RoleMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色菜单关联Mapper接口
 *
 * @author example
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenuEntity> {
    
    /**
     * 批量插入角色菜单关联
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 影响行数
     */
    int batchInsert(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    /**
     * 根据角色ID查询角色菜单关联
     *
     * @param roleIds 角色ID列表
     * @return 角色菜单关联列表
     */
    List<RoleMenuEntity> selectListByRoleId(List<Long> roleIds);
}
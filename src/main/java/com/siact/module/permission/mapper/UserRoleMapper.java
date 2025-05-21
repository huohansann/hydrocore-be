package com.siact.module.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.permission.entity.UserRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 *
 * @author example
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {
    
    /**
     * 批量插入用户角色关联
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 影响行数
     */
    int batchInsert(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
    
    /**
     * 根据用户ID查询角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
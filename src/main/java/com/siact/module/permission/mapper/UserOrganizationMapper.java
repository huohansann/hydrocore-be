package com.siact.module.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.permission.entity.UserOrganizationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户组织关联Mapper接口
 *
 * @author example
 */
@Mapper
public interface UserOrganizationMapper extends BaseMapper<UserOrganizationEntity> {
    
    /**
     * 批量插入用户组织关联
     *
     * @param userId 用户ID
     * @param orgIds 组织ID列表
     * @return 影响行数
     */
    int batchInsert(@Param("userId") Long userId, @Param("orgIds") List<Long> orgIds);
    
    /**
     * 根据用户ID查询组织ID列表
     *
     * @param userId 用户ID
     * @return 组织ID列表
     */
    List<Long> selectOrgIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询组织ID列表
     *
     * @param orgId 用户ID
     * @return 组织ID列表
     */
    List<Long> selectUserIdsByOrgId(@Param("orgId") Long orgId);
} 
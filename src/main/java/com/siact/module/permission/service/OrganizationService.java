package com.siact.module.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.permission.dto.OrganizationDTO;
import com.siact.module.permission.dto.PageDTO;
import com.siact.module.permission.vo.PageVO;
import com.siact.module.permission.entity.OrganizationEntity;

import java.util.List;

/**
 * 组织Service接口
 *
 * @author example
 */
public interface OrganizationService extends IService<OrganizationEntity> {
    
    /**
     * 保存组织
     *
     * @param request 组织请求DTO
     * @return 是否成功
     */
    Long saveOrganization(OrganizationDTO request);
    
    /**
     * 更新组织
     *
     * @param request 组织请求DTO
     * @return 是否成功
     */
    boolean updateOrganization(OrganizationDTO request);
    
    /**
     * 删除组织
     *
     * @param id 组织ID
     * @return 是否成功
     */
    boolean deleteOrganization(Long id);
    
    /**
     * 获取组织树
     *
     * @return 组织树列表
     */
    List<OrganizationEntity> getOrganizationTree();
    
    /**
     * 分页查询组织
     *
     * @param request 分页请求DTO
     * @return 分页结果
     */
    PageVO<OrganizationEntity> pageOrganization(PageDTO request);
    
    /**
     * 根据ID获取组织详情
     *
     * @param id 组织ID
     * @return 组织详情
     */
    OrganizationEntity getOrganizationById(Long id);
} 
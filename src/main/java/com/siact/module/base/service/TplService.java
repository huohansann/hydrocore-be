package com.siact.module.base.service;

import com.siact.module.base.dto.TplDTO;
import com.siact.module.base.dto.TplQuery;
import com.siact.module.base.vo.TplVO;

import java.util.List;

/**
 * 模板表 服务接口
 * 
 * @author siact
 */
public interface TplService {
    
    /**
     * 查询模板信息
     * 
     * @param id 模板ID
     * @return 模板信息
     */
    TplVO selectTplById(Long id);
    
    /**
     * 查询模板列表
     * 
     * @param query 查询条件
     * @return 模板集合
     */
    List<TplVO> selectTplList(TplQuery query);
    
    /**
     * 根据模板类型查询模板
     * 
     * @param tplType 模板类型
     * @return 模板集合
     */
    List<TplVO> selectTplByType(String tplType);
    
    /**
     * 根据模板编码查询模板
     * 
     * @param tplCode 模板编码
     * @return 模板信息
     */
    TplVO selectTplByCode(String tplCode);
    
    /**
     * 新增模板
     * 
     * @param dto 模板信息
     * @return 结果
     */
    int insertTpl(TplDTO dto);
    
    /**
     * 修改模板
     * 
     * @param dto 模板信息
     * @return 结果
     */
    int updateTpl(TplDTO dto);
    
    /**
     * 批量删除模板
     * 
     * @param ids 需要删除的模板ID
     * @return 结果
     */
    int deleteTplByIds(Long[] ids);
    
    /**
     * 删除模板信息
     * 
     * @param id 模板ID
     * @return 结果
     */
    int deleteTplById(Long id);
} 
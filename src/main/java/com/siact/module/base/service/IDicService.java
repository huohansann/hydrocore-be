package com.siact.module.base.service;

import com.siact.module.base.dto.DicDTO;
import com.siact.module.base.dto.DicQuery;
import com.siact.module.base.vo.DicVO;

import java.util.List;

/**
 * 字典表 服务接口
 * 
 * @author siact
 */
public interface IDicService {
    
    /**
     * 查询字典信息
     * 
     * @param id 字典ID
     * @return 字典信息
     */
    DicVO selectDicById(Long id);
    
    /**
     * 查询字典列表
     * 
     * @param query 查询条件
     * @return 字典集合
     */
    List<DicVO> selectDicList(DicQuery query);
    
    /**
     * 根据字典类型查询字典数据
     * 
     * @param type 字典类型
     * @return 字典数据集合
     */
    List<DicVO> selectDicByType(String type);
    
    /**
     * 根据字典类型和编码查询字典数据
     * 
     * @param type 字典类型
     * @param code 字典编码
     * @return 字典数据
     */
    DicVO selectDicByTypeAndCode(String type, String code);
    
    /**
     * 新增字典
     * 
     * @param dto 字典信息
     * @return 结果
     */
    int insertDic(DicDTO dto);
    
    /**
     * 修改字典
     * 
     * @param dto 字典信息
     * @return 结果
     */
    int updateDic(DicDTO dto);
    
    /**
     * 批量删除字典
     * 
     * @param ids 需要删除的字典ID
     * @return 结果
     */
    int deleteDicByIds(Long[] ids);
    
    /**
     * 删除字典信息
     * 
     * @param id 字典ID
     * @return 结果
     */
    int deleteDicById(Long id);
    
    /**
     * 构建树形字典结构
     * 
     * @param dicList 字典列表
     * @return 树形字典列表
     */
    List<DicVO> buildDicTree(List<DicVO> dicList);

} 
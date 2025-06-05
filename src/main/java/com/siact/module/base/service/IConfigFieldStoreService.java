package com.siact.module.base.service;

import com.siact.module.base.dto.ConfigFieldStoreDTO;
import com.siact.module.base.dto.ConfigFieldStoreQuery;
import com.siact.module.base.vo.ConfigFieldStoreVO;
import java.util.List;

/**
 * 配置字段存储 服务接口
 *
 * @author siact
 */
public interface IConfigFieldStoreService {
    /**
     * 查询配置信息
     * @param id 主键ID
     * @return 配置信息
     */
    ConfigFieldStoreVO selectConfigFieldStoreById(Long id);

    /**
     * 查询配置列表
     * @param query 查询条件
     * @return 配置集合
     */
    List<ConfigFieldStoreVO> selectConfigFieldStoreList(ConfigFieldStoreQuery query);

    /**
     * 新增配置
     * @param dto 配置信息
     * @return 结果
     */
    int insertConfigFieldStore(ConfigFieldStoreDTO dto);

    /**
     * 修改配置
     * @param dto 配置信息
     * @return 结果
     */
    int updateConfigFieldStore(List<ConfigFieldStoreDTO> dtoList);

    /**
     * 批量删除配置
     * @param ids 需要删除的ID
     * @return 结果
     */
    int deleteConfigFieldStoreByIds(Long[] ids);

} 
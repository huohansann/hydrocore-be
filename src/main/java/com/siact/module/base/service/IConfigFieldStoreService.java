package com.siact.module.base.service;

import com.siact.module.base.dto.ConfigFieldStoreDTO;
import com.siact.module.base.dto.ConfigFieldStoreQuery;
import com.siact.module.base.vo.ConfigFieldStoreVO;

import java.util.List;

public interface IConfigFieldStoreService {
    ConfigFieldStoreVO selectConfigFieldStoreById(Long id);

    List<ConfigFieldStoreVO> selectConfigFieldStoreList(ConfigFieldStoreQuery query);

    int insertConfigFieldStore(ConfigFieldStoreDTO dto);

    int updateConfigFieldStore(List<ConfigFieldStoreDTO> dtoList);

    int deleteConfigFieldStoreByIds(Long[] ids);

    int deleteConfigFieldStoreById(Long id);
}

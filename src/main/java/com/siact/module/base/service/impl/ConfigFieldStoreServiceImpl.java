package com.siact.module.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.base.dto.ConfigFieldStoreDTO;
import com.siact.module.base.dto.ConfigFieldStoreQuery;
import com.siact.module.base.entity.ConfigFieldStoreEntity;
import com.siact.module.base.mapper.ConfigFieldStoreMapper;
import com.siact.module.base.service.IConfigFieldStoreService;
import com.siact.module.base.vo.ConfigFieldStoreVO;
import com.siact.common.utils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfigFieldStoreServiceImpl extends ServiceImpl<ConfigFieldStoreMapper, ConfigFieldStoreEntity> implements IConfigFieldStoreService {
    @Override
    public ConfigFieldStoreVO selectConfigFieldStoreById(Long id) {
        ConfigFieldStoreEntity entity = this.getById(id);
        return ConvertUtils.sourceToTarget(entity, ConfigFieldStoreVO.class);
    }

    @Override
    public List<ConfigFieldStoreVO> selectConfigFieldStoreList(ConfigFieldStoreQuery query) {
        LambdaQueryWrapper<ConfigFieldStoreEntity> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotEmpty(query.getFieldKey())) {
            String fieldKey = query.getFieldKey();
            if (query.getIsLike()) {
                // 对 like 中的特殊字符进行转义，防止通配符误匹配
                String escapedFieldKey = fieldKey.replace("%", "\\%").replace("_", "\\_");
                wrapper.like(ConfigFieldStoreEntity::getFieldKey, escapedFieldKey);
            } else {
                wrapper.eq(ConfigFieldStoreEntity::getFieldKey, fieldKey);

            }
        }
        // 这里只做简单示例，实际可根据query构造Wrapper
        List<ConfigFieldStoreEntity> list = this.list(wrapper);
        return list.stream().map(e -> ConvertUtils.sourceToTarget(e, ConfigFieldStoreVO.class)).collect(Collectors.toList());
    }

    @Override
    public int insertConfigFieldStore(ConfigFieldStoreDTO dto) {
        ConfigFieldStoreEntity entity = ConvertUtils.sourceToTarget(dto, ConfigFieldStoreEntity.class);
        return this.save(entity) ? 1 : 0;
    }

    @Override
    public int updateConfigFieldStore(List<ConfigFieldStoreDTO> dtoList) {
        List<ConfigFieldStoreEntity> entities =
                ConvertUtils.sourceToTarget(dtoList, ConfigFieldStoreEntity.class);
        return this.updateBatchById(entities) ? 1 : 0;
    }

    @Override
    public int deleteConfigFieldStoreByIds(Long[] ids) {
        return this.removeByIds(java.util.Arrays.asList(ids)) ? 1 : 0;
    }

    @Override
    public int deleteConfigFieldStoreById(Long id) {
        return this.removeById(id) ? 1 : 0;
    }
} 
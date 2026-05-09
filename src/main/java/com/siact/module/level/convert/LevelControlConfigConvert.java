package com.siact.module.level.convert;

import com.siact.module.level.dto.LevelControlConfigDTO;
import com.siact.module.level.entity.LevelControlConfigEntity;
import com.siact.module.level.vo.LevelControlConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LevelControlConfigConvert {

    LevelControlConfigVO toVO(LevelControlConfigEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    LevelControlConfigEntity toEntity(LevelControlConfigDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    LevelControlConfigEntity copy(LevelControlConfigEntity entity);
}

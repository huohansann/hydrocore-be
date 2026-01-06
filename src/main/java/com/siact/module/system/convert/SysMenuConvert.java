package com.siact.module.system.convert;

import com.siact.module.system.command.SysMenuCreateCommand;
import com.siact.module.system.dto.SysMenuQueryDTO;
import com.siact.module.system.entity.SysMenuEntity;
import com.siact.module.system.query.SysMenuQuery;
import com.siact.module.system.vo.SysMenuTreeVO;
import com.siact.module.system.vo.SysMenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-19 9:48
 * @className : SysMenuConvert
 * @description : 系统菜单对象转换器
 */
@Mapper(componentModel = "spring")
public interface SysMenuConvert {
    @Mapping(source = "name", target = "label")
    SysMenuQueryDTO toQueryDTO(SysMenuQuery query);

    @Mappings({
            @Mapping(source = "label", target = "name"),
            @Mapping(source = "isShow", target = "show")
    })
    SysMenuVO toVO(SysMenuEntity entity);

    List<SysMenuVO> toVO(List<SysMenuEntity> entities);

    @Mappings({
            @Mapping(source = "label", target = "name"),
            @Mapping(source = "isShow", target = "show")
    })
    SysMenuTreeVO toTreeVO(SysMenuEntity entity);

    @Mappings({
            @Mapping(source = "name", target = "label"),
            @Mapping(source = "show", target = "isShow")
    })
    SysMenuEntity toEntity(SysMenuCreateCommand command);
}

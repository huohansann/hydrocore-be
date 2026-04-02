package com.siact.module.system.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.system.command.SysConfigCreateCommand;
import com.siact.module.system.command.SysConfigUpdateCommand;
import com.siact.module.system.dto.SysConfigDTO;
import com.siact.module.system.dto.SysConfigItemDTO;
import com.siact.module.system.entity.SysConfigEntity;
import com.siact.module.system.enums.SysConfigModuleEnum;

import java.util.List;
import java.util.Map;

/**
 * 系统选项配置服务接口
 *
 * @author siact
 */
public interface SysConfigService extends IService<SysConfigEntity> {

    /**
     * 获取配置（按 scCode）
     *
     * @param scCode 配置编码
     * @return 组装后的配置对象
     */
    SysConfigDTO getByCode(String scCode);

    /**
     * 创建配置
     *
     * @param command 创建命令
     * @return 是否成功
     */
    Boolean create(SysConfigCreateCommand command);

    /**
     * 更新配置（完整覆盖）
     *
     * @param scCode  配置编码
     * @param command 更新命令
     * @return 是否成功
     */
    Boolean update(String scCode, SysConfigUpdateCommand command);

    /**
     * 删除配置（按 scCode）
     *
     * @param scCode 配置编码
     * @return 是否成功
     */
    Boolean deleteByCode(String scCode);

    /**
     * 按模块查询所有配置
     *
     * @param module 模块
     * @return 配置列表
     */
    List<SysConfigDTO> listByModule(SysConfigModuleEnum module);

    /**
     * 按编码列表批量查询
     *
     * @param scCodes 配置编码列表
     * @return 配置 Map（key: scCode）
     */
    Map<String, SysConfigDTO> batchGet(List<String> scCodes);

    /**
     * 获取单个配置项
     *
     * @param scCode 配置编码
     * @param scPath 配置路径
     * @return 单项配置数据
     */
    SysConfigItemDTO getItem(String scCode, String scPath);

    /**
     * 更新单个配置项
     *
     * @param scCode  配置编码
     * @param scPath  配置路径
     * @param value   新值
     * @param version 版本号
     * @return 是否成功
     */
    Boolean updateItem(String scCode, String scPath, String value, Integer version);

    /**
     * 删除单个配置项
     *
     * @param scCode 配置编码
     * @param scPath 配置路径
     * @return 是否成功
     */
    Boolean deleteItem(String scCode, String scPath);

    /**
     * 全量刷新配置
     *
     * @param scCode  配置编码
     * @param command 更新命令
     * @return 是否成功
     */
    Boolean refresh(String scCode, SysConfigUpdateCommand command);
}
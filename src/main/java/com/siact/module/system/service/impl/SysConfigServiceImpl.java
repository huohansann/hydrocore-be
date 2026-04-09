package com.siact.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.siact.common.exception.BizException;
import com.siact.common.redis.RedisService;
import com.siact.common.utils.JacksonUtils;
import com.siact.module.system.command.SysConfigCreateCommand;
import com.siact.module.system.command.SysConfigUpdateCommand;
import com.siact.module.system.dto.SysConfigDTO;
import com.siact.module.system.dto.SysConfigItemDTO;
import com.siact.module.system.entity.SysConfigEntity;
import com.siact.module.system.enums.SysConfigModuleEnum;
import com.siact.module.system.mapper.SysConfigMapper;
import com.siact.module.system.processor.ConfigAssembler;
import com.siact.module.system.processor.ConfigFlattener;
import com.siact.module.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 系统选项配置服务实现
 *
 * @author siact
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfigEntity> implements SysConfigService {

    private final ConfigFlattener flattener;
    private final ConfigAssembler assembler;
    private final RedisService redisService;

    private static final String CACHE_KEY_PREFIX = "sys:config:";
    private static final String CACHE_KEY_MODULE = "sys:config:module:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    @Override
    public SysConfigDTO getByCode(String scCode) {
        String cacheKey = CACHE_KEY_PREFIX + scCode;

        // 1. 查缓存
        Object cached = redisService.getCacheObject(cacheKey);
        if (cached instanceof String) {
            try {
                return JacksonUtils.fromJson((String) cached, SysConfigDTO.class);
            } catch (Exception e) {
                log.warn("解析配置缓存失败，回退数据库查询: {}", e.getMessage());
            }
        }

        // 2. 查数据库
        List<SysConfigEntity> entities = listByScCode(scCode);
        if (entities.isEmpty()) {
            return null;
        }

        // 3. 组装并缓存
        SysConfigDTO dto = assembleDTO(entities);
        writeCache(cacheKey, dto);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(SysConfigCreateCommand command) {
        // 1. 检查 scCode 是否已存在
        if (existsByScCode(command.getScCode())) {
            throw new BizException("配置编码已存在: " + command.getScCode());
        }

        // 2. 扁平化并保存
        List<SysConfigEntity> entities = flattener.flatten(
                command.getModule(),
                command.getScCode(),
                command.getScName(),
                command.getDescription(),
                command.getData()
        );

        if (entities.isEmpty()) {
            // 空 JSON 对象，创建一个占位记录
            SysConfigEntity placeholder = new SysConfigEntity();
            placeholder.setModule(command.getModule());
            placeholder.setScCode(command.getScCode());
            placeholder.setScPath("");
            placeholder.setScName(command.getScName());
            placeholder.setScType(null);
            placeholder.setScValue("");
            placeholder.setDescription(command.getDescription());
            placeholder.setVersion(1);
            entities.add(placeholder);
        }

        boolean success = saveBatch(entities);

        // 3. 写缓存
        if (success) {
            SysConfigDTO dto = assembleDTO(entities);
            String cacheKey = CACHE_KEY_PREFIX + command.getScCode();
            writeCache(cacheKey, dto);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(String scCode, SysConfigUpdateCommand command) {
        // 1. 查询现有数据获取 module 和版本校验
        List<SysConfigEntity> existing = listByScCode(scCode);
        if (existing.isEmpty()) {
            throw new BizException("配置不存在: " + scCode);
        }

        // 2. 版本校验（取第一条记录的版本）
        Integer currentVersion = existing.get(0).getVersion();
        if (!currentVersion.equals(command.getVersion())) {
            throw new BizException("配置已被修改，请刷新后重试");
        }

        SysConfigModuleEnum module = existing.get(0).getModule();
        String scName = command.getScName() != null ? command.getScName() : existing.get(0).getScName();
        String description = command.getDescription() != null ? command.getDescription() : existing.get(0).getDescription();

        // 3. 扁平化新数据
        List<SysConfigEntity> newEntities = flattener.flatten(module, scCode, scName, description, command.getData());

        // 4. 设置新版本号
        int newVersion = currentVersion + 1;
        for (SysConfigEntity entity : newEntities) {
            entity.setVersion(newVersion);
        }

        // 5. 删除旧数据，插入新数据
        deleteByScCodeInternal(scCode);
        boolean success = saveBatch(newEntities);

        // 6. 删除缓存
        if (success) {
            evictCache(scCode, module);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByCode(String scCode) {
        List<SysConfigEntity> entities = listByScCode(scCode);
        if (entities.isEmpty()) {
            return false;
        }

        SysConfigModuleEnum module = entities.get(0).getModule();
        boolean success = deleteByScCodeInternal(scCode);

        if (success) {
            evictCache(scCode, module);
        }

        return success;
    }

    @Override
    public List<SysConfigDTO> listByModule(SysConfigModuleEnum module) {
        String cacheKey = CACHE_KEY_MODULE + module.name();

        // 1. 查缓存
        Object cached = redisService.getCacheObject(cacheKey);
        if (cached instanceof String) {
            try {
                return JacksonUtils.fromJson((String) cached, new TypeReference<List<SysConfigDTO>>() {});
            } catch (Exception e) {
                log.warn("解析模块配置缓存失败，回退数据库查询: {}", e.getMessage());
            }
        }

        // 2. 查数据库
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getModule, module);
        List<SysConfigEntity> allEntities = baseMapper.selectList(wrapper);

        // 3. 按 scCode 分组并组装
        Map<String, List<SysConfigEntity>> grouped = allEntities.stream()
                .collect(Collectors.groupingBy(SysConfigEntity::getScCode));

        List<SysConfigDTO> resultList = new ArrayList<>();
        for (Map.Entry<String, List<SysConfigEntity>> entry : grouped.entrySet()) {
            SysConfigDTO dto = assembleDTO(entry.getValue());
            resultList.add(dto);
        }

        // 4. 缓存
        writeCache(cacheKey, resultList);

        return resultList;
    }

    @Override
    public Map<String, SysConfigDTO> batchGet(List<String> scCodes) {
        Map<String, SysConfigDTO> result = new HashMap<>();

        for (String scCode : scCodes) {
            SysConfigDTO dto = getByCode(scCode);
            if (dto != null) {
                result.put(scCode, dto);
            }
        }

        return result;
    }

    @Override
    public SysConfigItemDTO getItem(String scCode, String scPath) {
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getScCode, scCode)
                .eq(SysConfigEntity::getScPath, scPath);

        SysConfigEntity entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            return null;
        }

        return toItemDTO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateItem(String scCode, String scPath, String value, Integer version) {
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getScCode, scCode)
                .eq(SysConfigEntity::getScPath, scPath);

        SysConfigEntity entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BizException("配置项不存在: " + scCode + "/" + scPath);
        }

        if (!entity.getVersion().equals(version)) {
            throw new BizException("配置已被修改，请刷新后重试");
        }

        entity.setScValue(value);
        entity.setVersion(version + 1);

        int rows = baseMapper.updateById(entity);

        if (rows > 0) {
            evictCache(scCode, entity.getModule());
        }

        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteItem(String scCode, String scPath) {
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getScCode, scCode)
                .eq(SysConfigEntity::getScPath, scPath);

        SysConfigEntity entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            return false;
        }

        int rows = baseMapper.delete(wrapper);

        if (rows > 0) {
            evictCache(scCode, entity.getModule());
        }

        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean refresh(String scCode, SysConfigUpdateCommand command) {
        return update(scCode, command);
    }

    // ========== 私有方法 ==========

    private List<SysConfigEntity> listByScCode(String scCode) {
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getScCode, scCode)
                .orderByAsc(SysConfigEntity::getScPath);
        return baseMapper.selectList(wrapper);
    }

    private boolean existsByScCode(String scCode) {
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getScCode, scCode);
        return baseMapper.selectCount(wrapper) > 0;
    }

    private boolean deleteByScCodeInternal(String scCode) {
        LambdaQueryWrapper<SysConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigEntity::getScCode, scCode);
        return baseMapper.delete(wrapper) > 0;
    }

    private SysConfigDTO assembleDTO(List<SysConfigEntity> entities) {
        if (entities.isEmpty()) {
            return null;
        }

        SysConfigDTO dto = new SysConfigDTO();
        SysConfigEntity first = entities.get(0);
        dto.setScCode(first.getScCode());
        dto.setModule(first.getModule());
        dto.setScName(first.getScName());
        dto.setDescription(first.getDescription());
        dto.setVersion(first.getVersion());
        dto.setData(assembler.assemble(entities));

        return dto;
    }

    private SysConfigItemDTO toItemDTO(SysConfigEntity entity) {
        SysConfigItemDTO dto = new SysConfigItemDTO();
        dto.setScCode(entity.getScCode());
        dto.setScPath(entity.getScPath());
        dto.setScName(entity.getScName());
        dto.setScType(entity.getScType());
        dto.setScValue(entity.getScValue());
        dto.setDescription(entity.getDescription());
        dto.setVersion(entity.getVersion());
        return dto;
    }

    private void evictCache(String scCode, SysConfigModuleEnum module) {
        redisService.deleteObject(CACHE_KEY_PREFIX + scCode);
        redisService.deleteObject(CACHE_KEY_MODULE + module.name());
    }

    private void writeCache(String key, Object value) {
        try {
            redisService.setCacheObject(key, JacksonUtils.toJson(value), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("写入配置缓存失败: {}", e.getMessage());
        }
    }
}
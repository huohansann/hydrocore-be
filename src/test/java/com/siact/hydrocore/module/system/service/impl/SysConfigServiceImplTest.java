package com.siact.hydrocore.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.siact.hydrocore.common.redis.RedisService;
import com.siact.hydrocore.module.system.dto.SysConfigDTO;
import com.siact.hydrocore.module.system.entity.SysConfigEntity;
import com.siact.hydrocore.module.system.enums.SysConfigModuleEnum;
import com.siact.hydrocore.module.system.enums.SysConfigTypeEnum;
import com.siact.hydrocore.module.system.mapper.SysConfigMapper;
import com.siact.hydrocore.module.system.processor.ConfigAssembler;
import com.siact.hydrocore.module.system.processor.ConfigFlattener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SysConfigServiceImplTest {
    private RedisService redisService;
    private SysConfigMapper mapper;
    private ConfigAssembler assembler;
    private SysConfigServiceImpl service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SysConfigEntity.class);
        redisService = mock(RedisService.class);
        mapper = mock(SysConfigMapper.class);
        assembler = mock(ConfigAssembler.class);
        service = new SysConfigServiceImpl(mock(ConfigFlattener.class), assembler, redisService);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
    }

    @Test
    void getByCodeFallsBackToDatabaseWhenCachedJsonIsInvalid() {
        when(redisService.getJson("sys:config:site", SysConfigDTO.class))
                .thenThrow(new IllegalArgumentException("bad cache"));
        when(mapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(entity("site")));

        LinkedHashMap<String, Object> assembled = new LinkedHashMap<>();
        assembled.put("name", "demo");
        when(assembler.assemble(any())).thenReturn(assembled);

        SysConfigDTO dto = service.getByCode("site");

        assertThat(dto).isNotNull();
        assertThat(dto.getScCode()).isEqualTo("site");
        assertThat(dto.getData()).isEqualTo(assembled);
        verify(redisService).setJson(eq("sys:config:site"), any(SysConfigDTO.class), eq(24L), eq(java.util.concurrent.TimeUnit.HOURS));
    }

    private SysConfigEntity entity(String scCode) {
        SysConfigEntity entity = new SysConfigEntity();
        entity.setScCode(scCode);
        entity.setModule(SysConfigModuleEnum.SYSTEM);
        entity.setScPath("name");
        entity.setScName("Site");
        entity.setScType(SysConfigTypeEnum.STRING);
        entity.setScValue("demo");
        entity.setDescription("demo");
        entity.setVersion(1);
        return entity;
    }
}

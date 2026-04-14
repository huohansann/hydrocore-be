# SysConfig Processor 从 FastJSON 迁移到 Jackson（Map/List）

## 背景

`SysConfig` 模块的 `processor` 包（`ConfigAssembler`、`ConfigFlattener`）当前使用 FastJSON 的 `JSONObject`/`JSONArray` 来组装配置数据。项目其余部分已统一使用 Jackson，需要将 processor 包迁移到标准 Java 集合（`LinkedHashMap`/`ArrayList`），彻底移除 FastJSON 依赖。

## 目标

1. `ConfigAssembler` 内部全部改为 `LinkedHashMap<String, Object>` 和 `ArrayList<Object>`
2. `ConfigFlattener` 删除 FastJSON 兼容分支
3. `SysConfigDTO.data` 运行时为 `Map` 或 `List`，不再为 `JSONObject`/`JSONArray`
4. `SysConfigService` 接口删除无用 FastJSON import

## 设计

### ConfigAssembler 改造

- `assemble()` 返回类型保持 `Object`，实际为 `LinkedHashMap` 或 `ArrayList`
- 空结果返回 `new LinkedHashMap<>()`
- 删除 `stripTypeInfo()` 方法（`@type` 是 FastJSON 特有问题）
- `assembleObject()` → 返回 `Map<String, Object>`，内部使用 `LinkedHashMap`
- `assembleArray()` → 返回 `List<Object>`，内部使用 `ArrayList`
- `setPathValue()` → 参数从 `JSONObject` 改为 `Map<String, Object>`
- `setArrayPathValue()` → 参数从 `JSONArray` 改为 `List<Object>`
- `getOrCreateArray()` → 返回 `List<Object>`
- `getOrCreateObject()` → 返回 `Map<String, Object>`
- `ensureArrayElement()` → 返回 `Map<String, Object>`

### ConfigFlattener 改造

- 删除 `JSONArray` 和 `JSONObject` 的 instanceof 分支（第 67-82 行）
- 只保留 `Map` 和 `List` 分支

### SysConfigService 接口

- 删除 `import com.alibaba.fastjson2.JSONObject`

### SysConfigDTO

- `data` 字段类型保持 `Object`，Swagger 注释更新为"配置数据（Map 或 List）"

### 不需要改动的文件

- `SysConfigServiceImpl` — 已使用 Jackson 做缓存序列化
- `SysConfigCreateCommand` / `SysConfigUpdateCommand` — `data` 字段为 `Object`，Spring MVC 用 Jackson 反序列化为 `LinkedHashMap`/`ArrayList`，自然匹配

## 影响范围

- `src/main/java/com/siact/module/system/processor/ConfigAssembler.java`
- `src/main/java/com/siact/module/system/processor/ConfigFlattener.java`
- `src/main/java/com/siact/module/system/service/SysConfigService.java`
- `src/main/java/com/siact/module/system/dto/SysConfigDTO.java`
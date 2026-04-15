# SysConfig 空数组/空对象处理设计

## 问题

`ConfigFlattener` 递归展开 JSON 时只为叶子节点（非 Map、非 List 的值）生成 Entity 记录。当 JSON 中包含空数组 `[]` 或空对象 `{}` 时，它们没有叶子节点，导致：

- 嵌套中的空值（如 `{"items": []}`）→ `items` 路径丢失 → 还原时结构不完整
- 根节点为空（`{}` 或 `[]`）→ 扁平化结果为空 → 无法保存

## 需求

保存后读取时，空数组/空对象应被原样保留。例如 `{"items": []}` 保存后读取仍返回 `{"items": []}`。

## 方案

### 1. 扩展 `SysConfigTypeEnum`

新增两个枚举值：

| 枚举值 | 含义 | scValue 存储格式 |
|--------|------|-----------------|
| `OBJECT` | 空对象 `{}` | `"{}"` |
| `ARRAY` | 空数组 `[]` | `"[]"` |

### 2. 修改 `ConfigFlattener`

**`doFlatten` 方法**：遍历 Map/List 时，如果子节点是空 Map 或空 List，将其作为叶子节点生成一行 entity，不再递归跳过。

- `map.isEmpty()` → 生成 `scType=OBJECT, scValue="{}"` 的 entity
- `list.isEmpty()` → 生成 `scType=ARRAY, scValue="[]"` 的 entity

**`determineType` 方法**：增加空 Map → `OBJECT`、空 List → `ARRAY` 的判断。

### 3. 修改 `ConfigAssembler`

**`parseValue` 方法**：增加对新类型的解析：

- `OBJECT` → 返回空 `LinkedHashMap`
- `ARRAY` → 返回空 `ArrayList`

### 4. Service 层

`SysConfigServiceImpl.create` 中现有的扁平化结果为空时的占位记录逻辑保留作为兜底，实际不会被触发。

## 影响范围

| 文件 | 改动 |
|------|------|
| `SysConfigTypeEnum` | 新增 `OBJECT`、`ARRAY` 枚举值 |
| `ConfigFlattener` | `doFlatten` 和 `determineType` 增加空值处理 |
| `ConfigAssembler` | `parseValue` 增加新类型解析 |

不涉及数据库表结构变更，不影响现有数据。
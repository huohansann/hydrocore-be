# 设备点位维护功能设计

## 背景

设备点位的现场编码、TAOS编码、孪生长码等映射关系此前未在系统中维护。新增 `device_mapping` 表及配套的增删改查、导入导出功能，使这些数据可以被系统化管理。

## 数据模型

### 表结构（MySQL）

```sql
create table if not exists device_mapping
(
    id          bigint              not null primary key comment '主键',
    point_name  varchar(255) unique not null comment '现场点位名称',
    item_id     varchar(100) unique not null comment '点位ID(TAOS_DB编码)',
    prop_code   varchar(255) unique not null comment '属性编码(孪生长码)',
    prop_name   varchar(255)        not null comment '属性名称',
    device_code varchar(255)        not null comment '设备编码',
    device_name varchar(255)        not null comment '设备名称',
    create_time timestamp default current_timestamp comment '创建时间',
    update_time timestamp default current_timestamp on update current_timestamp comment '更新时间',
    remark      text comment '备注'
);
```

- 主键使用雪花算法（`IdType.ASSIGN_ID`）
- 不继承 BaseEntity，时间字段由数据库自动管理
- 不使用逻辑删除

### Entity

`DeviceMappingEntity` — 严格映射上述表结构，添加 Knife4j `@Schema` 注解。

### Query

`DeviceMappingQuery` — 继承 `PageQuery`，支持多条件筛选：

| 字段 | 匹配方式 |
|------|----------|
| pointName | LIKE 模糊 |
| itemId | 精确 |
| propCode | 精确 |
| propName | LIKE 模糊 |
| deviceCode | 精确 |
| deviceName | LIKE 模糊 |

### Command

`DeviceMappingCommand` — 新增/修改共用，所有业务字段 `@NotBlank` 校验。

### VO

`DeviceMappingVO` — 返回全部字段（含 createTime、updateTime）。

## API 接口

Controller 路径：`/device/mapping`，添加 `@Tag`、`@Operation` 注解。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询（多条件筛选） |
| GET | `/{id}` | 查询单条 |
| POST | `/add` | 新增 |
| PUT | `/update` | 修改 |
| DELETE | `/{id}` | 单条删除 |
| DELETE | `/batch` | 批量删除（body 传 id 列表） |
| DELETE | `/clear` | 全部清空 |
| POST | `/import` | 导入（上传文件） |
| GET | `/export` | 导出（参数 format: excel/csv/json） |

## 导入功能

- 根据文件扩展名自动识别格式（.xlsx/.xls → Excel，.csv → CSV，.json → JSON）
- 冲突处理：按 `item_id` 和 `prop_code` 判断，已存在则覆盖更新，不存在则新增
- 单行失败不中断整批，记录到错误列表
- 返回 `DeviceImportResult`：

```java
@Data
public class DeviceImportResult {
    private int successCount;      // 新增成功条数
    private int updateCount;       // 覆盖更新条数
    private int failCount;         // 失败条数
    private List<ImportError> errors;
}

@Data
public class ImportError {
    private int row;               // 行号
    private String pointName;      // 点位名称（如有）
    private String itemId;         // 点位ID（如有）
    private String reason;         // 失败原因
}
```

## 导出功能

- 导出当前筛选条件下的数据（与查询条件一致，不传条件则导出全部）
- Excel：使用 EasyPOI 动态表头方式（与现有 DataServiceImpl 一致）
- CSV：直接生成文本流
- JSON：使用 `JacksonUtils` 序列化

## 代码分层（DDD 风格）

参照 system 模块（SysRole）的分层模式：

```
module/device/
├── controller/
│   └── DeviceMappingController.java
├── service/
│   ├── DeviceMappingService.java
│   └── impl/
│       └── DeviceMappingServiceImpl.java
├── repository/
│   ├── DeviceMappingRepository.java
│   └── impl/
│       └── DeviceMappingRepositoryImpl.java
├── mapper/
│   └── DeviceMappingMapper.java
├── entity/
│   └── DeviceMappingEntity.java
├── convert/
│   └── DeviceMappingConvert.java
├── query/
│   └── DeviceMappingQuery.java
├── command/
│   └── DeviceMappingCommand.java
└── vo/
    ├── DeviceMappingVO.java
    └── DeviceImportResult.java
```

### 职责划分

- **Controller**：接收请求，参数校验，返回 `PageVO<DeviceMappingVO>` 或 `Boolean`（由 ResponseBodyAdvice 自动包装）
- **Service 接口**：继承 `IService<DeviceMappingEntity>`，定义业务方法
- **ServiceImpl**：继承 `ServiceImpl<DeviceMappingMapper, DeviceMappingEntity>`，实现业务逻辑，导入导出编排
- **Repository**：封装多条件查询组装、导入时的冲突判断与 upsert
- **Mapper**：继承 `BaseMapper<DeviceMappingEntity>`
- **Convert**：MapStruct 转换器（Command → Entity、Entity → VO）

## 技术选型

| 项目 | 选择 |
|------|------|
| ORM | MyBatis-Plus 3.4.3.1 |
| ID 策略 | 雪花算法（IdType.ASSIGN_ID） |
| 对象转换 | MapStruct 1.5.3 |
| 分页 | MyBatis-Plus 分页（PageQuery + PageVO） |
| Excel 导入导出 | EasyPOI 4.4.0（动态表头） |
| JSON 序列化 | JacksonUtils（系统封装） |
| API 文档 | Knife4j 4.3.0 |
| DDL 管理 | 追加到 db/schema.sql |
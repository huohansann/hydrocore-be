# 系统选项配置接口文档

## 概述

系统选项配置功能用于管理界面上下拉框、多选框等选项式组件的配置数据。支持 JSON 对象或数组的扁平化存储，通过路径索引机制实现灵活的配置管理。

**基础路径**: `/sys-config`

---

## 数据模型

### 模块枚举 (module)

| 值 | 说明 |
|---|---|
| `SYSTEM` | 系统配置 |
| `CONTROL` | 控制模块配置 |
| `FORECAST` | 预测模块配置 |

### 数据类型枚举 (scType)

| 值 | 说明 |
|---|---|
| `STRING` | 字符串 |
| `INTEGER` | 整数 |
| `FLOAT` | 单精度浮点 |
| `DOUBLE` | 双精度浮点 |
| `DECIMAL` | 高精度数值 |
| `BOOLEAN` | 布尔值 |
| `TIMESTAMP` | 时间戳 |

### SysConfigDTO - 配置对象

| 字段 | 类型 | 说明 |
|---|---|---|
| scCode | String | 配置编码 |
| module | String | 模块 (枚举值) |
| scName | String | 配置名称 |
| description | String | 配置说明 |
| version | Integer | 乐观锁版本号 |
| data | Object | 配置数据 (JSON 对象或数组) |

### SysConfigItemDTO - 配置项对象

| 字段 | 类型 | 说明 |
|---|---|---|
| scCode | String | 配置编码 |
| scPath | String | 配置路径 |
| scName | String | 配置名称 |
| scType | String | 配置类型 (枚举值) |
| scValue | String | 配置值 |
| description | String | 配置说明 |
| version | Integer | 乐观锁版本号 |

---

## 接口列表

### 1. 获取配置

**GET** `/sys-config/{scCode}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| scCode | String | 是 | 配置编码 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "scCode": "temperature_range",
    "module": "SYSTEM",
    "scName": "温度范围配置",
    "description": "系统温度范围设置",
    "version": 1,
    "data": {
      "min": 100,
      "max": 500,
      "unit": "摄氏度"
    }
  }
}
```

**根级数组示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "scCode": "temperament_predict_menus",
    "module": "FORECAST",
    "scName": "温度预测点位菜单",
    "description": "温度预测点位菜单列表",
    "version": 1,
    "data": [
      {
        "dataCode": "PGY02037_SYL01001_ST00000000_U00000000_BJYL01WD001001_MPW132001",
        "name": "TE213"
      },
      {
        "dataCode": "PGY02037_SYL01001_ST00000000_U00000000_BJYL01WD001001_MPWD62001",
        "name": "TE206"
      }
    ]
  }
}
```

**错误响应**:

| 状态码 | 说明 |
|---|---|
| 500 | 配置不存在 |

---

### 2. 创建配置

**POST** `/sys-config`

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| module | String | 是 | 模块枚举值 |
| scCode | String | 是 | 配置编码 (全局唯一) |
| scName | String | 是 | 配置名称 |
| description | String | 是 | 配置说明 |
| data | Object | 是 | 配置数据 (JSON 对象或数组) |

**请求示例 - 对象类型**:

```json
{
  "module": "SYSTEM",
  "scCode": "temperature_range",
  "scName": "温度范围配置",
  "description": "系统温度范围设置",
  "data": {
    "min": 100,
    "max": 500,
    "unit": "摄氏度"
  }
}
```

**请求示例 - 数组类型**:

```json
{
  "module": "FORECAST",
  "scCode": "temperament_predict_menus",
  "scName": "温度预测点位菜单",
  "description": "温度预测点位菜单列表",
  "data": [
    {
      "dataCode": "PGY02037_SYL01001_ST00000000_U00000000_BJYL01WD001001_MPW132001",
      "name": "TE213"
    },
    {
      "dataCode": "PGY02037_SYL01001_ST00000000_U00000000_BJYL01WD001001_MPWD62001",
      "name": "TE206"
    }
  ]
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**错误响应**:

| 状态码 | 说明 |
|---|---|
| 500 | 配置编码已存在 |

---

### 3. 更新配置

**PUT** `/sys-config/{scCode}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| scCode | String | 是 | 配置编码 |

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| scName | String | 否 | 配置名称 |
| description | String | 否 | 配置说明 |
| data | Object | 是 | 配置数据 (JSON 对象或数组) |
| version | Integer | 是 | 当前版本号 (乐观锁) |

**请求示例**:

```json
{
  "scName": "温度范围配置(更新)",
  "description": "更新后的描述",
  "data": {
    "min": 50,
    "max": 600,
    "unit": "摄氏度"
  },
  "version": 1
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**错误响应**:

| 状态码 | 说明 |
|---|---|
| 500 | 配置不存在 |
| 500 | 配置已被修改，请刷新后重试 |

---

### 4. 删除配置

**DELETE** `/sys-config/{scCode}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| scCode | String | 是 | 配置编码 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

---

### 5. 按模块查询配置列表

**GET** `/sys-config/module/{module}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| module | String | 是 | 模块枚举值 |

**请求示例**:

```
GET /sys-config/module/SYSTEM
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "scCode": "temperature_range",
      "module": "SYSTEM",
      "scName": "温度范围配置",
      "description": "系统温度范围设置",
      "version": 1,
      "data": {
        "min": 100,
        "max": 500
      }
    },
    {
      "scCode": "pressure_range",
      "module": "SYSTEM",
      "scName": "压力范围配置",
      "description": "系统压力范围设置",
      "version": 2,
      "data": {
        "min": 0,
        "max": 100
      }
    }
  ]
}
```

---

### 6. 按编码列表批量查询

**POST** `/sys-config/batch`

**请求体**: 字符串数组 (配置编码列表)

**请求示例**:

```json
["temperature_range", "pressure_range"]
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "temperature_range": {
      "scCode": "temperature_range",
      "module": "SYSTEM",
      "scName": "温度范围配置",
      "description": "系统温度范围设置",
      "version": 1,
      "data": {
        "min": 100,
        "max": 500
      }
    },
    "pressure_range": {
      "scCode": "pressure_range",
      "module": "SYSTEM",
      "scName": "压力范围配置",
      "description": "系统压力范围设置",
      "version": 2,
      "data": {
        "min": 0,
        "max": 100
      }
    }
  }
}
```

---

### 7. 获取单个配置项

**GET** `/sys-config/{scCode}/path/{scPath}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| scCode | String | 是 | 配置编码 |
| scPath | String | 是 | 配置路径 |

**路径格式说明**:

- 对象属性: `propertyName`
- 嵌套对象: `parent.child`
- 数组元素: `parent.[index]`
- 根级数组: `[index]`

**请求示例**:

```
GET /sys-config/temperature_range/path/min
GET /sys-config/device_config/path/devices.[0].name
GET /sys-config/menu_list/path/[0].dataCode
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "scCode": "temperature_range",
    "scPath": "min",
    "scName": "温度范围配置",
    "scType": "INTEGER",
    "scValue": "100",
    "description": "系统温度范围设置",
    "version": 1
  }
}
```

**错误响应**:

| 状态码 | 说明 |
|---|---|
| 500 | 配置项不存在 |

---

### 8. 更新单个配置项

**PATCH** `/sys-config/{scCode}/path/{scPath}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| scCode | String | 是 | 配置编码 |
| scPath | String | 是 | 配置路径 |

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| value | String | 是 | 新的配置值 |
| version | Integer | 是 | 当前版本号 (乐观锁) |

**请求示例**:

```
PATCH /sys-config/temperature_range/path/min?value=50&version=1
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**错误响应**:

| 状态码 | 说明 |
|---|---|
| 500 | 配置项不存在 |
| 500 | 配置已被修改，请刷新后重试 |

---

### 9. 删除单个配置项

**DELETE** `/sys-config/{scCode}/path/{scPath}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| scCode | String | 是 | 配置编码 |
| scPath | String | 是 | 配置路径 |

**请求示例**:

```
DELETE /sys-config/temperature_range/path/unit
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

---

### 10. 全量刷新配置

**POST** `/sys-config/{scCode}/refresh`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| scCode | String | 是 | 配置编码 |

**请求体**: 与更新配置相同

**请求示例**:

```json
{
  "data": {
    "min": 0,
    "max": 1000,
    "step": 10
  },
  "version": 1
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

---

## 使用建议

### 前端获取配置

1. 使用 `GET /sys-config/{scCode}` 获取完整配置
2. `data` 字段可直接用于渲染界面组件

### 前端保存配置

1. 先获取配置获取当前 `version`
2. 修改 `data` 后调用更新接口，携带正确的 `version`
3. 若提示版本冲突，重新获取配置后重试

### 配置编码命名规范

建议使用有意义的命名，如:
- `temperature_range` - 温度范围
- `device_step_config` - 设备步长配置
- `menu_list` - 菜单列表

### 数据结构建议

- 配置对象深度建议不超过 5 层
- 数组元素建议不超过 1000 个
- 使用数组时注意顺序与路径对应
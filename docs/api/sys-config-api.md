# System Configuration API

Base path: `/sys-config`

The clean HydroCore baseline exposes generic system configuration only. Domain-specific water-treatment modules should add their own configuration through a separate OpenSpec/Comet change.

## Module Enum

| Value | Description |
|---|---|
| `SYSTEM` | Generic system configuration |
| `INTEGRATION` | External integration placeholder configuration |

## Type Enum

| Value | Description |
|---|---|
| `STRING` | String |
| `INTEGER` | Integer |
| `FLOAT` | Float |
| `DOUBLE` | Double |
| `DECIMAL` | Decimal |
| `BOOLEAN` | Boolean |
| `TIMESTAMP` | Timestamp |

## DTO Shape

```json
{
  "scCode": "system_display_name",
  "module": "SYSTEM",
  "scName": "System display name",
  "description": "HydroCore baseline display name",
  "version": 1,
  "data": {
    "value": "HydroCore"
  }
}
```

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/sys-config/{scCode}` | Get one configuration object |
| `POST` | `/sys-config` | Create a configuration object |
| `PUT` | `/sys-config/{scCode}` | Update a configuration object |
| `DELETE` | `/sys-config/{scCode}` | Delete a configuration object |
| `GET` | `/sys-config/module/{module}` | List configuration objects by module |
| `POST` | `/sys-config/batch` | Get configuration objects by code list |
| `GET` | `/sys-config/{scCode}/path/{scPath}` | Get one flattened configuration item |
| `PATCH` | `/sys-config/{scCode}/path/{scPath}` | Update one flattened configuration item |
| `DELETE` | `/sys-config/{scCode}/path/{scPath}` | Delete one flattened configuration item |
| `POST` | `/sys-config/{scCode}/refresh` | Replace all data for one configuration object |

## Examples

Create a generic system option:

```json
{
  "module": "SYSTEM",
  "scCode": "system_display_name",
  "scName": "System display name",
  "description": "HydroCore baseline display name",
  "data": {
    "value": "HydroCore"
  }
}
```

Create an integration placeholder:

```json
{
  "module": "INTEGRATION",
  "scCode": "integration_endpoints",
  "scName": "Integration endpoints",
  "description": "Placeholder endpoints supplied by deployment or later modules",
  "data": []
}
```

Batch query:

```json
["system_display_name", "integration_endpoints"]
```

Path query examples:

```text
GET /sys-config/system_display_name/path/value
GET /sys-config/integration_endpoints/path/[0].name
```

## Naming Guidance

Use neutral configuration names for the baseline, for example:

- `system_display_name`
- `system_default_locale`
- `integration_endpoints`
- `integration_datacodes`

Do not add process-specific configuration names, control algorithms, prediction examples, plant data, or production endpoint values to the baseline.

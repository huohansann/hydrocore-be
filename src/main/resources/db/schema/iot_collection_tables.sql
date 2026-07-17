-- IoT 协议连接 + 采集点（OPC UA / Modbus TCP / Modbus RTU）
-- Spec: docs/superpowers/specs/2026-05-18-iot-protocol-collection-design.md

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `iot_protocol_connection` (
  `id` bigint NOT NULL,
  `name` varchar(128) NOT NULL,
  `protocol` varchar(16) NOT NULL COMMENT 'OPC_UA|MODBUS_TCP|MODBUS_RTU',
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `endpoint_url` varchar(512) DEFAULT NULL,
  `security_policy` varchar(32) DEFAULT NULL,
  `username` varchar(128) DEFAULT NULL,
  `password` varchar(256) DEFAULT NULL,
  `host` varchar(64) DEFAULT NULL,
  `port` int DEFAULT NULL,
  `serial_port` varchar(64) DEFAULT NULL COMMENT 'RTU: COM3 or /dev/ttyUSB0',
  `baud_rate` int DEFAULT NULL COMMENT 'RTU: default 9600',
  `data_bits` int DEFAULT NULL COMMENT 'RTU: default 8',
  `parity` varchar(8) DEFAULT NULL COMMENT 'RTU: NONE|EVEN|ODD',
  `stop_bits` int DEFAULT NULL COMMENT 'RTU: 1 or 2',
  `connect_timeout_ms` int NOT NULL DEFAULT 3000,
  `extra_config` json DEFAULT NULL,
  `last_test_at` datetime(3) DEFAULT NULL,
  `last_test_ok` tinyint(1) DEFAULT NULL,
  `last_test_msg` varchar(512) DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_protocol_enabled` (`protocol`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT协议连接';

CREATE TABLE IF NOT EXISTS `iot_collection_point` (
  `id` bigint NOT NULL,
  `point_code` varchar(64) NOT NULL,
  `name` varchar(128) NOT NULL,
  `point_type` varchar(64) DEFAULT NULL,
  `unit` varchar(32) DEFAULT NULL,
  `connection_id` bigint NOT NULL,
  `collect_interval_ms` int NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `alarm_config` json DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_point_code` (`point_code`),
  KEY `idx_connection_enabled` (`connection_id`, `enabled`),
  KEY `idx_enabled_created` (`enabled`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT采集点';

CREATE TABLE IF NOT EXISTS `iot_point_opc_tag` (
  `point_id` bigint NOT NULL,
  `node_id` varchar(512) NOT NULL,
  PRIMARY KEY (`point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `iot_point_modbus_tag` (
  `point_id` bigint NOT NULL,
  `slave_id` int NOT NULL,
  `function_code` tinyint NOT NULL,
  `start_address` int NOT NULL,
  `quantity` int NOT NULL,
  `data_type` varchar(32) NOT NULL,
  `byte_order` varchar(16) DEFAULT 'BIG_ENDIAN',
  PRIMARY KEY (`point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Modbus TCP/RTU 共用测点';

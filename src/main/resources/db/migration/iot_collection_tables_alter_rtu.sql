-- 增量：为已存在的 iot_protocol_connection 增加 Modbus RTU 串口字段
-- 全新安装请直接用 iot_collection_tables.sql（Plan Task 1）

ALTER TABLE `iot_protocol_connection`
  ADD COLUMN IF NOT EXISTS `serial_port` varchar(64) DEFAULT NULL COMMENT 'RTU: COM3 or /dev/ttyUSB0' AFTER `port`,
  ADD COLUMN IF NOT EXISTS `baud_rate` int DEFAULT NULL COMMENT 'RTU: default 9600' AFTER `serial_port`,
  ADD COLUMN IF NOT EXISTS `data_bits` int DEFAULT NULL COMMENT 'RTU: default 8' AFTER `baud_rate`,
  ADD COLUMN IF NOT EXISTS `parity` varchar(8) DEFAULT NULL COMMENT 'RTU: NONE|EVEN|ODD' AFTER `data_bits`,
  ADD COLUMN IF NOT EXISTS `stop_bits` int DEFAULT NULL COMMENT 'RTU: 1 or 2' AFTER `parity`;

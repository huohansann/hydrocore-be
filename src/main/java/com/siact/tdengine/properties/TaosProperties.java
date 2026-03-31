package com.siact.tdengine.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TDengine 数据库连接配置属性
 * 从 Nacos 配置中心读取 spring.datasource.taos 配置
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource.taos")
public class TaosProperties {

    /**
     * JDBC URL，REST 模式格式: jdbc:TAOS-RS://host:6041/database
     */
    private String url;

    /**
     * 用户名，默认 root
     */
    private String username = "root";

    /**
     * 密码，默认 taosdata
     */
    private String password = "taosdata";

    /**
     * 连接池最大连接数
     */
    private int maximumPoolSize = 10;

    /**
     * 连接超时时间（毫秒）
     */
    private long connectionTimeout = 5000;
}
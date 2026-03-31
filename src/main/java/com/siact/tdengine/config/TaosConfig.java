package com.siact.tdengine.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

/**
 * TDengine 数据源配置
 * 管理 TDengine 连接池，但不注册为 Spring DataSource Bean
 * 避免干扰 MyBatis 的主数据源选择
 */
@Configuration
public class TaosConfig {

    @Value("${spring.datasource.taos.url:}")
    private String url;

    @Value("${spring.datasource.taos.username:root}")
    private String username;

    @Value("${spring.datasource.taos.password:taosdata}")
    private String password;

    @Value("${spring.datasource.taos.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Value("${spring.datasource.taos.connection-timeout:5000}")
    private long connectionTimeout;

    @Getter
    private volatile HikariDataSource taosDataSource;

    /**
     * 获取 TDengine DataSource（懒加载，非 Spring Bean）
     */
    public synchronized HikariDataSource getDataSource() {
        if (taosDataSource == null && url != null && !url.isEmpty()) {
            taosDataSource = new HikariDataSource();
            taosDataSource.setJdbcUrl(url);
            taosDataSource.setUsername(username);
            taosDataSource.setPassword(password);
            taosDataSource.setMaximumPoolSize(maximumPoolSize);
            taosDataSource.setConnectionTimeout(connectionTimeout);
            taosDataSource.setPoolName("TaosHikariPool");
        }
        return taosDataSource;
    }

    @PreDestroy
    public void destroy() {
        if (taosDataSource != null && !taosDataSource.isClosed()) {
            taosDataSource.close();
        }
    }
}
package com.siact;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 新特能源课题6
 *
 * @author example
 */
@EnableScheduling
@MapperScan(basePackages = {"com.siact.module.*.mapper", "com.siact.common.*"})
@EnableFeignClients
@SpringBootApplication
public class KilnApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(KilnApplication.class, args);
    }
} 
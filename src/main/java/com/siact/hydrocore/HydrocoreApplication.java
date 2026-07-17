package com.siact.hydrocore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan(basePackages = {"com.siact.hydrocore.module.*.mapper", "com.siact.hydrocore.common"})
@EnableFeignClients
@SpringBootApplication
public class HydrocoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(HydrocoreApplication.class, args);
    }
}

package com.siact.common.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 全局常量配置类型
 *
 * @author wr
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = "constant")
public class GlobalVariableService {

}

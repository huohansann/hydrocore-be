package com.siact.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;


import java.io.IOException;
import java.math.BigDecimal;

/**
 * 自定义BigDecimal序列化
 *
 * @author wr
 */
@JsonComponent
public class BigDecimalTrimmingConverter extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            // 去除尾部多余的 0，并避免科学计数法
            String formatted = value.stripTrailingZeros().toPlainString();
            gen.writeString(formatted);
        }
    }
}

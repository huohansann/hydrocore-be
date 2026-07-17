package com.siact.hydrocore.common.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {
    @Test
    void successResponseContainsCanonicalFields() {
        ApiResponse<String> response = ApiResponse.success("payload", "操作成功", "trace-1");

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("操作成功");
        assertThat(response.getData()).isEqualTo("payload");
        assertThat(response.getTraceId()).isEqualTo("trace-1");
    }

    @Test
    void failureResponseContainsCanonicalFields() {
        ApiResponse<Void> response = ApiResponse.fail(400, "名称不能为空", "trace-2");

        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo("名称不能为空");
        assertThat(response.getData()).isNull();
        assertThat(response.getTraceId()).isEqualTo("trace-2");
    }
}

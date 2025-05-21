package com.siact.common.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpUtil {
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)  // 连接超时
            .readTimeout(30, TimeUnit.SECONDS)    // 读取超时
            .build();

    public static String postJson(String url, String json) {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return ObjectUtils.isEmpty(response.body()) ? "" : response.body().string();
        } catch (IOException e) {
            log.error("getJson error:{}", e.getMessage(), e);
            return JSON.toJSONString("");
        }
    }

    public static String getJson(String url, HashMap<String, String> params) {
        Request request = new Request.Builder()
                .url(url+handleGetParams(params))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return ObjectUtils.isEmpty(response.body()) ? "" : response.body().string();
        } catch (IOException e) {
            log.error("getJson error:{}", e.getMessage(), e);
            return JSON.toJSONString("");
        }
    }

    /**
     * 处理Get请求的Url传参
     * @param params
     * @return
     */
    private static String handleGetParams(HashMap<String, String> params) {
        if (ObjectUtils.isEmpty(params)) {
            return "";
        }

        StringBuilder urlBuilder = new StringBuilder("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("&");
        }
        // 删除最后一个多余的"&"
        urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        return urlBuilder.toString();
    }
}

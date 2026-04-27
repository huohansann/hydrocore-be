package com.siact.module.algorithm.services;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public interface PythonAlgorithmService {

    /**
     * 执行远程 Python 脚本并返回解析结果
     *
     * @param scriptName Python 脚本文件名（相对于 scriptBasePath）
     * @param params     传递给脚本的参数（通过 sys.argv 传入）
     * @param returnType 期望的返回类型
     * @param <T>        返回类型泛型
     * @return 解析后的结果对象
     */
    <T> T execute(String scriptName, Map<String, String> params, Class<T> returnType);

    /**
     * 执行远程 Python 脚本并返回解析结果（支持泛型类型如 List&lt;T&gt;）
     *
     * @param scriptName Python 脚本文件名（相对于 scriptBasePath）
     * @param params     传递给脚本的参数（通过 sys.argv 传入）
     * @param typeRef    Jackson TypeReference，用于泛型类型
     * @param <T>        返回类型泛型
     * @return 解析后的结果对象
     */
    <T> T execute(String scriptName, Map<String, String> params, TypeReference<T> typeRef);
}

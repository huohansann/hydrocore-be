package com.siact.common.utils;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 生成JWT令牌
     * 使用JJWT库构建符合JWT标准的令牌，包含主题声明、过期时间声明，并通过指定算法进行签名
     *
     * @param username 令牌主题标识，通常表示用户唯一标识
     * @param userinfo
     * @return 经过Base64Url编码的完整JWT字符串，包含头部、载荷和签名三部分
     */
    public String generateToken(String username, Object userinfo) {
        String token = Jwts.builder()
                /* 设置标准声明：主题(subject)和过期时间(expiration) */
                .setSubject(username)
                /* 添加自定义声明：角色列表 */
                .claim("roles", userinfo)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                /* 使用HMAC-SHA256算法和预设密钥进行签名 */
                .signWith(SignatureAlgorithm.HS256, secretKey)
                /* 最终序列化为紧凑的URL安全字符串 */
                .compact();

                // 存储到Redis（用户名为key，24小时有效期）
        redisTemplate.opsForValue().set(
                username,
                token,
                expiration,
                TimeUnit.MILLISECONDS
        );
        return token;
    }


    /**
     * 从JWT令牌中提取用户名（即Subject字段）
     *
     * @param token 需要解析的JWT令牌字符串，格式应符合RFC 7519标准
     * @return String 解析后获得的用户主体信息，通常是用户在身份验证时设置的用户名
     * <p>
     * 函数通过以下步骤处理令牌：
     * 1. 使用预定义的SECRET_KEY初始化JWT解析器
     * 2. 验证签名并解析令牌内容
     * 3. 从JWT的claims体中提取subject字段
     * 注意：当令牌无效/过期/签名不匹配时会抛出异常
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                // 设置用于验证令牌签名的密钥
                .setSigningKey(secretKey)
                // 执行签名验证并解析令牌，返回完整的JWS对象
                .parseClaimsJws(token)
                // 获取JWT的payload部分（claims集合）
                .getBody()
                // 提取标准subject声明字段（通常用于存放用户标识）
                .getSubject();
    }

    /**
     * 删除Redis中的JWT令牌
     *
     * @param username 需要删除的令牌对应的用户标识
     */
    public void deleteToken(String username) {
        redisTemplate.delete(username);
    }
}
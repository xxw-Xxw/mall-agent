package com.ruoyi.system.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson2.JSONObject;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Agent 管理接口 JWT 签发服务。
 *
 * 按用户/调用方签发短期 JWT（HS256），并把会话写入 Redis：
 *   key = agent:session:{jti}，value = 会话 JSON，TTL 与 JWT 过期时间一致。
 * Agent 验签后到 Redis 校验会话，删除 key 即可主动撤销令牌。
 */
@Component
public class AgentJwtService
{
    public static final String SESSION_PREFIX = "agent:session:";

    @Value("${mall.agent.jwt-secret:}")
    private String jwtSecret;

    @Value("${mall.agent.jwt-expire-minutes:10}")
    private int expireMinutes;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 签发管理 JWT 并写入 Redis 会话。
     *
     * @param subject  调用方标识（如 mall-backend / mall-admin）
     * @param username 用户名
     * @return JWT 字符串
     */
    public String issueToken(String subject, String username)
    {
        String jti = UUID.randomUUID().toString().replace("-", "");
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireMinutes * 60_000L);

        JSONObject session = new JSONObject();
        session.put("sub", subject);
        session.put("username", username);
        session.put("issueTime", now.getTime());
        stringRedisTemplate.opsForValue().set(SESSION_PREFIX + jti, session.toJSONString(),
                expireMinutes, TimeUnit.MINUTES);

        return Jwts.builder()
                .setId(jti)
                .setSubject(subject)
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }
}

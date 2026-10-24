package com.ruoyi.framework.web.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.model.MemberSession;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 商城会员 token 处理（与 sys_user 管理端 TokenService 隔离）
 *
 * @author ruoyi
 */
@Component
public class MemberTokenService
{
    // 令牌有效期（分钟，与管理端共用 token.expireTime 配置）
    @Value("${token.expireTime}")
    private int expireTime;

    @Autowired
    private RedisCache redisCache;

    /**
     * 创建会员会话（同账号互踢：仅保留最新登录）
     */
    public String createToken(MemberSession session)
    {
        String token = IdUtils.fastUUID();
        session.setToken(token);
        // 互踢：删除该会员旧会话
        String memberTokenKey = CacheConstants.STORE_MEMBER_TOKEN_KEY + session.getMemberId();
        String oldToken = redisCache.getCacheObject(memberTokenKey);
        if (StringUtils.isNotEmpty(oldToken) && !oldToken.equals(token))
        {
            redisCache.deleteObject(getTokenKey(oldToken));
        }
        redisCache.setCacheObject(memberTokenKey, token, expireTime, TimeUnit.MINUTES);
        refreshToken(session);
        return token;
    }

    /**
     * 根据请求携带的 token 获取会员会话
     */
    public MemberSession getMemberSession(String token)
    {
        if (StringUtils.isEmpty(token))
        {
            return null;
        }
        MemberSession session = redisCache.getCacheObject(getTokenKey(token));
        if (session != null && session.getExpireTime() - System.currentTimeMillis() <= 20 * 60 * 1000L)
        {
            refreshToken(session);
        }
        return session;
    }

    /**
     * 删除会员会话（登出）
     */
    public void delToken(String token)
    {
        if (StringUtils.isEmpty(token))
        {
            return;
        }
        MemberSession session = redisCache.getCacheObject(getTokenKey(token));
        redisCache.deleteObject(getTokenKey(token));
        if (session != null)
        {
            String memberTokenKey = CacheConstants.STORE_MEMBER_TOKEN_KEY + session.getMemberId();
            if (token.equals(redisCache.getCacheObject(memberTokenKey)))
            {
                redisCache.deleteObject(memberTokenKey);
            }
        }
    }

    /**
     * 刷新会话有效期
     */
    public void refreshToken(MemberSession session)
    {
        session.setLoginTime(System.currentTimeMillis());
        session.setExpireTime(session.getLoginTime() + expireTime * 60 * 1000L);
        redisCache.setCacheObject(getTokenKey(session.getToken()), session, expireTime, TimeUnit.MINUTES);
        String memberTokenKey = CacheConstants.STORE_MEMBER_TOKEN_KEY + session.getMemberId();
        if (session.getToken().equals(redisCache.getCacheObject(memberTokenKey)))
        {
            redisCache.expire(memberTokenKey, expireTime, TimeUnit.MINUTES);
        }
    }

    private String getTokenKey(String token)
    {
        return CacheConstants.STORE_TOKEN_KEY + token;
    }
}

package com.ruoyi.common.core.domain.model;

import java.io.Serializable;

/**
 * 商城会员会话（存 Redis，与 sys_user 管理端 LoginUser 隔离）
 *
 * @author ruoyi
 */
public class MemberSession implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 会员token（uuid） */
    private String token;

    /** 会员ID */
    private Long memberId;

    /** 登录账号 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 登录时间（毫秒） */
    private Long loginTime;

    /** 过期时间（毫秒） */
    private Long expireTime;

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public Long getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(Long loginTime)
    {
        this.loginTime = loginTime;
    }

    public Long getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Long expireTime)
    {
        this.expireTime = expireTime;
    }
}

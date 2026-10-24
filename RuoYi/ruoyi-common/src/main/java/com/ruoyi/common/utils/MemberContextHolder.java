package com.ruoyi.common.utils;

import com.ruoyi.common.core.domain.model.MemberSession;
import com.ruoyi.common.exception.ServiceException;

/**
 * 商城会员上下文（ThreadLocal，由 StoreAuthInterceptor 填充）
 *
 * @author ruoyi
 */
public class MemberContextHolder
{
    private static final ThreadLocal<MemberSession> MEMBER_HOLDER = new ThreadLocal<>();

    public static void set(MemberSession session)
    {
        MEMBER_HOLDER.set(session);
    }

    public static MemberSession getSession()
    {
        return MEMBER_HOLDER.get();
    }

    /**
     * 获取当前登录会员ID，未登录返回 null（匿名可访问场景）
     */
    public static Long getMemberId()
    {
        MemberSession session = MEMBER_HOLDER.get();
        return session == null ? null : session.getMemberId();
    }

    /**
     * 获取当前登录会员ID，未登录抛异常（需登录场景）
     */
    public static Long requireMemberId()
    {
        MemberSession session = MEMBER_HOLDER.get();
        if (session == null)
        {
            throw new ServiceException("请先登录", 401);
        }
        return session.getMemberId();
    }

    public static void clear()
    {
        MEMBER_HOLDER.remove();
    }
}

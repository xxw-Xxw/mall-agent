package com.ruoyi.framework.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.model.MemberSession;
import com.ruoyi.common.utils.MemberContextHolder;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.MemberTokenService;

/**
 * 商城会员鉴权拦截器：解析会员 token 填充 ThreadLocal，
 * 鉴权由 Controller 层 MemberContextHolder.requireMemberId() 决定（匿名可浏览、需登录接口自然 401）
 *
 * @author ruoyi
 */
@Component
public class StoreAuthInterceptor implements HandlerInterceptor
{
    @Autowired
    private MemberTokenService memberTokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    {
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            token = token.replace(Constants.TOKEN_PREFIX, "");
            MemberSession session = memberTokenService.getMemberSession(token);
            if (session != null)
            {
                MemberContextHolder.set(session);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
    {
        MemberContextHolder.clear();
    }
}

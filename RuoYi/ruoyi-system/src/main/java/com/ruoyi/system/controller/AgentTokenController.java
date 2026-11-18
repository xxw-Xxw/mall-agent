package com.ruoyi.system.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.AgentJwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 管理接口 JWT 签发入口。
 * 仅管理员可调用，为当前登录用户签发短期 JWT（会话存 Redis）。
 */
@RestController
@RequestMapping("/mall/agent")
public class AgentTokenController
{
    @Autowired
    private AgentJwtService agentJwtService;

    /**
     * 为当前登录管理员签发 Agent 管理 JWT。
     * 前端/运维拿到后，以 Authorization: Bearer <jwt> 调用 Agent 管理接口。
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/token")
    public AjaxResult issueToken()
    {
        String username = SecurityUtils.getUsername();
        String token = agentJwtService.issueToken("mall-admin", username);
        return AjaxResult.success().put("token", token);
    }
}

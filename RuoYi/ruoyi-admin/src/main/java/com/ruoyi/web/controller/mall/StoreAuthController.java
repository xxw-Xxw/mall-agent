package com.ruoyi.web.controller.mall;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.MemberSession;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.MemberContextHolder;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.MemberTokenService;
import com.ruoyi.system.domain.MallMember;
import com.ruoyi.system.mapper.MallMemberMapper;

/**
 * 商城会员认证（与管理后台 sys_user 账号体系分离）
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/store/auth")
public class StoreAuthController extends BaseController
{
    @Autowired
    private MallMemberMapper mallMemberMapper;

    @Autowired
    private MemberTokenService memberTokenService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 会员注册（成功后自动登录）
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody Map<String, String> body)
    {
        String username = StringUtils.trim(body.get("username"));
        String password = body.get("password");
        String confirm = body.get("confirm");
        String nickname = StringUtils.trim(body.get("nickname"));
        if (StringUtils.isEmpty(username) || username.length() < 2 || username.length() > 30)
        {
            return error("账号长度须为 2-30 个字符");
        }
        if (StringUtils.isEmpty(password) || password.length() < 6 || password.length() > 30)
        {
            return error("密码长度须为 6-30 个字符");
        }
        if (!password.equals(confirm))
        {
            return error("两次输入的密码不一致");
        }
        if (mallMemberMapper.selectMemberByUsername(username) != null)
        {
            return error("该账号已被注册");
        }
        MallMember member = new MallMember();
        member.setUsername(username);
        member.setPassword(bCryptPasswordEncoder.encode(password));
        member.setNickname(StringUtils.isEmpty(nickname) ? username : nickname);
        member.setStatus("0");
        mallMemberMapper.insertMember(member);
        return loginResult(buildSession(member));
    }

    /**
     * 会员登录
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, String> body)
    {
        String username = StringUtils.trim(body.get("username"));
        String password = body.get("password");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            return error("请输入账号和密码");
        }
        MallMember member = mallMemberMapper.selectMemberByUsername(username);
        if (member == null || !bCryptPasswordEncoder.matches(password, member.getPassword()))
        {
            return error("账号或密码错误");
        }
        if ("1".equals(member.getStatus()))
        {
            return error("账号已停用");
        }
        return loginResult(buildSession(member));
    }

    /**
     * 会员登出
     */
    @PostMapping("/logout")
    public AjaxResult logout(@RequestHeader(value = "Authorization", required = false) String authorization)
    {
        if (StringUtils.isNotEmpty(authorization) && authorization.startsWith(Constants.TOKEN_PREFIX))
        {
            memberTokenService.delToken(authorization.replace(Constants.TOKEN_PREFIX, ""));
        }
        return success();
    }

    /**
     * 当前登录会员信息
     */
    @GetMapping("/info")
    public AjaxResult info()
    {
        MemberSession session = MemberContextHolder.getSession();
        if (session == null)
        {
            throw new ServiceException("请先登录", 401);
        }
        Map<String, Object> user = new HashMap<>();
        user.put("userId", session.getMemberId());
        user.put("userName", session.getUsername());
        user.put("nickName", StringUtils.isEmpty(session.getNickname()) ? session.getUsername() : session.getNickname());
        user.put("avatar", session.getAvatar());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        return ajax;
    }

    private MemberSession buildSession(MallMember member)
    {
        MemberSession session = new MemberSession();
        session.setMemberId(member.getMemberId());
        session.setUsername(member.getUsername());
        session.setNickname(member.getNickname());
        session.setAvatar(member.getAvatar());
        return session;
    }

    private AjaxResult loginResult(MemberSession session)
    {
        String token = memberTokenService.createToken(session);
        return AjaxResult.success().put("token", token);
    }
}

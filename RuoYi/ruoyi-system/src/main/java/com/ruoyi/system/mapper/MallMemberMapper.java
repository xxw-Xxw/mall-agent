package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MallMember;

/**
 * 商城会员 数据层
 *
 * @author ruoyi
 */
public interface MallMemberMapper
{
    /**
     * 根据登录账号查询会员
     */
    public MallMember selectMemberByUsername(String username);

    /**
     * 新增会员
     */
    public int insertMember(MallMember member);
}

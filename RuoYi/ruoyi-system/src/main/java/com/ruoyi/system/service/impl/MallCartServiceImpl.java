package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.MallCartMapper;
import com.ruoyi.system.domain.MallCart;
import com.ruoyi.system.service.IMallCartService;

/**
 * 购物车Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
@Service
public class MallCartServiceImpl implements IMallCartService 
{
    @Autowired
    private MallCartMapper mallCartMapper;

    /**
     * 查询购物车
     * 
     * @param cartId 购物车主键
     * @return 购物车
     */
    @Override
    public MallCart selectMallCartByCartId(Long cartId)
    {
        return mallCartMapper.selectMallCartByCartId(cartId);
    }

    /**
     * 查询购物车列表
     * 
     * @param mallCart 购物车
     * @return 购物车
     */
    @Override
    public List<MallCart> selectMallCartList(MallCart mallCart)
    {
        return mallCartMapper.selectMallCartList(mallCart);
    }

    /**
     * 新增购物车
     * 
     * @param mallCart 购物车
     * @return 结果
     */
    @Override
    public int insertMallCart(MallCart mallCart)
    {
        mallCart.setCreateTime(DateUtils.getNowDate());
        return mallCartMapper.insertMallCart(mallCart);
    }

    /**
     * 修改购物车
     * 
     * @param mallCart 购物车
     * @return 结果
     */
    @Override
    public int updateMallCart(MallCart mallCart)
    {
        return mallCartMapper.updateMallCart(mallCart);
    }

    /**
     * 批量删除购物车
     * 
     * @param cartIds 需要删除的购物车主键
     * @return 结果
     */
    @Override
    public int deleteMallCartByCartIds(Long[] cartIds)
    {
        return mallCartMapper.deleteMallCartByCartIds(cartIds);
    }

    /**
     * 删除购物车信息
     * 
     * @param cartId 购物车主键
     * @return 结果
     */
    @Override
    public int deleteMallCartByCartId(Long cartId)
    {
        return mallCartMapper.deleteMallCartByCartId(cartId);
    }
}

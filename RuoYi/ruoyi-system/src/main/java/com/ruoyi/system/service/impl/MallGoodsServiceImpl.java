package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.MallGoodsMapper;
import com.ruoyi.system.domain.MallGoods;
import com.ruoyi.system.service.IMallGoodsService;

/**
 * 商城商品Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
@Service
public class MallGoodsServiceImpl implements IMallGoodsService 
{
    @Autowired
    private MallGoodsMapper mallGoodsMapper;

    /**
     * 查询商城商品
     * 
     * @param goodsId 商城商品主键
     * @return 商城商品
     */
    @Override
    public MallGoods selectMallGoodsByGoodsId(Long goodsId)
    {
        return mallGoodsMapper.selectMallGoodsByGoodsId(goodsId);
    }

    /**
     * 查询商城商品列表
     * 
     * @param mallGoods 商城商品
     * @return 商城商品
     */
    @Override
    public List<MallGoods> selectMallGoodsList(MallGoods mallGoods)
    {
        return mallGoodsMapper.selectMallGoodsList(mallGoods);
    }

    /**
     * 新增商城商品
     * 
     * @param mallGoods 商城商品
     * @return 结果
     */
    @Override
    public int insertMallGoods(MallGoods mallGoods)
    {
        mallGoods.setCreateTime(DateUtils.getNowDate());
        return mallGoodsMapper.insertMallGoods(mallGoods);
    }

    /**
     * 修改商城商品
     * 
     * @param mallGoods 商城商品
     * @return 结果
     */
    @Override
    public int updateMallGoods(MallGoods mallGoods)
    {
        mallGoods.setUpdateTime(DateUtils.getNowDate());
        return mallGoodsMapper.updateMallGoods(mallGoods);
    }

    /**
     * 批量删除商城商品
     * 
     * @param goodsIds 需要删除的商城商品主键
     * @return 结果
     */
    @Override
    public int deleteMallGoodsByGoodsIds(Long[] goodsIds)
    {
        return mallGoodsMapper.deleteMallGoodsByGoodsIds(goodsIds);
    }

    /**
     * 删除商城商品信息
     * 
     * @param goodsId 商城商品主键
     * @return 结果
     */
    @Override
    public int deleteMallGoodsByGoodsId(Long goodsId)
    {
        return mallGoodsMapper.deleteMallGoodsByGoodsId(goodsId);
    }
}

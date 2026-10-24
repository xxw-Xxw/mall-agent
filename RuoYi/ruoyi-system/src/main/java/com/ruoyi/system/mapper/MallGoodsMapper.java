package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.MallGoods;

/**
 * 商城商品Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
public interface MallGoodsMapper 
{
    /**
     * 查询商城商品
     * 
     * @param goodsId 商城商品主键
     * @return 商城商品
     */
    public MallGoods selectMallGoodsByGoodsId(Long goodsId);

    /**
     * 查询商城商品列表
     * 
     * @param mallGoods 商城商品
     * @return 商城商品集合
     */
    public List<MallGoods> selectMallGoodsList(MallGoods mallGoods);

    /**
     * 新增商城商品
     * 
     * @param mallGoods 商城商品
     * @return 结果
     */
    public int insertMallGoods(MallGoods mallGoods);

    /**
     * 修改商城商品
     * 
     * @param mallGoods 商城商品
     * @return 结果
     */
    public int updateMallGoods(MallGoods mallGoods);

    /**
     * 删除商城商品
     * 
     * @param goodsId 商城商品主键
     * @return 结果
     */
    public int deleteMallGoodsByGoodsId(Long goodsId);

    /**
     * 批量删除商城商品
     * 
     * @param goodsIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMallGoodsByGoodsIds(Long[] goodsIds);
}

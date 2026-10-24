package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MallCart;

/**
 * 购物车Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
public interface MallCartMapper 
{
    /**
     * 查询购物车
     * 
     * @param cartId 购物车主键
     * @return 购物车
     */
    public MallCart selectMallCartByCartId(Long cartId);

    /**
     * 查询购物车列表
     * 
     * @param mallCart 购物车
     * @return 购物车集合
     */
    public List<MallCart> selectMallCartList(MallCart mallCart);

    /**
     * 新增购物车
     * 
     * @param mallCart 购物车
     * @return 结果
     */
    public int insertMallCart(MallCart mallCart);

    /**
     * 修改购物车
     * 
     * @param mallCart 购物车
     * @return 结果
     */
    public int updateMallCart(MallCart mallCart);

    /**
     * 删除购物车
     * 
     * @param cartId 购物车主键
     * @return 结果
     */
    public int deleteMallCartByCartId(Long cartId);

    /**
     * 批量删除购物车
     *
     * @param cartIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMallCartByCartIds(Long[] cartIds);

    /**
     * 按商品聚合的加购热度统计（加购件数/加购人数/最近加购时间）
     *
     * @param goodsName 商品名称关键词
     * @return 商品维度聚合列表
     */
    public List<Map<String, Object>> selectCartStatsList(@Param("goodsName") String goodsName);
}

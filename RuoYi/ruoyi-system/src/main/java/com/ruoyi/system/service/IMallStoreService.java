package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MallCategory;
import com.ruoyi.system.domain.MallGoods;
import com.ruoyi.system.domain.dto.StoreOrderDTO;
import com.ruoyi.system.domain.vo.StoreCartVO;
import com.ruoyi.system.domain.vo.StoreFavoriteVO;
import com.ruoyi.system.domain.vo.StoreHistoryVO;
import com.ruoyi.system.domain.vo.StoreOrderVO;

/**
 * 用户端商城Service接口
 *
 * @author ruoyi
 */
public interface IMallStoreService
{
    /**
     * 用户端在售商品列表（支持分类/品牌/关键词/排序）
     */
    public List<MallGoods> selectStoreGoodsList(MallGoods query);

    /**
     * 在售商品品牌列表（去重）
     */
    public List<String> selectBrandList();

    /**
     * 用户端商品详情（仅限在售商品）
     */
    public MallGoods selectStoreGoodsDetail(Long goodsId);

    /**
     * 正常状态分类树（两级）
     */
    public List<MallCategory> selectCategoryTree();

    /**
     * 加入购物车
     */
    public void addCart(Long userId, Long goodsId, Long quantity);

    /**
     * 购物车列表（含商品信息与小计）
     */
    public List<StoreCartVO> selectCartList(Long userId);

    /**
     * 修改购物车数量
     */
    public void updateCartQuantity(Long userId, Long cartId, Long quantity);

    /**
     * 删除购物车项
     */
    public int deleteCarts(Long userId, Long[] cartIds);

    /**
     * 收藏/取消收藏，返回收藏后的状态
     */
    public boolean toggleFavorite(Long userId, Long goodsId);

    /**
     * 是否已收藏
     */
    public boolean isFavorite(Long userId, Long goodsId);

    /**
     * 收藏列表（含商品信息）
     */
    public List<StoreFavoriteVO> selectFavoriteList(Long userId);

    /**
     * 记录浏览日志
     */
    public void recordBrowse(Long userId, Long goodsId);

    /**
     * 浏览记录列表（按商品去重）
     */
    public List<StoreHistoryVO> selectHistoryList(Long userId);

    /**
     * 创建订单（购物车结算或直接购买）
     */
    public StoreOrderVO createOrder(Long userId, StoreOrderDTO dto);

    /**
     * 我的订单列表（含明细）
     */
    public List<StoreOrderVO> selectUserOrderList(Long userId, String status);

    /**
     * 订单详情（校验归属）
     */
    public StoreOrderVO selectUserOrderDetail(Long userId, Long orderId);

    /**
     * 确认收货（待发货 -> 已完成）
     */
    public void confirmOrder(Long userId, Long orderId);

    /**
     * 取消订单（待发货 -> 已取消，回补库存）
     */
    public void cancelOrder(Long userId, Long orderId);
}

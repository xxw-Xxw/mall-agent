package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MallCart;
import com.ruoyi.system.domain.MallGoods;
import com.ruoyi.system.domain.vo.StoreCartVO;
import com.ruoyi.system.domain.vo.StoreFavoriteVO;
import com.ruoyi.system.domain.vo.StoreHistoryVO;
import com.ruoyi.system.domain.vo.StoreOrderItemVO;
import com.ruoyi.system.domain.vo.StoreOrderVO;

/**
 * 用户端商城Mapper接口
 *
 * @author ruoyi
 */
public interface MallStoreMapper
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
     * 购物车列表（联表商品信息）
     */
    public List<StoreCartVO> selectCartListWithGoods(Long userId);

    /**
     * 按购物车项ID集合查询（联表商品信息，用于结算）
     */
    public List<StoreCartVO> selectCartsForOrder(@Param("userId") Long userId, @Param("cartIds") Long[] cartIds);

    /**
     * 加入购物车（同商品数量累加）
     */
    public int insertCartUpsert(MallCart cart);

    /**
     * 修改购物车数量（校验归属）
     */
    public int updateCartQuantity(@Param("cartId") Long cartId, @Param("userId") Long userId, @Param("quantity") Long quantity);

    /**
     * 删除购物车项（校验归属）
     */
    public int deleteCartsByUserAndIds(@Param("userId") Long userId, @Param("cartIds") Long[] cartIds);

    /**
     * 查询用户是否已收藏某商品
     */
    public Long selectFavoriteIdByUserAndGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    /**
     * 新增收藏
     */
    public int insertFavorite(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    /**
     * 取消收藏
     */
    public int deleteFavoriteByUserAndGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    /**
     * 收藏列表（联表商品信息）
     */
    public List<StoreFavoriteVO> selectFavoriteListWithGoods(Long userId);

    /**
     * 记录浏览日志
     */
    public int insertBrowseLog(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    /**
     * 浏览记录列表（按商品去重，联表商品信息）
     */
    public List<StoreHistoryVO> selectHistoryListWithGoods(Long userId);

    /**
     * 我的订单列表
     */
    public List<StoreOrderVO> selectUserOrderList(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 订单详情（含用户归属字段，用于权限校验）
     */
    public StoreOrderVO selectUserOrderDetail(Long orderId);

    /**
     * 按订单ID集合查询订单明细（含商品主图）
     */
    public List<StoreOrderItemVO> selectOrderItemsByOrderIds(@Param("orderIds") List<Long> orderIds);

    /**
     * 更新我的订单状态（校验归属与当前状态）
     */
    public int updateUserOrderStatus(@Param("orderId") Long orderId, @Param("userId") Long userId,
            @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus);

    /**
     * 扣减库存并累加销量（库存充足时生效）
     */
    public int deductGoodsStock(@Param("goodsId") Long goodsId, @Param("quantity") Long quantity);

    /**
     * 回补库存并扣减销量（取消订单）
     */
    public int restockGoods(@Param("goodsId") Long goodsId, @Param("quantity") Long quantity);
}

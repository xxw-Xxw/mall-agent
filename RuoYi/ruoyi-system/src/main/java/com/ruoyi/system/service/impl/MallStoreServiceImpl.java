package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.MallCart;
import com.ruoyi.system.domain.MallCategory;
import com.ruoyi.system.domain.MallGoods;
import com.ruoyi.system.domain.MallOrder;
import com.ruoyi.system.domain.MallOrderItem;
import com.ruoyi.system.domain.dto.StoreOrderDTO;
import com.ruoyi.system.domain.vo.StoreCartVO;
import com.ruoyi.system.domain.vo.StoreFavoriteVO;
import com.ruoyi.system.domain.vo.StoreHistoryVO;
import com.ruoyi.system.domain.vo.StoreOrderItemVO;
import com.ruoyi.system.domain.vo.StoreOrderVO;
import com.ruoyi.system.mapper.MallCategoryMapper;
import com.ruoyi.system.mapper.MallGoodsMapper;
import com.ruoyi.system.mapper.MallOrderItemMapper;
import com.ruoyi.system.mapper.MallOrderMapper;
import com.ruoyi.system.mapper.MallStoreMapper;
import com.ruoyi.system.service.IMallStoreService;

/**
 * 用户端商城Service实现
 *
 * @author ruoyi
 */
@Service
public class MallStoreServiceImpl implements IMallStoreService
{
    @Autowired
    private MallStoreMapper mallStoreMapper;

    @Autowired
    private MallGoodsMapper mallGoodsMapper;

    @Autowired
    private MallCategoryMapper mallCategoryMapper;

    @Autowired
    private MallOrderMapper mallOrderMapper;

    @Autowired
    private MallOrderItemMapper mallOrderItemMapper;

    @Override
    public List<MallGoods> selectStoreGoodsList(MallGoods query)
    {
        return mallStoreMapper.selectStoreGoodsList(query);
    }

    @Override
    public List<String> selectBrandList()
    {
        return mallStoreMapper.selectBrandList();
    }

    @Override
    public MallGoods selectStoreGoodsDetail(Long goodsId)
    {
        MallGoods goods = mallGoodsMapper.selectMallGoodsByGoodsId(goodsId);
        checkGoodsOnSale(goods);
        return goods;
    }

    @Override
    public List<MallCategory> selectCategoryTree()
    {
        MallCategory query = new MallCategory();
        query.setStatus("0");
        List<MallCategory> all = mallCategoryMapper.selectMallCategoryList(query);

        Map<Long, List<MallCategory>> childrenMap = new HashMap<>();
        for (MallCategory category : all)
        {
            Long parentId = category.getParentId() == null ? 0L : category.getParentId();
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
        }
        List<MallCategory> roots = childrenMap.getOrDefault(0L, new ArrayList<>());
        roots.sort(this::compareBySortOrder);
        for (MallCategory root : roots)
        {
            List<MallCategory> children = childrenMap.getOrDefault(root.getCategoryId(), new ArrayList<>());
            children.sort(this::compareBySortOrder);
            root.setChildren(children);
        }
        return roots;
    }

    private int compareBySortOrder(MallCategory a, MallCategory b)
    {
        long sa = a.getSortOrder() == null ? Long.MAX_VALUE : a.getSortOrder();
        long sb = b.getSortOrder() == null ? Long.MAX_VALUE : b.getSortOrder();
        return Long.compare(sa, sb);
    }

    @Override
    public void addCart(Long userId, Long goodsId, Long quantity)
    {
        MallGoods goods = mallGoodsMapper.selectMallGoodsByGoodsId(goodsId);
        checkGoodsOnSale(goods);
        Long qty = normalizeQuantity(quantity);
        if (goods.getStock() != null && goods.getStock() < qty)
        {
            throw new ServiceException("商品库存不足");
        }
        MallCart cart = new MallCart();
        cart.setUserId(userId);
        cart.setGoodsId(goodsId);
        cart.setQuantity(qty);
        mallStoreMapper.insertCartUpsert(cart);
    }

    @Override
    public List<StoreCartVO> selectCartList(Long userId)
    {
        return mallStoreMapper.selectCartListWithGoods(userId);
    }

    @Override
    public void updateCartQuantity(Long userId, Long cartId, Long quantity)
    {
        Long qty = normalizeQuantity(quantity);
        int rows = mallStoreMapper.updateCartQuantity(cartId, userId, qty);
        if (rows == 0)
        {
            throw new ServiceException("购物车项不存在");
        }
    }

    @Override
    public int deleteCarts(Long userId, Long[] cartIds)
    {
        return mallStoreMapper.deleteCartsByUserAndIds(userId, cartIds);
    }

    @Override
    public boolean toggleFavorite(Long userId, Long goodsId)
    {
        MallGoods goods = mallGoodsMapper.selectMallGoodsByGoodsId(goodsId);
        checkGoodsOnSale(goods);
        if (mallStoreMapper.selectFavoriteIdByUserAndGoods(userId, goodsId) != null)
        {
            mallStoreMapper.deleteFavoriteByUserAndGoods(userId, goodsId);
            return false;
        }
        mallStoreMapper.insertFavorite(userId, goodsId);
        return true;
    }

    @Override
    public boolean isFavorite(Long userId, Long goodsId)
    {
        return mallStoreMapper.selectFavoriteIdByUserAndGoods(userId, goodsId) != null;
    }

    @Override
    public List<StoreFavoriteVO> selectFavoriteList(Long userId)
    {
        return mallStoreMapper.selectFavoriteListWithGoods(userId);
    }

    @Override
    public void recordBrowse(Long userId, Long goodsId)
    {
        mallStoreMapper.insertBrowseLog(userId, goodsId);
    }

    @Override
    public List<StoreHistoryVO> selectHistoryList(Long userId)
    {
        return mallStoreMapper.selectHistoryListWithGoods(userId);
    }

    @Override
    @Transactional
    public StoreOrderVO createOrder(Long userId, StoreOrderDTO dto)
    {
        List<MallOrderItem> items = new ArrayList<>();
        boolean fromCart = dto.getCartIds() != null && dto.getCartIds().length > 0;
        if (fromCart)
        {
            List<StoreCartVO> carts = mallStoreMapper.selectCartsForOrder(userId, dto.getCartIds());
            if (carts.isEmpty())
            {
                throw new ServiceException("购物车商品不存在");
            }
            for (StoreCartVO cart : carts)
            {
                if (cart.getStock() == null || cart.getStock() < cart.getQuantity())
                {
                    throw new ServiceException("商品【" + cart.getGoodsName() + "】库存不足");
                }
                items.add(buildItem(cart.getGoodsId(), cart.getGoodsName(), cart.getPrice(), cart.getQuantity()));
            }
        }
        else if (dto.getGoodsId() != null)
        {
            MallGoods goods = mallGoodsMapper.selectMallGoodsByGoodsId(dto.getGoodsId());
            checkGoodsOnSale(goods);
            Long qty = normalizeQuantity(dto.getQuantity());
            if (goods.getStock() != null && goods.getStock() < qty)
            {
                throw new ServiceException("商品库存不足");
            }
            items.add(buildItem(goods.getGoodsId(), goods.getGoodsName(), goods.getPrice(), qty));
        }
        else
        {
            throw new ServiceException("请选择要结算的商品");
        }

        // 逐件扣库存（库存不足则整体回滚）
        for (MallOrderItem item : items)
        {
            if (mallStoreMapper.deductGoodsStock(item.getGoodsId(), item.getQuantity()) == 0)
            {
                throw new ServiceException("商品【" + item.getGoodsName() + "】库存不足");
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        for (MallOrderItem item : items)
        {
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        MallOrder order = new MallOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus("1");
        order.setRemark(dto.getRemark());
        order.setCreateBy(String.valueOf(userId));
        order.setCreateTime(DateUtils.getNowDate());
        mallOrderMapper.insertMallOrder(order);

        for (MallOrderItem item : items)
        {
            item.setOrderId(order.getOrderId());
            mallOrderItemMapper.insertMallOrderItem(item);
        }

        if (fromCart)
        {
            mallStoreMapper.deleteCartsByUserAndIds(userId, dto.getCartIds());
        }
        return selectUserOrderDetail(userId, order.getOrderId());
    }

    @Override
    public List<StoreOrderVO> selectUserOrderList(Long userId, String status)
    {
        List<StoreOrderVO> orders = mallStoreMapper.selectUserOrderList(userId, status);
        attachItems(orders);
        return orders;
    }

    @Override
    public StoreOrderVO selectUserOrderDetail(Long userId, Long orderId)
    {
        StoreOrderVO order = mallStoreMapper.selectUserOrderDetail(orderId);
        if (order == null || !order.getUserId().equals(userId))
        {
            throw new ServiceException("订单不存在");
        }
        attachItems(Collections.singletonList(order));
        return order;
    }

    @Override
    public void confirmOrder(Long userId, Long orderId)
    {
        StoreOrderVO order = mallStoreMapper.selectUserOrderDetail(orderId);
        if (order == null || !order.getUserId().equals(userId))
        {
            throw new ServiceException("订单不存在");
        }
        if (!"2".equals(order.getStatus()))
        {
            throw new ServiceException("当前订单状态无法确认收货");
        }
        int rows = mallStoreMapper.updateUserOrderStatus(orderId, userId, "2", "3");
        if (rows == 0)
        {
            throw new ServiceException("订单状态已变化，请刷新后重试");
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId)
    {
        StoreOrderVO order = mallStoreMapper.selectUserOrderDetail(orderId);
        if (order == null || !order.getUserId().equals(userId))
        {
            throw new ServiceException("订单不存在");
        }
        if (!"1".equals(order.getStatus()))
        {
            throw new ServiceException("已发货订单无法取消");
        }
        List<StoreOrderItemVO> items = mallStoreMapper.selectOrderItemsByOrderIds(Collections.singletonList(orderId));
        for (StoreOrderItemVO item : items)
        {
            mallStoreMapper.restockGoods(item.getGoodsId(), item.getQuantity());
        }
        int rows = mallStoreMapper.updateUserOrderStatus(orderId, userId, "1", "4");
        if (rows == 0)
        {
            throw new ServiceException("订单状态已变化，请刷新后重试");
        }
    }

    private void attachItems(List<StoreOrderVO> orders)
    {
        if (orders.isEmpty())
        {
            return;
        }
        List<Long> orderIds = orders.stream().map(StoreOrderVO::getOrderId).collect(Collectors.toList());
        List<StoreOrderItemVO> items = mallStoreMapper.selectOrderItemsByOrderIds(orderIds);
        Map<Long, List<StoreOrderItemVO>> itemsMap = items.stream().collect(Collectors.groupingBy(StoreOrderItemVO::getOrderId));
        for (StoreOrderVO order : orders)
        {
            order.setItems(itemsMap.getOrDefault(order.getOrderId(), new ArrayList<>()));
        }
    }

    private MallOrderItem buildItem(Long goodsId, String goodsName, BigDecimal price, Long quantity)
    {
        MallOrderItem item = new MallOrderItem();
        item.setGoodsId(goodsId);
        item.setGoodsName(goodsName);
        item.setPrice(price);
        item.setQuantity(quantity);
        return item;
    }

    private void checkGoodsOnSale(MallGoods goods)
    {
        if (goods == null || !"0".equals(goods.getStatus()) || "2".equals(goods.getDelFlag()))
        {
            throw new ServiceException("商品不存在或已下架");
        }
    }

    private Long normalizeQuantity(Long quantity)
    {
        if (quantity == null || quantity < 1)
        {
            return 1L;
        }
        if (quantity > 999)
        {
            return 999L;
        }
        return quantity;
    }

    private String generateOrderNo()
    {
        return "SO" + DateUtils.dateTimeNow() + (int) (Math.random() * 900 + 100);
    }
}

package com.ruoyi.system.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商城运营控制台统计 Mapper
 *
 * @author ruoyi
 */
public interface MallDashboardMapper
{
    /** 用户总数 */
    public long countUsers();

    /** 今日新增用户 */
    public long countUsersToday();

    /** 总营业额（已完成订单） */
    public BigDecimal sumRevenue();

    /** 今日营业额 */
    public BigDecimal sumRevenueToday();

    /** 订单总数 */
    public long countOrders();

    /** 今日订单数 */
    public long countOrdersToday();

    /** 待收货订单数 */
    public long countOrdersPending();

    /** 商品总数 */
    public long countGoods();

    /** 商品分类总数 */
    public long countCategories();

    /** 库存预警商品数（库存小于10） */
    public long countLowStockGoods();

    /** 近7日营业额（按天，已完成订单） */
    public List<Map<String, Object>> selectRevenueTrend();

    /** 订单状态分布 */
    public List<Map<String, Object>> selectOrderStatusDist();

    /** 热销商品 TOP5 */
    public List<Map<String, Object>> selectHotGoods();

    /** 低库存商品 TOP5 */
    public List<Map<String, Object>> selectLowStockGoods();
}

package com.ruoyi.web.controller.mall;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.mapper.MallDashboardMapper;

/**
 * 商城运营控制台
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mall/dashboard")
public class MallDashboardController extends BaseController
{
    private static final Map<String, String> ORDER_STATUS_NAME = Map.of("0", "待付款", "1", "待发货", "2", "已发货", "3", "已完成", "4", "已取消");

    @Autowired
    private MallDashboardMapper dashboardMapper;

    /**
     * 控制台统计数据（一次返回全部指标）
     */
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("userTotal", dashboardMapper.countUsers());
        data.put("userToday", dashboardMapper.countUsersToday());
        data.put("revenueTotal", dashboardMapper.sumRevenue());
        data.put("revenueToday", dashboardMapper.sumRevenueToday());
        data.put("orderTotal", dashboardMapper.countOrders());
        data.put("orderToday", dashboardMapper.countOrdersToday());
        data.put("orderPending", dashboardMapper.countOrdersPending());
        data.put("goodsTotal", dashboardMapper.countGoods());
        data.put("categoryTotal", dashboardMapper.countCategories());
        data.put("lowStockCount", dashboardMapper.countLowStockGoods());

        data.put("revenueTrend", buildRevenueTrend());
        data.put("orderStatusDist", buildStatusDist());
        data.put("hotGoods", dashboardMapper.selectHotGoods());
        data.put("lowStockGoods", dashboardMapper.selectLowStockGoods());

        return success(data);
    }

    /**
     * 近7日营业额趋势，缺失的日期补 0
     */
    private List<Map<String, Object>> buildRevenueTrend()
    {
        Map<String, Object> amountByDate = new HashMap<>();
        for (Map<String, Object> row : dashboardMapper.selectRevenueTrend())
        {
            LocalDate day = LocalDate.parse(String.valueOf(row.get("date")));
            amountByDate.put(day.format(DateTimeFormatter.ofPattern("MM-dd")), row.get("amount"));
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--)
        {
            String day = LocalDate.now().minusDays(i).format(fmt);
            Object amount = amountByDate.getOrDefault(day, BigDecimal.ZERO);
            Map<String, Object> point = new HashMap<>();
            point.put("date", day);
            point.put("amount", amount == null ? BigDecimal.ZERO : amount);
            trend.add(point);
        }
        return trend;
    }

    /**
     * 订单状态分布，补齐无订单的状态
     */
    private List<Map<String, Object>> buildStatusDist()
    {
        Map<String, Object> countByStatus = new HashMap<>();
        for (Map<String, Object> row : dashboardMapper.selectOrderStatusDist())
        {
            countByStatus.put(String.valueOf(row.get("status")), row.get("count"));
        }
        List<Map<String, Object>> dist = new ArrayList<>();
        for (String status : new String[] { "0", "1", "2" })
        {
            Map<String, Object> item = new HashMap<>();
            item.put("name", ORDER_STATUS_NAME.get(status));
            item.put("value", countByStatus.getOrDefault(status, 0L));
            dist.add(item);
        }
        return dist;
    }
}

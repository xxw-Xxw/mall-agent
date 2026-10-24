package com.ruoyi.web.controller.mall;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.MemberContextHolder;
import com.ruoyi.system.domain.dto.StoreOrderDTO;
import com.ruoyi.system.domain.vo.StoreOrderVO;
import com.ruoyi.system.service.IMallStoreService;

/**
 * 用户端订单（需会员登录）
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/store/orders")
public class StoreOrderController extends BaseController
{
    @Autowired
    private IMallStoreService mallStoreService;

    /**
     * 创建订单（购物车结算或直接购买）
     */
    @PostMapping
    public AjaxResult create(@RequestBody StoreOrderDTO dto)
    {
        StoreOrderVO order = mallStoreService.createOrder(MemberContextHolder.requireMemberId(), dto);
        return success(order);
    }

    /**
     * 我的订单列表（分页，可按状态筛选）
     */
    @GetMapping("/list")
    public TableDataInfo list(String status)
    {
        startPage();
        return getDataTable(mallStoreService.selectUserOrderList(MemberContextHolder.requireMemberId(), status));
    }

    /**
     * 订单详情
     */
    @GetMapping("/{orderId}")
    public AjaxResult detail(@PathVariable Long orderId)
    {
        return success(mallStoreService.selectUserOrderDetail(MemberContextHolder.requireMemberId(), orderId));
    }

    /**
     * 确认收货
     */
    @PutMapping("/{orderId}/confirm")
    public AjaxResult confirm(@PathVariable Long orderId)
    {
        mallStoreService.confirmOrder(MemberContextHolder.requireMemberId(), orderId);
        return success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/{orderId}/cancel")
    public AjaxResult cancel(@PathVariable Long orderId)
    {
        mallStoreService.cancelOrder(MemberContextHolder.requireMemberId(), orderId);
        return success();
    }
}

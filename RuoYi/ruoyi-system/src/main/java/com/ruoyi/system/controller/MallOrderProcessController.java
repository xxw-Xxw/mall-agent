package com.ruoyi.system.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.MallOrder;
import com.ruoyi.system.domain.MallOrderItem;
import com.ruoyi.system.mapper.MallOrderItemMapper;
import com.ruoyi.system.mapper.MallOrderMapper;

/**
 * 订单处理Controller（员工发货工作台，与订单管理分离）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/orderProcess")
public class MallOrderProcessController extends BaseController
{
    @Autowired
    private MallOrderMapper mallOrderMapper;

    @Autowired
    private MallOrderItemMapper mallOrderItemMapper;

    /**
     * 订单商品明细（发货工作台自包含，不依赖订单明细菜单权限）
     */
    @PreAuthorize("@ss.hasPermi('system:orderProcess:query')")
    @GetMapping("/items/{orderId}")
    public AjaxResult items(@PathVariable("orderId") Long orderId)
    {
        MallOrderItem query = new MallOrderItem();
        query.setOrderId(orderId);
        return success(mallOrderItemMapper.selectMallOrderItemList(query));
    }

    /**
     * 待发货订单列表（status=1）
     */
    @PreAuthorize("@ss.hasPermi('system:orderProcess:list')")
    @GetMapping("/pending")
    public TableDataInfo pending(MallOrder mallOrder)
    {
        mallOrder.setStatus("1");
        startPage();
        List<MallOrder> list = mallOrderMapper.selectProcessOrderList(mallOrder);
        return getDataTable(list);
    }

    /**
     * 已发货订单列表（status=2，含发货人）
     */
    @PreAuthorize("@ss.hasPermi('system:orderProcess:list')")
    @GetMapping("/shipped")
    public TableDataInfo shipped(MallOrder mallOrder)
    {
        mallOrder.setStatus("2");
        startPage();
        List<MallOrder> list = mallOrderMapper.selectProcessOrderList(mallOrder);
        return getDataTable(list);
    }

    /**
     * 单个订单发货（1→2）
     */
    @PreAuthorize("@ss.hasPermi('system:orderProcess:ship')")
    @Log(title = "订单发货", businessType = BusinessType.UPDATE)
    @PutMapping("/ship/{orderId}")
    public AjaxResult ship(@PathVariable("orderId") Long orderId)
    {
        int rows = mallOrderMapper.shipOrder(orderId, SecurityUtils.getUsername());
        if (rows == 0)
        {
            throw new ServiceException("订单不存在或已发货");
        }
        return success();
    }

    /**
     * 批量发货
     */
    @PreAuthorize("@ss.hasPermi('system:orderProcess:ship')")
    @Log(title = "订单批量发货", businessType = BusinessType.UPDATE)
    @Transactional
    @PutMapping("/shipBatch/{orderIds}")
    public AjaxResult shipBatch(@PathVariable Long[] orderIds)
    {
        String operName = SecurityUtils.getUsername();
        int success = 0;
        for (Long orderId : orderIds)
        {
            success += mallOrderMapper.shipOrder(orderId, operName);
        }
        if (success == 0)
        {
            throw new ServiceException("订单均不存在或已发货");
        }
        AjaxResult ajax = AjaxResult.success("成功发货 " + success + " 单");
        ajax.put("shipped", success);
        ajax.put("skipped", orderIds.length - success);
        return ajax;
    }
}

package com.ruoyi.system.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.MallOrderItem;
import com.ruoyi.system.service.IMallOrderItemService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 订单明细Controller
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
@RestController
@RequestMapping("/system/item")
public class MallOrderItemController extends BaseController
{
    @Autowired
    private IMallOrderItemService mallOrderItemService;

    /**
     * 查询订单明细列表
     */
    @PreAuthorize("@ss.hasPermi('system:item:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallOrderItem mallOrderItem)
    {
        startPage();
        List<MallOrderItem> list = mallOrderItemService.selectMallOrderItemList(mallOrderItem);
        return getDataTable(list);
    }

    /**
     * 导出订单明细列表
     */
    @PreAuthorize("@ss.hasPermi('system:item:export')")
    @Log(title = "订单明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MallOrderItem mallOrderItem)
    {
        List<MallOrderItem> list = mallOrderItemService.selectMallOrderItemList(mallOrderItem);
        ExcelUtil<MallOrderItem> util = new ExcelUtil<MallOrderItem>(MallOrderItem.class);
        util.exportExcel(response, list, "订单明细数据");
    }

    /**
     * 获取订单明细详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:item:query')")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable("itemId") Long itemId)
    {
        return success(mallOrderItemService.selectMallOrderItemByItemId(itemId));
    }

    /**
     * 新增订单明细
     */
    @PreAuthorize("@ss.hasPermi('system:item:add')")
    @Log(title = "订单明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MallOrderItem mallOrderItem)
    {
        return toAjax(mallOrderItemService.insertMallOrderItem(mallOrderItem));
    }

    /**
     * 修改订单明细
     */
    @PreAuthorize("@ss.hasPermi('system:item:edit')")
    @Log(title = "订单明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MallOrderItem mallOrderItem)
    {
        return toAjax(mallOrderItemService.updateMallOrderItem(mallOrderItem));
    }

    /**
     * 删除订单明细
     */
    @PreAuthorize("@ss.hasPermi('system:item:remove')")
    @Log(title = "订单明细", businessType = BusinessType.DELETE)
	@DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds)
    {
        return toAjax(mallOrderItemService.deleteMallOrderItemByItemIds(itemIds));
    }
}

package com.ruoyi.system.controller;

import java.util.List;
import java.util.Map;
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
import com.ruoyi.system.domain.MallCart;
import com.ruoyi.system.mapper.MallCartMapper;
import com.ruoyi.system.service.IMallCartService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 购物车Controller
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
@RestController
@RequestMapping("/system/cart")
public class MallCartController extends BaseController
{
    @Autowired
    private IMallCartService mallCartService;

    @Autowired
    private MallCartMapper mallCartMapper;

    /**
     * 按商品聚合的加购热度统计（加购件数/加购人数，可按列排序）
     */
    @PreAuthorize("@ss.hasPermi('system:cart:list')")
    @GetMapping("/stats")
    public TableDataInfo stats(String goodsName)
    {
        startPage();
        List<Map<String, Object>> list = mallCartMapper.selectCartStatsList(goodsName);
        return getDataTable(list);
    }

    /**
     * 查询购物车列表
     */
    @PreAuthorize("@ss.hasPermi('system:cart:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallCart mallCart)
    {
        startPage();
        List<MallCart> list = mallCartService.selectMallCartList(mallCart);
        return getDataTable(list);
    }

    /**
     * 导出购物车列表
     */
    @PreAuthorize("@ss.hasPermi('system:cart:export')")
    @Log(title = "购物车", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MallCart mallCart)
    {
        List<MallCart> list = mallCartService.selectMallCartList(mallCart);
        ExcelUtil<MallCart> util = new ExcelUtil<MallCart>(MallCart.class);
        util.exportExcel(response, list, "购物车数据");
    }

    /**
     * 获取购物车详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:cart:query')")
    @GetMapping(value = "/{cartId}")
    public AjaxResult getInfo(@PathVariable("cartId") Long cartId)
    {
        return success(mallCartService.selectMallCartByCartId(cartId));
    }

    /**
     * 新增购物车
     */
    @PreAuthorize("@ss.hasPermi('system:cart:add')")
    @Log(title = "购物车", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MallCart mallCart)
    {
        return toAjax(mallCartService.insertMallCart(mallCart));
    }

    /**
     * 修改购物车
     */
    @PreAuthorize("@ss.hasPermi('system:cart:edit')")
    @Log(title = "购物车", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MallCart mallCart)
    {
        return toAjax(mallCartService.updateMallCart(mallCart));
    }

    /**
     * 删除购物车
     */
    @PreAuthorize("@ss.hasPermi('system:cart:remove')")
    @Log(title = "购物车", businessType = BusinessType.DELETE)
	@DeleteMapping("/{cartIds}")
    public AjaxResult remove(@PathVariable Long[] cartIds)
    {
        return toAjax(mallCartService.deleteMallCartByCartIds(cartIds));
    }
}

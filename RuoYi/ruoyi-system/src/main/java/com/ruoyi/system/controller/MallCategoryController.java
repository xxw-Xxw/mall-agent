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
import com.ruoyi.system.domain.MallCategory;
import com.ruoyi.system.service.IMallCategoryService;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 商城商品分类Controller
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
@RestController
@RequestMapping("/system/category")
public class MallCategoryController extends BaseController
{
    @Autowired
    private IMallCategoryService mallCategoryService;

    /**
     * 查询商城商品分类列表
     */
    @PreAuthorize("@ss.hasPermi('system:category:list')")
    @GetMapping("/list")
    public AjaxResult list(MallCategory mallCategory)
    {
        List<MallCategory> list = mallCategoryService.selectMallCategoryList(mallCategory);
        return success(list);
    }

    /**
     * 导出商城商品分类列表
     */
    @PreAuthorize("@ss.hasPermi('system:category:export')")
    @Log(title = "商城商品分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MallCategory mallCategory)
    {
        List<MallCategory> list = mallCategoryService.selectMallCategoryList(mallCategory);
        ExcelUtil<MallCategory> util = new ExcelUtil<MallCategory>(MallCategory.class);
        util.exportExcel(response, list, "商城商品分类数据");
    }

    /**
     * 获取商城商品分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId)
    {
        return success(mallCategoryService.selectMallCategoryByCategoryId(categoryId));
    }

    /**
     * 新增商城商品分类
     */
    @PreAuthorize("@ss.hasPermi('system:category:add')")
    @Log(title = "商城商品分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MallCategory mallCategory)
    {
        return toAjax(mallCategoryService.insertMallCategory(mallCategory));
    }

    /**
     * 修改商城商品分类
     */
    @PreAuthorize("@ss.hasPermi('system:category:edit')")
    @Log(title = "商城商品分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MallCategory mallCategory)
    {
        return toAjax(mallCategoryService.updateMallCategory(mallCategory));
    }

    /**
     * 删除商城商品分类
     */
    @PreAuthorize("@ss.hasPermi('system:category:remove')")
    @Log(title = "商城商品分类", businessType = BusinessType.DELETE)
	@DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(mallCategoryService.deleteMallCategoryByCategoryIds(categoryIds));
    }
}

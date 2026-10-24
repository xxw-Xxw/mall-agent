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
import com.ruoyi.system.domain.MallFavorite;
import com.ruoyi.system.mapper.MallFavoriteMapper;
import com.ruoyi.system.service.IMallFavoriteService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 商品收藏Controller
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
@RestController
@RequestMapping("/system/favorite")
public class MallFavoriteController extends BaseController
{
    @Autowired
    private IMallFavoriteService mallFavoriteService;

    @Autowired
    private MallFavoriteMapper mallFavoriteMapper;

    /**
     * 按商品聚合的收藏热度统计（收藏人数，可按列排序）
     */
    @PreAuthorize("@ss.hasPermi('system:favorite:list')")
    @GetMapping("/stats")
    public TableDataInfo stats(String goodsName)
    {
        startPage();
        List<Map<String, Object>> list = mallFavoriteMapper.selectFavoriteStatsList(goodsName);
        return getDataTable(list);
    }

    /**
     * 查询商品收藏列表
     */
    @PreAuthorize("@ss.hasPermi('system:favorite:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallFavorite mallFavorite)
    {
        startPage();
        List<MallFavorite> list = mallFavoriteService.selectMallFavoriteList(mallFavorite);
        return getDataTable(list);
    }

    /**
     * 导出商品收藏列表
     */
    @PreAuthorize("@ss.hasPermi('system:favorite:export')")
    @Log(title = "商品收藏", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MallFavorite mallFavorite)
    {
        List<MallFavorite> list = mallFavoriteService.selectMallFavoriteList(mallFavorite);
        ExcelUtil<MallFavorite> util = new ExcelUtil<MallFavorite>(MallFavorite.class);
        util.exportExcel(response, list, "商品收藏数据");
    }

    /**
     * 获取商品收藏详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:favorite:query')")
    @GetMapping(value = "/{favoriteId}")
    public AjaxResult getInfo(@PathVariable("favoriteId") Long favoriteId)
    {
        return success(mallFavoriteService.selectMallFavoriteByFavoriteId(favoriteId));
    }

    /**
     * 新增商品收藏
     */
    @PreAuthorize("@ss.hasPermi('system:favorite:add')")
    @Log(title = "商品收藏", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MallFavorite mallFavorite)
    {
        return toAjax(mallFavoriteService.insertMallFavorite(mallFavorite));
    }

    /**
     * 修改商品收藏
     */
    @PreAuthorize("@ss.hasPermi('system:favorite:edit')")
    @Log(title = "商品收藏", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MallFavorite mallFavorite)
    {
        return toAjax(mallFavoriteService.updateMallFavorite(mallFavorite));
    }

    /**
     * 删除商品收藏
     */
    @PreAuthorize("@ss.hasPermi('system:favorite:remove')")
    @Log(title = "商品收藏", businessType = BusinessType.DELETE)
	@DeleteMapping("/{favoriteIds}")
    public AjaxResult remove(@PathVariable Long[] favoriteIds)
    {
        return toAjax(mallFavoriteService.deleteMallFavoriteByFavoriteIds(favoriteIds));
    }
}

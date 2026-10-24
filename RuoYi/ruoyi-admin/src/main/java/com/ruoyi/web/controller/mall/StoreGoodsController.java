package com.ruoyi.web.controller.mall;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.MemberContextHolder;
import com.ruoyi.system.domain.MallCategory;
import com.ruoyi.system.domain.MallGoods;
import com.ruoyi.system.service.IMallStoreService;

/**
 * 用户端商品浏览（匿名可访问）
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/store")
public class StoreGoodsController extends BaseController
{
    @Autowired
    private IMallStoreService mallStoreService;

    /**
     * 分类树
     */
    @GetMapping("/categories")
    public AjaxResult categories()
    {
        List<MallCategory> tree = mallStoreService.selectCategoryTree();
        return success(tree);
    }

    /**
     * 在售商品列表（分页，支持分类/品牌/关键词/排序）
     */
    @GetMapping("/goods/list")
    public TableDataInfo goodsList(Long categoryId, String keyword, String brand, String sortField, String order)
    {
        startPage();
        MallGoods query = new MallGoods();
        query.setCategoryId(categoryId);
        query.setGoodsName(keyword);
        query.setBrand(brand);
        query.getParams().put("sortField", sortField);
        query.getParams().put("order", order);
        List<MallGoods> list = mallStoreService.selectStoreGoodsList(query);
        return getDataTable(list);
    }

    /**
     * 在售商品品牌列表（去重）
     */
    @GetMapping("/goods/brands")
    public AjaxResult brands()
    {
        return success(mallStoreService.selectBrandList());
    }

    /**
     * 商品详情（匿名可看；已登录时记录浏览日志并返回收藏状态）
     */
    @GetMapping("/goods/{goodsId}")
    public AjaxResult goodsDetail(@PathVariable Long goodsId)
    {
        MallGoods goods = mallStoreService.selectStoreGoodsDetail(goodsId);
        Map<String, Object> data = new HashMap<>();
        data.put("goods", goods);
        Long userId = MemberContextHolder.getMemberId();
        if (userId != null)
        {
            mallStoreService.recordBrowse(userId, goodsId);
            data.put("isFavorite", mallStoreService.isFavorite(userId, goodsId));
        }
        else
        {
            data.put("isFavorite", false);
        }
        return success(data);
    }
}

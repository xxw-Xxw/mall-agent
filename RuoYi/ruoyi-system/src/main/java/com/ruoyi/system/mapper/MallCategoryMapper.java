package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.MallCategory;

/**
 * 商城商品分类Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
public interface MallCategoryMapper 
{
    /**
     * 查询商城商品分类
     * 
     * @param categoryId 商城商品分类主键
     * @return 商城商品分类
     */
    public MallCategory selectMallCategoryByCategoryId(Long categoryId);

    /**
     * 查询商城商品分类列表
     * 
     * @param mallCategory 商城商品分类
     * @return 商城商品分类集合
     */
    public List<MallCategory> selectMallCategoryList(MallCategory mallCategory);

    /**
     * 新增商城商品分类
     * 
     * @param mallCategory 商城商品分类
     * @return 结果
     */
    public int insertMallCategory(MallCategory mallCategory);

    /**
     * 修改商城商品分类
     * 
     * @param mallCategory 商城商品分类
     * @return 结果
     */
    public int updateMallCategory(MallCategory mallCategory);

    /**
     * 删除商城商品分类
     * 
     * @param categoryId 商城商品分类主键
     * @return 结果
     */
    public int deleteMallCategoryByCategoryId(Long categoryId);

    /**
     * 批量删除商城商品分类
     * 
     * @param categoryIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMallCategoryByCategoryIds(Long[] categoryIds);
}

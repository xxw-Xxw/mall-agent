package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.MallCategoryMapper;
import com.ruoyi.system.domain.MallCategory;
import com.ruoyi.system.service.IMallCategoryService;

/**
 * 商城商品分类Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
@Service
public class MallCategoryServiceImpl implements IMallCategoryService 
{
    @Autowired
    private MallCategoryMapper mallCategoryMapper;

    /**
     * 查询商城商品分类
     * 
     * @param categoryId 商城商品分类主键
     * @return 商城商品分类
     */
    @Override
    public MallCategory selectMallCategoryByCategoryId(Long categoryId)
    {
        return mallCategoryMapper.selectMallCategoryByCategoryId(categoryId);
    }

    /**
     * 查询商城商品分类列表
     * 
     * @param mallCategory 商城商品分类
     * @return 商城商品分类
     */
    @Override
    public List<MallCategory> selectMallCategoryList(MallCategory mallCategory)
    {
        return mallCategoryMapper.selectMallCategoryList(mallCategory);
    }

    /**
     * 新增商城商品分类
     * 
     * @param mallCategory 商城商品分类
     * @return 结果
     */
    @Override
    public int insertMallCategory(MallCategory mallCategory)
    {
        mallCategory.setCreateTime(DateUtils.getNowDate());
        return mallCategoryMapper.insertMallCategory(mallCategory);
    }

    /**
     * 修改商城商品分类
     * 
     * @param mallCategory 商城商品分类
     * @return 结果
     */
    @Override
    public int updateMallCategory(MallCategory mallCategory)
    {
        mallCategory.setUpdateTime(DateUtils.getNowDate());
        return mallCategoryMapper.updateMallCategory(mallCategory);
    }

    /**
     * 批量删除商城商品分类
     * 
     * @param categoryIds 需要删除的商城商品分类主键
     * @return 结果
     */
    @Override
    public int deleteMallCategoryByCategoryIds(Long[] categoryIds)
    {
        return mallCategoryMapper.deleteMallCategoryByCategoryIds(categoryIds);
    }

    /**
     * 删除商城商品分类信息
     * 
     * @param categoryId 商城商品分类主键
     * @return 结果
     */
    @Override
    public int deleteMallCategoryByCategoryId(Long categoryId)
    {
        return mallCategoryMapper.deleteMallCategoryByCategoryId(categoryId);
    }
}

package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.MallFavoriteMapper;
import com.ruoyi.system.domain.MallFavorite;
import com.ruoyi.system.service.IMallFavoriteService;

/**
 * 商品收藏Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
@Service
public class MallFavoriteServiceImpl implements IMallFavoriteService 
{
    @Autowired
    private MallFavoriteMapper mallFavoriteMapper;

    /**
     * 查询商品收藏
     * 
     * @param favoriteId 商品收藏主键
     * @return 商品收藏
     */
    @Override
    public MallFavorite selectMallFavoriteByFavoriteId(Long favoriteId)
    {
        return mallFavoriteMapper.selectMallFavoriteByFavoriteId(favoriteId);
    }

    /**
     * 查询商品收藏列表
     * 
     * @param mallFavorite 商品收藏
     * @return 商品收藏
     */
    @Override
    public List<MallFavorite> selectMallFavoriteList(MallFavorite mallFavorite)
    {
        return mallFavoriteMapper.selectMallFavoriteList(mallFavorite);
    }

    /**
     * 新增商品收藏
     * 
     * @param mallFavorite 商品收藏
     * @return 结果
     */
    @Override
    public int insertMallFavorite(MallFavorite mallFavorite)
    {
        mallFavorite.setCreateTime(DateUtils.getNowDate());
        return mallFavoriteMapper.insertMallFavorite(mallFavorite);
    }

    /**
     * 修改商品收藏
     * 
     * @param mallFavorite 商品收藏
     * @return 结果
     */
    @Override
    public int updateMallFavorite(MallFavorite mallFavorite)
    {
        return mallFavoriteMapper.updateMallFavorite(mallFavorite);
    }

    /**
     * 批量删除商品收藏
     * 
     * @param favoriteIds 需要删除的商品收藏主键
     * @return 结果
     */
    @Override
    public int deleteMallFavoriteByFavoriteIds(Long[] favoriteIds)
    {
        return mallFavoriteMapper.deleteMallFavoriteByFavoriteIds(favoriteIds);
    }

    /**
     * 删除商品收藏信息
     * 
     * @param favoriteId 商品收藏主键
     * @return 结果
     */
    @Override
    public int deleteMallFavoriteByFavoriteId(Long favoriteId)
    {
        return mallFavoriteMapper.deleteMallFavoriteByFavoriteId(favoriteId);
    }
}

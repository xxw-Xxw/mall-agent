package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MallFavorite;

/**
 * 商品收藏Service接口
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
public interface IMallFavoriteService 
{
    /**
     * 查询商品收藏
     * 
     * @param favoriteId 商品收藏主键
     * @return 商品收藏
     */
    public MallFavorite selectMallFavoriteByFavoriteId(Long favoriteId);

    /**
     * 查询商品收藏列表
     * 
     * @param mallFavorite 商品收藏
     * @return 商品收藏集合
     */
    public List<MallFavorite> selectMallFavoriteList(MallFavorite mallFavorite);

    /**
     * 新增商品收藏
     * 
     * @param mallFavorite 商品收藏
     * @return 结果
     */
    public int insertMallFavorite(MallFavorite mallFavorite);

    /**
     * 修改商品收藏
     * 
     * @param mallFavorite 商品收藏
     * @return 结果
     */
    public int updateMallFavorite(MallFavorite mallFavorite);

    /**
     * 批量删除商品收藏
     * 
     * @param favoriteIds 需要删除的商品收藏主键集合
     * @return 结果
     */
    public int deleteMallFavoriteByFavoriteIds(Long[] favoriteIds);

    /**
     * 删除商品收藏信息
     * 
     * @param favoriteId 商品收藏主键
     * @return 结果
     */
    public int deleteMallFavoriteByFavoriteId(Long favoriteId);
}

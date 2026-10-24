package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MallFavorite;

/**
 * 商品收藏Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
public interface MallFavoriteMapper 
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
     * 删除商品收藏
     * 
     * @param favoriteId 商品收藏主键
     * @return 结果
     */
    public int deleteMallFavoriteByFavoriteId(Long favoriteId);

    /**
     * 批量删除商品收藏
     * 
     * @param favoriteIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMallFavoriteByFavoriteIds(Long[] favoriteIds);

    /**
     * 按商品聚合的收藏热度统计（收藏人数/最近收藏时间）
     *
     * @param goodsName 商品名称关键词
     * @return 商品维度聚合列表
     */
    public List<Map<String, Object>> selectFavoriteStatsList(@Param("goodsName") String goodsName);
}

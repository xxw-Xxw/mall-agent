package com.ruoyi.system.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户端收藏项（含商品信息）
 *
 * @author ruoyi
 */
public class StoreFavoriteVO
{
    /** 收藏ID */
    private Long favoriteId;

    /** 商品ID */
    private Long goodsId;

    /** 商品名称 */
    private String goodsName;

    /** 单价 */
    private BigDecimal price;

    /** 商品主图 */
    private String imageUrl;

    /** 库存 */
    private Long stock;

    /** 收藏时间 */
    private Date favTime;

    public Long getFavoriteId()
    {
        return favoriteId;
    }

    public void setFavoriteId(Long favoriteId)
    {
        this.favoriteId = favoriteId;
    }

    public Long getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(Long goodsId)
    {
        this.goodsId = goodsId;
    }

    public String getGoodsName()
    {
        return goodsName;
    }

    public void setGoodsName(String goodsName)
    {
        this.goodsName = goodsName;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public Long getStock()
    {
        return stock;
    }

    public void setStock(Long stock)
    {
        this.stock = stock;
    }

    public Date getFavTime()
    {
        return favTime;
    }

    public void setFavTime(Date favTime)
    {
        this.favTime = favTime;
    }
}

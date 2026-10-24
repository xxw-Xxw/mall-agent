package com.ruoyi.system.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户端浏览记录（含商品信息）
 *
 * @author ruoyi
 */
public class StoreHistoryVO
{
    /** 记录ID */
    private Long logId;

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

    /** 商品状态（0在售 1下架） */
    private String status;

    /** 浏览时间 */
    private Date browseTime;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getBrowseTime()
    {
        return browseTime;
    }

    public void setBrowseTime(Date browseTime)
    {
        this.browseTime = browseTime;
    }
}

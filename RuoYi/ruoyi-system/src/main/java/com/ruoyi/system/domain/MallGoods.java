package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 商城商品对象 mall_goods
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
public class MallGoods extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Long goodsId;

    /** 分类ID */
    @Excel(name = "分类ID")
    private Long categoryId;

    /** 商品名称 */
    @Excel(name = "商品名称")
    private String goodsName;

    /** 品牌 */
    @Excel(name = "品牌")
    private String brand;

    /** 价格（元） */
    @Excel(name = "价格", readConverterExp = "元=")
    private BigDecimal price;

    /** 库存数量 */
    @Excel(name = "库存数量")
    private Long stock;

    /** 销量 */
    @Excel(name = "销量")
    private Long sales;

    /** 商品主图URL */
    @Excel(name = "商品主图URL")
    private String imageUrl;

    /** 商品描述（导购AI知识源） */
    @Excel(name = "商品描述", readConverterExp = "导=购AI知识源")
    private String description;

    /** 商品标签（逗号分隔，画像数据源） */
    @Excel(name = "商品标签", readConverterExp = "逗=号分隔，画像数据源")
    private String tags;

    /** 商品状态（0在售 1下架） */
    @Excel(name = "商品状态", readConverterExp = "0=在售,1=下架")
    private String status;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    public void setGoodsId(Long goodsId) 
    {
        this.goodsId = goodsId;
    }

    public Long getGoodsId() 
    {
        return goodsId;
    }

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }

    public void setGoodsName(String goodsName) 
    {
        this.goodsName = goodsName;
    }

    public String getGoodsName() 
    {
        return goodsName;
    }

    public void setBrand(String brand) 
    {
        this.brand = brand;
    }

    public String getBrand() 
    {
        return brand;
    }

    public void setPrice(BigDecimal price) 
    {
        this.price = price;
    }

    public BigDecimal getPrice() 
    {
        return price;
    }

    public void setStock(Long stock) 
    {
        this.stock = stock;
    }

    public Long getStock() 
    {
        return stock;
    }

    public void setSales(Long sales) 
    {
        this.sales = sales;
    }

    public Long getSales() 
    {
        return sales;
    }

    public void setImageUrl(String imageUrl) 
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() 
    {
        return imageUrl;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setTags(String tags) 
    {
        this.tags = tags;
    }

    public String getTags() 
    {
        return tags;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("goodsId", getGoodsId())
            .append("categoryId", getCategoryId())
            .append("goodsName", getGoodsName())
            .append("brand", getBrand())
            .append("price", getPrice())
            .append("stock", getStock())
            .append("sales", getSales())
            .append("imageUrl", getImageUrl())
            .append("description", getDescription())
            .append("tags", getTags())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

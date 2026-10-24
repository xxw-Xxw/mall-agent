package com.ruoyi.system.domain.vo;

import com.ruoyi.system.domain.MallOrderItem;

/**
 * 用户端订单明细（补充商品主图）
 *
 * @author ruoyi
 */
public class StoreOrderItemVO extends MallOrderItem
{
    private static final long serialVersionUID = 1L;

    /** 商品主图 */
    private String imageUrl;

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }
}

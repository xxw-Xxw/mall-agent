package com.ruoyi.system.domain.dto;

/**
 * 用户端下单请求
 *
 * cartIds 非空：从购物车结算；否则按 goodsId + quantity 直接购买
 *
 * @author ruoyi
 */
public class StoreOrderDTO
{
    /** 购物车项ID集合（从购物车结算时必填） */
    private Long[] cartIds;

    /** 商品ID（直接购买时必填） */
    private Long goodsId;

    /** 购买数量（直接购买时可选，默认1） */
    private Long quantity;

    /** 订单备注 */
    private String remark;

    public Long[] getCartIds()
    {
        return cartIds;
    }

    public void setCartIds(Long[] cartIds)
    {
        this.cartIds = cartIds;
    }

    public Long getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(Long goodsId)
    {
        this.goodsId = goodsId;
    }

    public Long getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Long quantity)
    {
        this.quantity = quantity;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}

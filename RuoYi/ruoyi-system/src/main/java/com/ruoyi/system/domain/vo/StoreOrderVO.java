package com.ruoyi.system.domain.vo;

import java.util.List;
import com.ruoyi.system.domain.MallOrder;

/**
 * 用户端订单视图（含订单明细）
 *
 * @author ruoyi
 */
public class StoreOrderVO extends MallOrder
{
    private static final long serialVersionUID = 1L;

    /** 订单明细 */
    private List<StoreOrderItemVO> items;

    public List<StoreOrderItemVO> getItems()
    {
        return items;
    }

    public void setItems(List<StoreOrderItemVO> items)
    {
        this.items = items;
    }
}

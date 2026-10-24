package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.MallOrderItem;

/**
 * 订单明细Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
public interface MallOrderItemMapper 
{
    /**
     * 查询订单明细
     * 
     * @param itemId 订单明细主键
     * @return 订单明细
     */
    public MallOrderItem selectMallOrderItemByItemId(Long itemId);

    /**
     * 查询订单明细列表
     * 
     * @param mallOrderItem 订单明细
     * @return 订单明细集合
     */
    public List<MallOrderItem> selectMallOrderItemList(MallOrderItem mallOrderItem);

    /**
     * 新增订单明细
     * 
     * @param mallOrderItem 订单明细
     * @return 结果
     */
    public int insertMallOrderItem(MallOrderItem mallOrderItem);

    /**
     * 修改订单明细
     * 
     * @param mallOrderItem 订单明细
     * @return 结果
     */
    public int updateMallOrderItem(MallOrderItem mallOrderItem);

    /**
     * 删除订单明细
     * 
     * @param itemId 订单明细主键
     * @return 结果
     */
    public int deleteMallOrderItemByItemId(Long itemId);

    /**
     * 批量删除订单明细
     * 
     * @param itemIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMallOrderItemByItemIds(Long[] itemIds);
}

package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.MallOrderItemMapper;
import com.ruoyi.system.domain.MallOrderItem;
import com.ruoyi.system.service.IMallOrderItemService;

/**
 * 订单明细Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
@Service
public class MallOrderItemServiceImpl implements IMallOrderItemService 
{
    @Autowired
    private MallOrderItemMapper mallOrderItemMapper;

    /**
     * 查询订单明细
     * 
     * @param itemId 订单明细主键
     * @return 订单明细
     */
    @Override
    public MallOrderItem selectMallOrderItemByItemId(Long itemId)
    {
        return mallOrderItemMapper.selectMallOrderItemByItemId(itemId);
    }

    /**
     * 查询订单明细列表
     * 
     * @param mallOrderItem 订单明细
     * @return 订单明细
     */
    @Override
    public List<MallOrderItem> selectMallOrderItemList(MallOrderItem mallOrderItem)
    {
        return mallOrderItemMapper.selectMallOrderItemList(mallOrderItem);
    }

    /**
     * 新增订单明细
     * 
     * @param mallOrderItem 订单明细
     * @return 结果
     */
    @Override
    public int insertMallOrderItem(MallOrderItem mallOrderItem)
    {
        return mallOrderItemMapper.insertMallOrderItem(mallOrderItem);
    }

    /**
     * 修改订单明细
     * 
     * @param mallOrderItem 订单明细
     * @return 结果
     */
    @Override
    public int updateMallOrderItem(MallOrderItem mallOrderItem)
    {
        return mallOrderItemMapper.updateMallOrderItem(mallOrderItem);
    }

    /**
     * 批量删除订单明细
     * 
     * @param itemIds 需要删除的订单明细主键
     * @return 结果
     */
    @Override
    public int deleteMallOrderItemByItemIds(Long[] itemIds)
    {
        return mallOrderItemMapper.deleteMallOrderItemByItemIds(itemIds);
    }

    /**
     * 删除订单明细信息
     * 
     * @param itemId 订单明细主键
     * @return 结果
     */
    @Override
    public int deleteMallOrderItemByItemId(Long itemId)
    {
        return mallOrderItemMapper.deleteMallOrderItemByItemId(itemId);
    }
}

package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MallOrder;

/**
 * 订单Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-23
 */
public interface MallOrderMapper 
{
    /**
     * 查询订单
     * 
     * @param orderId 订单主键
     * @return 订单
     */
    public MallOrder selectMallOrderByOrderId(Long orderId);

    /**
     * 查询订单列表
     * 
     * @param mallOrder 订单
     * @return 订单集合
     */
    public List<MallOrder> selectMallOrderList(MallOrder mallOrder);

    /**
     * 新增订单
     * 
     * @param mallOrder 订单
     * @return 结果
     */
    public int insertMallOrder(MallOrder mallOrder);

    /**
     * 修改订单
     * 
     * @param mallOrder 订单
     * @return 结果
     */
    public int updateMallOrder(MallOrder mallOrder);

    /**
     * 删除订单
     * 
     * @param orderId 订单主键
     * @return 结果
     */
    public int deleteMallOrderByOrderId(Long orderId);

    /**
     * 批量删除订单
     *
     * @param orderIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMallOrderByOrderIds(Long[] orderIds);

    /**
     * 订单处理工作台：按状态查询订单（联查会员昵称、件数）
     */
    public List<MallOrder> selectProcessOrderList(MallOrder mallOrder);

    /**
     * 发货：待发货(1) → 已发货(2)，条件更新防并发重复发货
     */
    public int shipOrder(@Param("orderId") Long orderId, @Param("operName") String operName);
}

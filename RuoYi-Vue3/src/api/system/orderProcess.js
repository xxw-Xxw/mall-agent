import request from '@/utils/request'

// 待发货订单列表
export function listPending(query) {
  return request({
    url: '/system/orderProcess/pending',
    method: 'get',
    params: query
  })
}

// 已发货订单列表
export function listShipped(query) {
  return request({
    url: '/system/orderProcess/shipped',
    method: 'get',
    params: query
  })
}

// 单个订单发货
export function shipOrder(orderId) {
  return request({
    url: '/system/orderProcess/ship/' + orderId,
    method: 'put'
  })
}

// 订单商品明细
export function getOrderItems(orderId) {
  return request({
    url: '/system/orderProcess/items/' + orderId,
    method: 'get'
  })
}

// 批量发货
export function shipOrders(orderIds) {
  return request({
    url: '/system/orderProcess/shipBatch/' + orderIds,
    method: 'put'
  })
}

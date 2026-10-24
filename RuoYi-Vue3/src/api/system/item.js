import request from '@/utils/request'

// 查询订单明细列表
export function listItem(query) {
  return request({
    url: '/system/item/list',
    method: 'get',
    params: query
  })
}

// 查询订单明细详细
export function getItem(itemId) {
  return request({
    url: '/system/item/' + itemId,
    method: 'get'
  })
}

// 新增订单明细
export function addItem(data) {
  return request({
    url: '/system/item',
    method: 'post',
    data: data
  })
}

// 修改订单明细
export function updateItem(data) {
  return request({
    url: '/system/item',
    method: 'put',
    data: data
  })
}

// 删除订单明细
export function delItem(itemId) {
  return request({
    url: '/system/item/' + itemId,
    method: 'delete'
  })
}

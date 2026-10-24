import request from '@/utils/request'

// 获取商城运营控制台统计数据
export function getDashboardStats() {
  return request({
    url: '/mall/dashboard/stats',
    method: 'get'
  })
}

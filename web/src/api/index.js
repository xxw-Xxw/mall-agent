import request from './request'

// ---------- 认证（商城会员体系，与管理后台账号分离） ----------
export function login(data) {
  return request.post('/store/auth/login', data)
}

export function register(data) {
  return request.post('/store/auth/register', data)
}

export function logout() {
  return request.post('/store/auth/logout')
}

export function getInfo() {
  return request.get('/store/auth/info')
}

// ---------- 匿名浏览 ----------
export function getCategories() {
  return request.get('/store/categories')
}

export function getGoodsList(params) {
  return request.get('/store/goods/list', { params })
}

export function getBrands() {
  return request.get('/store/goods/brands')
}

export function getGoodsDetail(goodsId) {
  return request.get(`/store/goods/${goodsId}`)
}

// ---------- 购物车 ----------
export function addCart(goodsId, quantity) {
  return request.post('/store/cart', null, { params: { goodsId, quantity } })
}

export function getCartList() {
  return request.get('/store/cart/list')
}

export function updateCartQuantity(cartId, quantity) {
  return request.put(`/store/cart/${cartId}`, null, { params: { quantity } })
}

export function removeCarts(cartIds) {
  return request.delete(`/store/cart/${cartIds.join(',')}`)
}

// ---------- 收藏 ----------
export function toggleFavorite(goodsId) {
  return request.post(`/store/favorites/${goodsId}`)
}

export function getFavoriteList() {
  return request.get('/store/favorites/list')
}

// ---------- 订单 ----------
export function createOrder(data) {
  return request.post('/store/orders', data)
}

export function getOrderList(params) {
  return request.get('/store/orders/list', { params })
}

export function getOrderDetail(orderId) {
  return request.get(`/store/orders/${orderId}`)
}

export function confirmOrder(orderId) {
  return request.put(`/store/orders/${orderId}/confirm`)
}

export function cancelOrder(orderId) {
  return request.put(`/store/orders/${orderId}/cancel`)
}

// ---------- 浏览记录 ----------
export function getHistoryList(params) {
  return request.get('/store/history/list', { params })
}

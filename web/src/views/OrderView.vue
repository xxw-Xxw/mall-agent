<script setup>
import { ref, onMounted } from 'vue'
import { getOrderList, confirmOrder, cancelOrder } from '@/api'
import toast from '@/utils/toast'

const tabs = [
  { label: '全部订单', value: '' },
  { label: '待发货', value: '1' },
  { label: '已发货', value: '2' },
  { label: '已完成', value: '3' },
  { label: '已取消', value: '4' }
]
const statusText = { '0': '待付款', '1': '待发货', '2': '已发货', '3': '已完成', '4': '已取消' }
const statusClass = { '0': 'st-wait', '1': 'st-wait', '2': 'st-ship', '3': 'st-done', '4': 'st-cancel' }

const activeTab = ref('')
const orders = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getOrderList({ pageNum: 1, pageSize: 20, status: activeTab.value || undefined })
    orders.value = res.rows || []
  } finally {
    loading.value = false
  }
}

function switchTab(value) {
  activeTab.value = value
  load()
}

function fmtTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

async function doConfirm(order) {
  if (!confirm('确认已收到货？')) return
  try {
    await confirmOrder(order.orderId)
    toast.success('已确认收货')
    load()
  } catch (e) {
    toast.error(e.message)
  }
}

async function doCancel(order) {
  if (!confirm('确定取消该订单吗？')) return
  try {
    await cancelOrder(order.orderId)
    toast.success('订单已取消')
    load()
  } catch (e) {
    toast.error(e.message)
  }
}

onMounted(load)
</script>

<template>
  <div class="container">
    <div class="page-head">
      <h1>我的订单</h1>
      <div class="sub">查看和管理您的订单</div>
    </div>

    <div class="card tabs">
      <span
        v-for="tab in tabs"
        :key="tab.value"
        class="tab"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >{{ tab.label }}</span>
    </div>

    <div v-if="loading" class="loading-tip" style="padding-top:60px">加载中...</div>
    <div v-else-if="!orders.length" class="empty card">
      <span class="icon">📦</span>
      暂无订单
    </div>

    <div v-for="order in orders" :key="order.orderId" class="order card">
      <div class="o-head">
        <span class="o-no">订单号 {{ order.orderNo }}</span>
        <span class="o-time">{{ fmtTime(order.createTime) }}</span>
        <span class="o-status" :class="statusClass[order.status]">{{ statusText[order.status] }}</span>
      </div>
      <div v-for="item in order.items" :key="item.itemId" class="o-item">
        <img :src="item.imageUrl" />
        <div class="oi-info">
          <div class="oi-name">{{ item.goodsName }}</div>
          <div class="oi-meta">
            <span class="price"><span class="rmb">¥</span>{{ item.price }}</span>
            <span class="oi-qty">x{{ item.quantity }}</span>
          </div>
        </div>
      </div>
      <div class="o-foot">
        <span class="o-total">共 {{ order.items.reduce((s, i) => s + i.quantity, 0) }} 件 · 合计
          <span class="price"><span class="rmb">¥</span>{{ order.totalAmount }}</span>
        </span>
        <template v-if="order.status === '1'">
          <button class="btn-plain small" @click="doCancel(order)">取消订单</button>
        </template>
        <button v-else-if="order.status === '2'" class="btn-primary small" @click="doConfirm(order)">确认收货</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tabs {
  display: flex;
  margin-bottom: 18px;
  overflow: hidden;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 14px 0;
  font-size: 14px;
  color: var(--text-sub);
  cursor: pointer;
  position: relative;
  transition: color 0.18s;
}

.tab:hover {
  color: var(--primary);
}

.tab.active {
  color: var(--primary);
  font-weight: 600;
}

.tab.active::after {
  content: '';
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  bottom: 0;
  width: 40px;
  height: 3px;
  border-radius: 2px;
  background: var(--primary);
}

.order {
  padding: 20px 28px;
  margin-bottom: 16px;
}

.o-head {
  display: flex;
  align-items: center;
  gap: 18px;
  font-size: 13px;
  color: var(--text-sub);
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
}

.o-time {
  flex: 1;
}

.o-status {
  font-weight: 600;
}

.o-status.st-wait {
  color: #ff9a2e;
}

.o-status.st-ship {
  color: #409eff;
}

.o-status.st-done {
  color: #37c079;
}

.o-status.st-cancel {
  color: #9aa0aa;
}

.o-item {
  display: flex;
  gap: 14px;
  padding: 14px 0;
}

.o-item img {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
  background: #f2f3f5;
}

.oi-info {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.oi-name {
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.oi-meta {
  display: flex;
  align-items: baseline;
  gap: 16px;
  font-size: 13px;
}

.oi-qty {
  color: var(--text-sub);
}

.o-foot {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

.o-total {
  font-size: 13px;
  color: var(--text-sub);
  margin-right: auto;
}

.o-total .price {
  font-size: 18px;
}

.btn-plain.small,
.btn-primary.small {
  height: 34px;
  padding: 0 18px;
  font-size: 13px;
}
</style>

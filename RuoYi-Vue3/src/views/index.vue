<template>
  <div class="app-container dashboard">
    <div class="dash-head">
      <div>
        <div class="dash-title">橙心优选商城 · 运营控制台</div>
        <div class="dash-sub">{{ todayText }}，欢迎回来，{{ userName }}</div>
      </div>
      <el-button type="primary" plain icon="Refresh" :loading="loading" @click="loadStats">刷新数据</el-button>
    </div>

    <el-row :gutter="16" class="mt16">
      <el-col v-for="c in coreCards" :key="c.label" :xs="12" :sm="12" :md="6">
        <div class="stat-card" :class="{ clickable: !!c.to }" @click="go(c)">
          <div class="sc-top">
            <span class="sc-icon">{{ c.icon }}</span>
            <span class="sc-label">{{ c.label }}</span>
            <span v-if="c.badge" class="sc-badge">今日 +{{ c.badge }}</span>
          </div>
          <div class="sc-value" :style="c.color ? { color: c.color } : {}">{{ c.value }}</div>
          <div class="sc-foot">{{ c.foot }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col v-for="c in opsCards" :key="c.label" :xs="12" :sm="12" :md="6">
        <div class="stat-card ops" :class="{ clickable: !!c.to }" @click="go(c)">
          <div class="sc-top">
            <span class="sc-icon">{{ c.icon }}</span>
            <span class="sc-label">{{ c.label }}</span>
          </div>
          <div class="sc-value">{{ c.value }}</div>
          <div class="sc-foot">{{ c.foot }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :xs="24" :lg="14">
        <div class="panel">
          <div class="panel-title">近 7 日营业额趋势</div>
          <div v-loading="loading" ref="trendRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="10">
        <div class="panel">
          <div class="panel-title">订单状态占比</div>
          <div v-loading="loading" ref="pieRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :xs="24" :lg="12">
        <div class="panel">
          <div class="panel-title">热销商品 TOP5</div>
          <el-table :data="stats.hotGoods" v-loading="loading" size="default">
            <el-table-column label="排名" width="60" align="center">
              <template #default="scope">
                <span :class="['rank', scope.$index < 3 ? 'top' : '']">{{ scope.$index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="商品" min-width="200">
              <template #default="scope">
                <div class="goods-cell">
                  <el-image v-if="scope.row.imageUrl" :src="scope.row.imageUrl" fit="cover" class="goods-thumb" />
                  <span class="goods-name">{{ scope.row.goodsName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="价格" width="110" align="right">
              <template #default="scope">¥{{ scope.row.price }}</template>
            </el-table-column>
            <el-table-column label="销量" width="90" align="center">
              <template #default="scope">
                <span class="sales-num">{{ scope.row.sales }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="panel">
          <div class="panel-title">
            库存预警
            <el-button link type="primary" @click="router.push('/mall/goods')">管理商品</el-button>
          </div>
          <el-table :data="stats.lowStockGoods" v-loading="loading" size="default">
            <el-table-column label="商品" min-width="200">
              <template #default="scope">
                <div class="goods-cell">
                  <el-image v-if="scope.row.imageUrl" :src="scope.row.imageUrl" fit="cover" class="goods-thumb" />
                  <span class="goods-name">{{ scope.row.goodsName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="价格" width="110" align="right">
              <template #default="scope">¥{{ scope.row.price }}</template>
            </el-table-column>
            <el-table-column label="剩余库存" width="100" align="center">
              <template #default="scope">
                <span class="stock-warn">{{ scope.row.stock }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!loading && !stats.lowStockGoods.length" class="empty-tip">库存充足，暂无预警商品</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/mall/dashboard'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const stats = reactive({
  userTotal: 0, userToday: 0,
  revenueTotal: 0, revenueToday: 0,
  orderTotal: 0, orderToday: 0, orderPending: 0,
  goodsTotal: 0, categoryTotal: 0, lowStockCount: 0,
  revenueTrend: [], orderStatusDist: [], hotGoods: [], lowStockGoods: []
})

const trendRef = ref(null)
const pieRef = ref(null)
let trendChart = null
let pieChart = null

const userName = computed(() => userStore.name || '管理员')
const todayText = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

const money = (v) => '¥' + Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const num = (v) => Number(v || 0).toLocaleString('zh-CN')

const coreCards = computed(() => [
  { icon: '👥', label: '用户总数', value: num(stats.userTotal), badge: stats.userToday, foot: '含今日新增 ' + stats.userToday + ' 人', to: '/system/user' },
  { icon: '✨', label: '今日新增用户', value: num(stats.userToday), foot: '注册时间在今日的账号', to: '/system/user' },
  { icon: '💰', label: '总营业额', value: money(stats.revenueTotal), foot: '已完成订单金额合计', color: '#ff6b35', to: '/mall/order?status=1' },
  { icon: '📈', label: '今日营业额', value: money(stats.revenueToday), foot: '今日已完成订单金额', to: '/mall/order?status=1' }
])

const opsCards = computed(() => [
  { icon: '🧾', label: '订单总数', value: num(stats.orderTotal), badge: stats.orderToday, foot: '今日新增 ' + stats.orderToday + ' 单', to: '/mall/order' },
  { icon: '🚚', label: '待发货订单', value: num(stats.orderPending), foot: '员工发货工作台待处理', to: '/mall/orderProcess' },
  { icon: '📦', label: '在售商品', value: num(stats.goodsTotal), foot: '共 ' + stats.categoryTotal + ' 个商品分类', to: '/mall/goods' },
  { icon: '⚠️', label: '库存预警', value: num(stats.lowStockCount), foot: '库存低于 10 的在售商品', to: '/mall/goods' }
])

function go(card) {
  if (card.to) router.push(card.to)
}

function renderCharts() {
  if (trendRef.value) {
    if (!trendChart) trendChart = echarts.init(trendRef.value)
    trendChart.setOption({
      grid: { left: 50, right: 20, top: 30, bottom: 30 },
      tooltip: { trigger: 'axis', valueFormatter: (v) => money(v) },
      xAxis: { type: 'category', data: stats.revenueTrend.map((d) => d.date), boundaryGap: false },
      yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
      series: [{
        type: 'line', smooth: true, symbolSize: 8, data: stats.revenueTrend.map((d) => Number(d.amount)),
        lineStyle: { width: 3, color: '#ff6b35' },
        itemStyle: { color: '#ff6b35' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255,107,53,0.25)' },
            { offset: 1, color: 'rgba(255,107,53,0.02)' }
          ])
        }
      }]
    })
  }
  if (pieRef.value) {
    if (!pieChart) pieChart = echarts.init(pieRef.value)
    const colorMap = { 待付款: '#909399', 待发货: '#e6a23c', 已发货: '#409eff', 已完成: '#67c23a', 已取消: '#c0c4cc' }
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}：{c} 单（{d}%）' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'],
        label: { formatter: '{b} {d}%' },
        data: stats.orderStatusDist.map((d) => ({ name: d.name, value: d.value, itemStyle: { color: colorMap[d.name] } }))
      }]
    })
  }
}

function handleResize() {
  trendChart && trendChart.resize()
  pieChart && pieChart.resize()
}

async function loadStats() {
  loading.value = true
  try {
    const res = await getDashboardStats()
    Object.assign(stats, res.data)
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart && trendChart.dispose()
  pieChart && pieChart.dispose()
})
</script>

<style scoped>
.dashboard {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
  border-radius: 4px;
}

.dash-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.dash-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
}

.dash-sub {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}

.mt16 {
  margin-top: 0;
}

.mt16 + .mt16,
.dashboard .el-row + .el-row {
  margin-top: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px 20px;
  box-shadow: 0 1px 4px rgba(31, 45, 61, 0.06);
  margin-bottom: 16px;
  transition: all 0.2s;
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-card.clickable:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.12);
}

.sc-top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sc-icon {
  font-size: 22px;
}

.sc-label {
  font-size: 13px;
  color: #909399;
}

.sc-badge {
  font-size: 11px;
  color: #ff6b35;
  background: rgba(255, 107, 53, 0.1);
  border-radius: 10px;
  padding: 1px 8px;
}

.sc-value {
  margin-top: 12px;
  font-size: 28px;
  font-weight: 700;
  color: #1f2d3d;
  font-variant-numeric: tabular-nums;
}

.sc-foot {
  margin-top: 8px;
  font-size: 12px;
  color: #b0b6bf;
}

.panel {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px 20px;
  box-shadow: 0 1px 4px rgba(31, 45, 61, 0.06);
  margin-bottom: 16px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chart {
  height: 300px;
}

.goods-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.goods-thumb {
  width: 42px;
  height: 42px;
  border-radius: 6px;
  flex-shrink: 0;
}

.goods-name {
  color: #303133;
}

.rank {
  display: inline-block;
  width: 22px;
  height: 22px;
  line-height: 22px;
  border-radius: 50%;
  background: #f0f2f5;
  color: #909399;
  font-size: 12px;
  font-weight: 700;
}

.rank.top {
  background: #ff6b35;
  color: #fff;
}

.sales-num {
  color: #ff6b35;
  font-weight: 700;
}

.stock-warn {
  color: #f56c6c;
  font-weight: 700;
}

.empty-tip {
  padding: 30px 0;
  text-align: center;
  color: #b0b6bf;
  font-size: 13px;
}
</style>

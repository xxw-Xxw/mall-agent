<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHistoryList } from '@/api'

const router = useRouter()
const items = ref([])
const loading = ref(true)

function fmtTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(5, 16)
}

onMounted(async () => {
  try {
    const res = await getHistoryList({ pageNum: 1, pageSize: 30 })
    items.value = res.rows || []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="container">
    <div class="page-head">
      <h1>浏览足迹</h1>
      <div class="sub">回顾您最近看过的商品</div>
    </div>

    <div v-if="loading" class="loading-tip" style="padding-top:60px">加载中...</div>
    <div v-else-if="!items.length" class="empty card">
      <span class="icon">🕘</span>
      暂无浏览记录，去逛逛吧
    </div>

    <div v-else class="goods-grid">
      <div v-for="item in items" :key="item.logId" class="goods-item" @click="router.push(`/detail/${item.goodsId}`)">
        <img :src="item.imageUrl" />
        <div class="info">
          <div class="name">{{ item.goodsName }}</div>
          <div class="meta">
            <span class="price"><span class="rmb">¥</span>{{ item.price }}</span>
            <span class="sales">{{ fmtTime(item.browseTime) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

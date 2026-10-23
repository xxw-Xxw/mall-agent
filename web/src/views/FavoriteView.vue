<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavoriteList, toggleFavorite } from '@/api'
import toast from '@/utils/toast'

const router = useRouter()
const items = ref([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    const res = await getFavoriteList()
    items.value = res.data || []
  } finally {
    loading.value = false
  }
}

function fmtTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 10)
}

async function remove(item) {
  try {
    const res = await toggleFavorite(item.goodsId)
    if (!res.data.isFavorite) {
      items.value = items.value.filter((i) => i.favoriteId !== item.favoriteId)
      toast.success('已取消收藏')
    }
  } catch (e) {
    toast.error(e.message)
  }
}

onMounted(load)
</script>

<template>
  <div class="container">
    <div class="page-head">
      <h1>我的收藏</h1>
      <div class="sub">共 {{ items.length }} 件心仪好物</div>
    </div>

    <div v-if="loading" class="loading-tip" style="padding-top:60px">加载中...</div>
    <div v-else-if="!items.length" class="empty card">
      <span class="icon">❤️</span>
      还没有收藏的商品，去逛逛吧
    </div>

    <div v-else class="goods-grid">
      <div v-for="item in items" :key="item.favoriteId" class="fav-card card">
        <img :src="item.imageUrl" @click="router.push(`/detail/${item.goodsId}`)" />
        <div class="info">
          <div class="fi-name" @click="router.push(`/detail/${item.goodsId}`)">{{ item.goodsName }}</div>
          <div class="fi-meta">
            <span class="price"><span class="rmb">¥</span>{{ item.price }}</span>
            <span class="fi-time">{{ fmtTime(item.favTime) }}</span>
          </div>
          <button class="btn-plain small" @click="remove(item)">取消收藏</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fav-card {
  overflow: hidden;
  cursor: default;
  transition: all 0.2s;
}

.fav-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.fav-card img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  display: block;
  background: #f2f3f5;
  cursor: pointer;
}

.info {
  padding: 12px 14px 14px;
}

.fi-name {
  font-size: 14px;
  line-height: 1.4;
  height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  cursor: pointer;
}

.fav-card:hover .fi-name {
  color: var(--primary);
}

.fi-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 8px;
}

.fi-time {
  font-size: 12px;
  color: var(--text-sub);
}

.info .btn-plain {
  width: 100%;
  height: 32px;
  margin-top: 12px;
  font-size: 13px;
}
</style>

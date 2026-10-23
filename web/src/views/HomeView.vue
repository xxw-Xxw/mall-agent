<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, getGoodsList } from '@/api'
import GoodsCard from '@/components/GoodsCard.vue'
import HomeBanner from '@/components/HomeBanner.vue'
import { categoryIcon } from '@/utils/categoryIcon'

const router = useRouter()
const categories = ref([])
const hotGoods = ref([])

function goCategory(categoryId, name) {
  router.push({ name: 'list', query: { categoryId, name } })
}

onMounted(async () => {
  const [catRes, goodsRes] = await Promise.all([
    getCategories(),
    getGoodsList({ pageNum: 1, pageSize: 8, sortField: 'sales' })
  ])
  categories.value = catRes.data || []
  hotGoods.value = goodsRes.rows || []
})
</script>

<template>
  <div class="container">
    <HomeBanner />

    <div class="card cat-card">
      <div class="cat-grid">
        <div v-for="cat in categories" :key="cat.categoryId" class="cat-item" @click="goCategory(cat.categoryId, cat.categoryName)">
          <span class="cat-icon">{{ categoryIcon(cat.categoryName) }}</span>
          <span class="cat-name">{{ cat.categoryName }}</span>
        </div>
        <div class="cat-item more" @click="router.push('/categories')">
          <span class="cat-icon">☰</span>
          <span class="cat-name">更多分类</span>
        </div>
      </div>
    </div>

    <div class="sec-head">
      <span class="title">🔥 热销好物</span>
      <span class="more" @click="router.push('/list')">查看全部 →</span>
    </div>
    <div class="goods-grid">
      <GoodsCard v-for="goods in hotGoods" :key="goods.goodsId" :goods="goods" />
    </div>
  </div>
</template>

<style scoped>
.cat-card {
  margin-top: 20px;
  padding: 18px 10px;
}

.cat-grid {
  display: grid;
  grid-template-columns: repeat(11, 1fr);
  gap: 4px;
}

.cat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  cursor: pointer;
  border-radius: 10px;
  padding: 6px 0;
  transition: background 0.18s;
}

.cat-item:hover {
  background: var(--primary-soft);
}

.cat-icon {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: var(--primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 23px;
  transition: all 0.18s;
}

.cat-item:hover .cat-icon {
  transform: scale(1.08);
}

.cat-name {
  font-size: 12px;
  color: var(--text);
  white-space: nowrap;
}

.cat-item.more .cat-icon {
  background: #fff;
  border: 1.5px dashed var(--primary);
  font-size: 18px;
  color: var(--primary);
}

.cat-item.more .cat-name {
  color: var(--primary);
  font-weight: 700;
}

@media (max-width: 900px) {
  .cat-grid {
    display: flex;
    overflow-x: auto;
    gap: 14px;
    padding-bottom: 6px;
    scrollbar-width: thin;
  }

  .cat-item {
    flex-shrink: 0;
    min-width: 64px;
  }
}
</style>

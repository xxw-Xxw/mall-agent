<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories, getGoodsList, getBrands } from '@/api'
import GoodsCard from '@/components/GoodsCard.vue'

const route = useRoute()

const categories = ref([])
const activeCategory = ref(null)
const keyword = ref(route.query.keyword || '')
const sortField = ref('')
const priceOrder = ref('asc')
const goods = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 20
const loading = ref(false)
const finished = ref(false)

const brands = ref([])
const activeBrand = ref('')
const brandOpen = ref(false)
const brandKeyword = ref('')

const sortOptions = [
  { label: '综合', value: '' },
  { label: '销量', value: 'sales' },
  { label: '价格', value: 'price' },
  { label: '最新', value: 'new' }
]

const filteredBrands = computed(() => {
  const kw = brandKeyword.value.trim().toLowerCase()
  if (!kw) return brands.value
  return brands.value.filter((b) => String(b).toLowerCase().includes(kw))
})

const activeTopId = computed(() => {
  const cid = activeCategory.value
  if (cid == null) return null
  for (const g of categories.value) {
    if (g.categoryId === cid) return cid
    for (const c of g.children || []) {
      if (c.categoryId === cid) return g.categoryId
    }
  }
  return null
})

const topName = computed(() => {
  const g = categories.value.find((c) => c.categoryId === activeTopId.value)
  return (g && g.categoryName) || ''
})

const subCategories = computed(() => {
  const g = categories.value.find((c) => c.categoryId === activeTopId.value)
  return (g && g.children) || []
})

function resolveSort(value) {
  if (value === 'sales' || value === 'new') return { sortField: value, order: 'desc' }
  if (value === 'price') return { sortField: 'price', order: priceOrder.value }
  return {}
}

async function loadGoods(reset) {
  if (loading.value) return
  if (reset) {
    pageNum.value = 1
    finished.value = false
    goods.value = []
  }
  if (finished.value) return
  loading.value = true
  try {
    const { sortField: sf, order } = resolveSort(sortField.value)
    const res = await getGoodsList({
      pageNum: pageNum.value,
      pageSize,
      categoryId: activeCategory.value || undefined,
      keyword: keyword.value || undefined,
      brand: activeBrand.value || undefined,
      sortField: sf,
      order
    })
    total.value = res.total || 0
    goods.value.push(...(res.rows || []))
    if (goods.value.length >= total.value) finished.value = true
    else pageNum.value++
  } finally {
    loading.value = false
  }
}

function switchCategory(categoryId) {
  if (activeCategory.value === categoryId) return
  activeCategory.value = categoryId
  loadGoods(true)
}

function switchSort(value) {
  if (value === 'price') {
    if (sortField.value !== 'price') {
      sortField.value = 'price'
      priceOrder.value = 'asc'
    } else {
      priceOrder.value = priceOrder.value === 'asc' ? 'desc' : 'asc'
    }
    loadGoods(true)
    return
  }
  if (sortField.value === value) return
  sortField.value = value
  loadGoods(true)
}

function selectBrand(b) {
  activeBrand.value = b || ''
  brandOpen.value = false
  brandKeyword.value = ''
  loadGoods(true)
}

function clearKeyword() {
  keyword.value = ''
  loadGoods(true)
}

function onDocClick() {
  brandOpen.value = false
}

onMounted(async () => {
  document.addEventListener('click', onDocClick)
  const catRes = await getCategories()
  categories.value = [{ categoryId: null, categoryName: '全部商品' }, ...(catRes.data || [])]
  const cid = Number(route.query.categoryId)
  if (cid) activeCategory.value = cid
  if (route.query.sortField) sortField.value = String(route.query.sortField)
  getBrands().then((res) => {
    brands.value = res.data || []
  })
  loadGoods(true)
})

onUnmounted(() => {
  document.removeEventListener('click', onDocClick)
})
</script>

<template>
  <div class="container">
    <div class="list-layout">
      <aside class="side card">
        <div class="side-title">全部分类</div>
        <div
          v-for="cat in categories"
          :key="cat.categoryId ?? 'all'"
          class="side-item"
          :class="{ active: activeTopId === cat.categoryId }"
          @click="switchCategory(cat.categoryId)"
        >{{ cat.categoryName }}</div>
      </aside>

      <section class="main-area">
        <div class="toolbar card">
          <span
            v-for="opt in sortOptions"
            :key="opt.value"
            class="sort-item"
            :class="{ active: sortField === opt.value }"
            @click="switchSort(opt.value)"
          >
            {{ opt.label }}<i v-if="opt.value === 'price' && sortField === 'price'" class="sort-arrow">{{ priceOrder === 'asc' ? '↑' : '↓' }}</i>
          </span>

          <div class="brand-filter" @click.stop="brandOpen = !brandOpen">
            <span class="brand-btn" :class="{ active: !!activeBrand }">
              品牌{{ activeBrand ? '：' + activeBrand : '' }}<i class="brand-caret" :class="{ up: brandOpen }">▾</i>
            </span>
            <div v-if="brandOpen" class="brand-panel card" @click.stop>
              <div class="brand-search">
                <input v-model="brandKeyword" type="text" placeholder="搜索品牌" />
              </div>
              <div class="brand-list">
                <span class="brand-item" :class="{ on: !activeBrand }" @click="selectBrand(null)">全部品牌</span>
                <span
                  v-for="b in filteredBrands"
                  :key="b"
                  class="brand-item"
                  :class="{ on: activeBrand === b }"
                  @click="selectBrand(b)"
                >{{ b }}</span>
              </div>
            </div>
          </div>

          <div class="tool-right">
            <span v-if="keyword" class="kw-chip">
              “{{ keyword }}”
              <i class="kw-clear" @click="clearKeyword">×</i>
            </span>
            <span class="count">共 {{ total }} 件商品</span>
          </div>
        </div>

        <div v-if="subCategories.length" class="sub-bar card">
          <span class="sub-label">{{ topName }}</span>
          <span
            class="sub-chip"
            :class="{ on: activeCategory === activeTopId }"
            @click="switchCategory(activeTopId)"
          >全部</span>
          <span
            v-for="c in subCategories"
            :key="c.categoryId"
            class="sub-chip"
            :class="{ on: activeCategory === c.categoryId }"
            @click="switchCategory(c.categoryId)"
          >{{ c.categoryName }}</span>
        </div>

        <div class="goods-grid list-grid">
          <GoodsCard v-for="g in goods" :key="g.goodsId" :goods="g" />
        </div>

        <div v-if="!goods.length && !loading" class="empty">
          <span class="icon">🛍️</span>
          没有找到相关商品
        </div>
        <div v-if="loading" class="loading-tip">加载中...</div>
        <div v-else-if="!finished && goods.length" class="load-more" @click="loadGoods()">点击加载更多</div>
        <div v-else-if="finished && goods.length" class="loading-tip">没有更多了</div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.list-layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 20px;
  align-items: start;
}

.side {
  padding: 14px 0;
  position: sticky;
  top: 96px;
}

.side-title {
  font-size: 15px;
  font-weight: 700;
  padding: 6px 20px 12px;
  border-bottom: 1px solid var(--line);
}

.side-item {
  padding: 12px 20px;
  font-size: 14px;
  color: var(--text-sub);
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all 0.18s;
}

.side-item:hover {
  color: var(--primary);
  background: var(--primary-soft);
}

.side-item.active {
  color: var(--primary);
  font-weight: 600;
  background: var(--primary-soft);
  border-left-color: var(--primary);
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 8px;
  height: 48px;
  margin-bottom: 18px;
}

.sort-item {
  font-size: 14px;
  color: var(--text-sub);
  padding: 5px 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.18s;
}

.sort-item:hover {
  color: var(--primary);
}

.sort-item.active {
  color: #fff;
  background: var(--primary-grad);
  font-weight: 600;
}

.sort-arrow {
  font-style: normal;
  font-size: 12px;
  margin-left: 2px;
}

.brand-filter {
  position: relative;
  margin-left: 12px;
}

.brand-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-sub);
  padding: 5px 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.18s;
  max-width: 180px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.brand-btn:hover {
  color: var(--primary);
}

.brand-btn.active {
  color: var(--primary);
  background: var(--primary-soft);
  font-weight: 600;
}

.brand-caret {
  font-style: normal;
  font-size: 12px;
  transition: transform 0.18s;
}

.brand-caret.up {
  transform: rotate(180deg);
}

.brand-panel {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  width: 320px;
  padding: 12px;
  z-index: 30;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.brand-search input {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #e3e5ea;
  border-radius: 6px;
  padding: 7px 10px;
  font-size: 13px;
  outline: none;
  margin-bottom: 10px;
}

.brand-search input:focus {
  border-color: var(--primary);
}

.brand-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 260px;
  overflow-y: auto;
}

.brand-item {
  font-size: 13px;
  color: var(--text-sub);
  border: 1px solid #e3e5ea;
  border-radius: 14px;
  padding: 5px 14px;
  cursor: pointer;
  transition: all 0.15s;
  background: #fff;
}

.brand-item:hover {
  color: var(--primary);
  border-color: var(--primary);
}

.brand-item.on {
  color: #fff;
  background: var(--primary-grad);
  border-color: transparent;
  font-weight: 600;
}

.sub-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 16px;
  margin-bottom: 18px;
}

.sub-label {
  font-size: 14px;
  font-weight: 700;
  margin-right: 4px;
}

.sub-chip {
  font-size: 13px;
  color: var(--text-sub);
  border: 1px solid #e3e5ea;
  border-radius: 14px;
  padding: 5px 14px;
  cursor: pointer;
  transition: all 0.15s;
  background: #fff;
}

.sub-chip:hover {
  color: var(--primary);
  border-color: var(--primary);
}

.sub-chip.on {
  color: #fff;
  background: var(--primary-grad);
  border-color: transparent;
  font-weight: 600;
}

.tool-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
  color: var(--text-sub);
}

.kw-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: var(--primary-soft);
  color: var(--primary);
  padding: 4px 10px;
  border-radius: 14px;
  font-size: 12px;
}

.kw-clear {
  font-style: normal;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
}

.kw-clear:hover {
  color: var(--price);
}

.load-more {
  text-align: center;
  color: var(--primary);
  font-size: 13px;
  padding: 18px 0;
  cursor: pointer;
}

.load-more:hover {
  text-decoration: underline;
}

@media (max-width: 900px) {
  .list-layout {
    grid-template-columns: 1fr;
  }

  .toolbar {
    height: auto;
    flex-wrap: wrap;
    padding: 8px;
    gap: 6px;
  }

  .brand-panel {
    width: 280px;
  }

  .side {
    position: static;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    padding: 10px;
  }

  .side-title {
    width: 100%;
    border: none;
    padding: 4px 10px;
  }

  .side-item {
    border-left: none;
    padding: 8px 14px;
    background: #f4f5f7;
    border-radius: 16px;
  }

  .side-item.active {
    background: var(--primary-grad);
    color: #fff;
  }
}
</style>

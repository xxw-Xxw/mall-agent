<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories } from '@/api'
import { categoryIcon } from '@/utils/categoryIcon'

const router = useRouter()
const groups = ref([])
const loading = ref(true)

function goCategory(categoryId) {
  router.push({ name: 'list', query: { categoryId } })
}

onMounted(async () => {
  try {
    const res = await getCategories()
    groups.value = res.data || []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="container">
    <div class="crumb">
      <a @click="router.push('/')">首页</a>
      <span class="sep">/</span>
      <span class="cur">全部分类</span>
    </div>

    <div class="page-head">
      <h1>全部分类</h1>
      <div class="sub">{{ groups.length }} 大分类 · {{ groups.reduce((s, g) => s + (g.children || []).length, 0) }} 个子分类，点击直达对应商品</div>
    </div>

    <div v-if="loading" class="loading-tip" style="padding-top:60px">加载中...</div>

    <div v-else class="cat-groups">
      <div v-for="g in groups" :key="g.categoryId" class="cgroup card">
        <div class="cg-head" @click="goCategory(g.categoryId)">
          <span class="cg-icon">{{ categoryIcon(g.categoryName) }}</span>
          <span class="cg-name">{{ g.categoryName }}</span>
          <span class="cg-count">{{ (g.children || []).length }} 个子类</span>
          <span class="cg-arrow">›</span>
        </div>
        <div class="chips">
          <span
            v-for="child in g.children"
            :key="child.categoryId"
            class="chip"
            @click="goCategory(child.categoryId)"
          >{{ child.categoryName }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cat-groups {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

.cgroup {
  padding: 20px 24px;
}

.cg-head {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
  transition: color 0.18s;
}

.cg-head:hover {
  color: var(--primary);
}

.cg-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: var(--primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.cg-name {
  font-size: 17px;
  font-weight: 700;
}

.cg-count {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-sub);
  font-weight: 400;
}

.cg-arrow {
  color: #c0c4cc;
  font-size: 20px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding-top: 16px;
}

.chip {
  font-size: 13px;
  color: var(--text-sub);
  border: 1px solid #e3e5ea;
  background: #fff;
  border-radius: 16px;
  padding: 7px 18px;
  cursor: pointer;
  transition: all 0.18s;
}

.chip:hover {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-soft);
  transform: translateY(-1px);
}

@media (max-width: 900px) {
  .cat-groups {
    grid-template-columns: 1fr;
  }
}
</style>

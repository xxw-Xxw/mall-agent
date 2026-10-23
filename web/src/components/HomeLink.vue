<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <button class="home-link" title="返回商城首页" @click="router.push('/')">
    <span class="hl-icon">🏠</span>
    <span class="hl-text">回首页</span>
  </button>
</template>

<style scoped>
.home-link {
  display: flex;
  align-items: center;
  gap: 6px;
  border: 1px solid #e3e5ea;
  border-radius: 22px;
  padding: 9px 16px;
  font-size: 14px;
  color: var(--text-sub);
  background: #fff;
  cursor: pointer;
  transition: all 0.18s;
  white-space: nowrap;
}

.hl-icon {
  font-size: 16px;
  line-height: 1;
}

.home-link:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-soft);
}
</style>

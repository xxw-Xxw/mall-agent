<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getInfo, logout, getCartList } from '@/api'
import { userState, isLoggedIn, clearToken, setUserInfo } from '@/store/user'
import toast from '@/utils/toast'
import HomeLink from '@/components/HomeLink.vue'

const router = useRouter()
const route = useRoute()
const keyword = ref('')

const quickLinks = [
  { label: '全部分类', to: '/categories' },
  { label: '我的订单', to: '/orders' },
  { label: '我的收藏', to: '/favorites' },
  { label: '浏览足迹', to: '/history' },
  { label: '个人中心', to: '/mine' }
]

function goSearch() {
  const kw = keyword.value.trim()
  router.push({ name: 'list', query: kw ? { keyword: kw } : {} })
}

async function handleLogout() {
  try {
    await logout()
  } catch (e) {
    // token失效也继续清理本地状态
  }
  clearToken()
  toast.success('已退出登录')
  router.push('/')
}

onMounted(async () => {
  if (isLoggedIn()) {
    try {
      const res = await getInfo()
      setUserInfo(res.user)
      const cartRes = await getCartList()
      userState.cartCount = (cartRes.data || []).length
    } catch (e) {
      // token 失效时 request 拦截器已跳登录
    }
  }
})
</script>

<template>
  <div class="site-top">
    <div class="container top-inner">
      <span class="welcome">欢迎来到 AI智选商城，AI 导购随时为您服务</span>
      <div class="top-links">
        <template v-if="isLoggedIn()">
          <span class="user">👤 {{ userState.nickName || '商城用户' }}</span>
          <a @click="handleLogout">退出登录</a>
        </template>
        <template v-else>
          <a class="strong" @click="router.push('/login')">请登录</a>
          <a @click="router.push('/register')">免费注册</a>
        </template>
        <span class="divider"></span>
        <a v-for="link in quickLinks" :key="link.to" @click="router.push(link.to)">{{ link.label }}</a>
      </div>
    </div>
  </div>

  <header class="site-header">
    <div class="container header-inner">
      <a class="logo" @click="router.push('/')">
        <span class="logo-icon">🛍️</span>
        <span class="logo-text">AI智选<em>商城</em></span>
      </a>

      <div class="search-box">
        <input v-model="keyword" placeholder="搜索商品，如“手机”“笔记本”" @keyup.enter="goSearch" />
        <button @click="goSearch">搜索</button>
      </div>

      <div class="header-actions">
        <HomeLink v-if="route.path !== '/'" />
        <a class="cart-btn" @click="router.push('/cart')">
          <span class="c-icon">🛒</span>
          <span class="c-label">购物车</span>
          <span v-if="userState.cartCount" class="c-badge">{{ userState.cartCount }}</span>
        </a>
        <a class="ai-btn" @click="router.push('/chat')">
          <span>🤖</span> AI 导购
        </a>
      </div>
    </div>
  </header>

  <main class="page">
    <router-view />
  </main>

  <footer class="site-footer">
    <div class="container footer-inner">
      <span>AI智选商城 · Spring Boot + Vue3 学习演示项目</span>
      <span>仅供学习交流使用</span>
    </div>
  </footer>
</template>

<style scoped>
.site-top {
  background: #fff;
  border-bottom: 1px solid var(--line);
  font-size: 12px;
  color: var(--text-sub);
}

.top-inner {
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.top-links {
  display: flex;
  align-items: center;
  gap: 18px;
}

.top-links a:hover {
  color: var(--primary);
}

.top-links .strong {
  color: var(--primary);
  font-weight: 600;
}

.top-links .user {
  color: var(--text);
}

.divider {
  width: 1px;
  height: 12px;
  background: #dfe2e8;
}

.site-header {
  background: #fff;
  box-shadow: var(--shadow);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  height: 76px;
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  cursor: pointer;
}

.logo-icon {
  font-size: 32px;
}

.logo-text {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 1px;
}

.logo-text em {
  font-style: normal;
  background: var(--primary-grad);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.search-box {
  flex: 1;
  max-width: 560px;
  display: flex;
  border: 2px solid var(--primary);
  border-radius: 10px;
  overflow: hidden;
}

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  padding: 0 16px;
  height: 42px;
  font-size: 14px;
  background: transparent;
  min-width: 0;
}

.search-box input::placeholder {
  color: #b8bdc6;
}

.search-box button {
  border: none;
  background: var(--primary-grad);
  color: #fff;
  width: 84px;
  font-size: 15px;
  cursor: pointer;
  flex-shrink: 0;
}

.search-box button:hover {
  filter: brightness(1.05);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-left: auto;
  flex-shrink: 0;
}

.cart-btn {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  border: 1px solid #e3e5ea;
  border-radius: 22px;
  padding: 9px 18px;
  font-size: 14px;
  transition: all 0.18s;
}

.cart-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-soft);
}

.c-icon {
  font-size: 17px;
}

.c-badge {
  position: absolute;
  top: -8px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  background: var(--price);
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
}

.ai-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--primary-grad);
  color: #fff;
  border-radius: 22px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(255, 96, 52, 0.3);
  transition: all 0.18s;
}

.ai-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(255, 96, 52, 0.4);
}

.site-footer {
  background: #2b2f36;
  color: #9aa0aa;
  font-size: 13px;
}

.footer-inner {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>

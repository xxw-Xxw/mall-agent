<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getInfo, logout } from '@/api'
import { userState, isLoggedIn, setUserInfo, clearToken } from '@/store/user'
import { getCartList } from '@/api'
import toast from '@/utils/toast'

const router = useRouter()

const menus = computed(() => [
  { icon: '📦', label: '我的订单', desc: '查看订单状态与物流', to: '/orders' },
  { icon: '🛒', label: '购物车', desc: `共 ${userState.cartCount} 件商品`, to: '/cart' },
  { icon: '❤️', label: '我的收藏', desc: '心仪好物一键直达', to: '/favorites', auth: true },
  { icon: '🕘', label: '浏览足迹', desc: '回顾您看过的商品', to: '/history', auth: true },
  { icon: '🤖', label: 'AI 导购', desc: '让小智帮您挑好物', to: '/chat' },
  { icon: '👤', label: '账号设置', desc: '管理登录状态', to: '#', action: 'toggle-auth' }
])

function goMenu(menu) {
  if (menu.action === 'toggle-auth') {
    if (isLoggedIn()) handleLogout()
    else router.push({ name: 'login' })
    return
  }
  if (menu.auth && !isLoggedIn()) {
    router.push({ name: 'login', query: { redirect: menu.to } })
    return
  }
  router.push(menu.to)
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
  <div class="container">
    <div class="page-head">
      <h1>个人中心</h1>
      <div class="sub">管理您的账户与购物数据</div>
    </div>

    <div class="mine-grid">
      <div class="profile-card card">
        <div class="avatar">👤</div>
        <template v-if="isLoggedIn()">
          <div class="p-name">{{ userState.nickName || '商城用户' }}</div>
          <div class="p-sub">欢迎回来，AI 助手随时为您服务</div>
          <button class="btn-plain logout-btn" @click="handleLogout">退出登录</button>
        </template>
        <template v-else>
          <div class="p-name" @click="router.push({ name: 'login' })">点击登录</div>
          <div class="p-sub">登录后可下单、收藏与管理购物车</div>
          <button class="btn-primary" @click="router.push({ name: 'login' })">立即登录</button>
        </template>
      </div>

      <div class="menu-grid">
        <div v-for="menu in menus" :key="menu.label" class="menu-card card" @click="goMenu(menu)">
          <span class="m-icon">{{ menu.icon }}</span>
          <div class="m-text">
            <div class="m-label">{{ menu.label }}</div>
            <div class="m-desc">{{ menu.desc }}</div>
          </div>
          <span class="m-arrow">›</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mine-grid {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  align-items: start;
}

.profile-card {
  padding: 36px 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.avatar {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: var(--primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 42px;
}

.p-name {
  font-size: 18px;
  font-weight: 700;
  margin-top: 16px;
  cursor: pointer;
}

.p-sub {
  font-size: 13px;
  color: var(--text-sub);
  margin-top: 8px;
  line-height: 1.6;
}

.profile-card .btn-primary,
.logout-btn {
  margin-top: 22px;
}

.logout-btn {
  color: var(--text-sub);
}

.logout-btn:hover {
  color: var(--price);
  border-color: var(--price);
  background: #fff5f5;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

.menu-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px 24px;
  cursor: pointer;
  transition: all 0.2s;
}

.menu-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}

.m-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: var(--primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
}

.m-text {
  flex: 1;
  min-width: 0;
}

.m-label {
  font-size: 15px;
  font-weight: 600;
}

.m-desc {
  font-size: 12px;
  color: var(--text-sub);
  margin-top: 4px;
}

.m-arrow {
  color: #c0c4cc;
  font-size: 22px;
}

@media (max-width: 900px) {
  .mine-grid {
    grid-template-columns: 1fr;
  }

  .menu-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login, getInfo } from '@/api'
import { setToken, setUserInfo } from '@/store/user'
import toast from '@/utils/toast'

const route = useRoute()
const router = useRouter()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function doLogin() {
  if (!username.value || !password.value) {
    toast.error('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const res = await login({
      username: username.value,
      password: password.value
    })
    setToken(res.token)
    const infoRes = await getInfo()
    setUserInfo(infoRes.user)
    toast.success('登录成功')
    router.push(route.query.redirect || '/')
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="login-brand">
        <div class="lb-inner">
          <div class="lb-logo">🛍️</div>
          <div class="lb-title">AI智选商城</div>
          <div class="lb-slogan">AI 导购 · 智享购物</div>
          <ul class="lb-points">
            <li>🤖 AI 导购小智，帮你精准选品</li>
            <li>🛍️ 海量精选好物，一站式购齐</li>
            <li>🚀 极速发货，7 天无理由退换</li>
          </ul>
        </div>
      </div>

      <div class="login-form">
        <h2>欢迎回来</h2>
        <p class="form-sub">登录后可下单、收藏与管理购物车</p>

        <div class="field">
          <input v-model="username" placeholder="请输入账号" @keyup.enter="doLogin" />
        </div>
        <div class="field">
          <input v-model="password" type="password" placeholder="请输入密码" @keyup.enter="doLogin" />
        </div>

        <button class="btn-primary login-btn" :disabled="loading" @click="doLogin">
          {{ loading ? '登录中...' : '登 录' }}
        </button>

        <div class="switch-line">
          <span>还没有账号？</span>
          <router-link class="link" :to="{ name: 'register', query: route.query }">免费注册</router-link>
        </div>
        <div class="tip">浏览商品无需登录，下单 / 收藏需先登录</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
}

.login-card {
  width: 860px;
  max-width: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.login-brand {
  background:
    radial-gradient(circle at 20% 85%, rgba(255, 255, 255, 0.16), transparent 46%),
    linear-gradient(135deg, #ff6034, #ff9a44);
  color: #fff;
  padding: 48px 40px;
  display: flex;
  align-items: center;
}

.lb-logo {
  font-size: 46px;
}

.lb-title {
  font-size: 26px;
  font-weight: 800;
  margin-top: 14px;
  letter-spacing: 1px;
}

.lb-slogan {
  font-size: 13px;
  opacity: 0.92;
  margin-top: 8px;
  letter-spacing: 3px;
}

.lb-points {
  list-style: none;
  margin-top: 36px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  font-size: 14px;
  opacity: 0.95;
}

.login-form {
  background: #fff;
  padding: 48px 44px;
}

.login-form h2 {
  font-size: 24px;
  font-weight: 700;
}

.form-sub {
  font-size: 13px;
  color: var(--text-sub);
  margin-top: 8px;
}

.field {
  display: flex;
  align-items: center;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  margin-top: 18px;
  transition: all 0.18s;
}

.field:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(255, 96, 52, 0.1);
}

.field input {
  flex: 1;
  border: none;
  outline: none;
  height: 46px;
  padding: 0 16px;
  font-size: 14px;
  background: transparent;
  min-width: 0;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  margin-top: 26px;
  letter-spacing: 6px;
}

.switch-line {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-sub);
  margin-top: 16px;
}

.switch-line .link {
  color: var(--primary);
  font-weight: 600;
  cursor: pointer;
}

.tip {
  text-align: center;
  color: var(--text-sub);
  font-size: 12px;
  margin-top: 18px;
}

@media (max-width: 760px) {
  .login-card {
    grid-template-columns: 1fr;
  }

  .login-brand {
    display: none;
  }
}
</style>

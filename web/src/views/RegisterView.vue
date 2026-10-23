<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { register, getInfo } from '@/api'
import { setToken, setUserInfo } from '@/store/user'
import toast from '@/utils/toast'

const route = useRoute()
const router = useRouter()

const username = ref('')
const password = ref('')
const confirm = ref('')
const nickname = ref('')
const loading = ref(false)

async function doRegister() {
  if (!username.value || !password.value) {
    toast.error('请输入账号和密码')
    return
  }
  if (username.value.length < 2 || username.value.length > 30) {
    toast.error('账号长度须为 2-30 个字符')
    return
  }
  if (password.value.length < 6) {
    toast.error('密码长度不能少于 6 位')
    return
  }
  if (password.value !== confirm.value) {
    toast.error('两次输入的密码不一致')
    return
  }
  loading.value = true
  try {
    const res = await register({
      username: username.value,
      password: password.value,
      confirm: confirm.value,
      nickname: nickname.value
    })
    setToken(res.token)
    const infoRes = await getInfo()
    setUserInfo(infoRes.user)
    toast.success('注册成功，已自动登录')
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
          <div class="lb-title">加入我们</div>
          <div class="lb-slogan">注册即享会员购物</div>
          <ul class="lb-points">
            <li>🤖 AI 导购小智，帮你精准选品</li>
            <li>🛍️ 海量精选好物，一站式购齐</li>
            <li>🚀 极速发货，7 天无理由退换</li>
          </ul>
        </div>
      </div>

      <div class="login-form">
        <h2>免费注册</h2>
        <p class="form-sub">注册商城会员，即可下单、收藏与管理购物车</p>

        <div class="field">
          <input v-model="username" placeholder="请输入账号（2-30 个字符）" />
        </div>
        <div class="field">
          <input v-model="nickname" placeholder="昵称（选填，默认为账号）" @keyup.enter="doRegister" />
        </div>
        <div class="field">
          <input v-model="password" type="password" placeholder="请输入密码（不少于 6 位）" @keyup.enter="doRegister" />
        </div>
        <div class="field">
          <input v-model="confirm" type="password" placeholder="请再次输入密码" @keyup.enter="doRegister" />
        </div>

        <button class="btn-primary login-btn" :disabled="loading" @click="doRegister">
          {{ loading ? '注册中...' : '注 册' }}
        </button>

        <div class="switch-line">
          <span>已有账号？</span>
          <router-link class="link" :to="{ name: 'login', query: route.query }">去登录</router-link>
        </div>
        <div class="tip">商城会员账号与管理后台账号相互独立</div>
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
  padding: 40px 44px;
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
  margin-top: 16px;
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
  margin-top: 24px;
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
  margin-top: 14px;
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

import { reactive } from 'vue'

const TOKEN_KEY = 'mall_token'
const USER_KEY = 'mall_user'

export const userState = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  userId: null,
  nickName: '',
  avatar: '',
  cartCount: 0
})

export function getToken() {
  return userState.token
}

export function setToken(token) {
  userState.token = token
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  userState.token = ''
  userState.userId = null
  userState.nickName = ''
  userState.avatar = ''
  userState.cartCount = 0
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function isLoggedIn() {
  return !!userState.token
}

export function setUserInfo(user) {
  userState.userId = user.userId
  userState.nickName = user.nickName || ''
  userState.avatar = user.avatar || ''
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function restoreUser() {
  try {
    const cached = JSON.parse(localStorage.getItem(USER_KEY))
    if (cached) {
      userState.userId = cached.userId
      userState.nickName = cached.nickName || ''
      userState.avatar = cached.avatar || ''
    }
  } catch (e) {
    // ignore
  }
}

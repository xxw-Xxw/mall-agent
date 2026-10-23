import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '@/store/user'
import { restoreUser } from '@/store/user'

restoreUser()

const routes = [
  { path: '/', name: 'home', component: () => import('@/views/HomeView.vue'), meta: { tab: 'home' } },
  { path: '/list', name: 'list', component: () => import('@/views/ListView.vue'), meta: { tab: 'list' } },
  { path: '/categories', name: 'categories', component: () => import('@/views/CategoryView.vue') },
  { path: '/detail/:id', name: 'detail', component: () => import('@/views/DetailView.vue') },
  { path: '/cart', name: 'cart', component: () => import('@/views/CartView.vue'), meta: { tab: 'cart', auth: true } },
  { path: '/orders', name: 'orders', component: () => import('@/views/OrderView.vue'), meta: { tab: 'orders', auth: true } },
  { path: '/mine', name: 'mine', component: () => import('@/views/MineView.vue'), meta: { tab: 'mine' } },
  { path: '/favorites', name: 'favorites', component: () => import('@/views/FavoriteView.vue'), meta: { auth: true } },
  { path: '/history', name: 'history', component: () => import('@/views/HistoryView.vue'), meta: { auth: true } },
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
  { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue') },
  { path: '/chat', name: 'chat', component: () => import('@/views/ChatView.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.auth && !isLoggedIn()) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router

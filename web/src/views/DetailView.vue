<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGoodsDetail, addCart, toggleFavorite, createOrder } from '@/api'
import { isLoggedIn } from '@/store/user'
import toast from '@/utils/toast'

const route = useRoute()
const router = useRouter()
const goodsId = Number(route.params.id)

const goods = ref(null)
const isFavorite = ref(false)
const loading = ref(true)
const quantity = ref(1)
const submitting = ref(false)

const tags = computed(() => {
  if (!goods.value || !goods.value.tags) return []
  return goods.value.tags.split(',').map((t) => t.trim()).filter(Boolean)
})

async function loadDetail() {
  loading.value = true
  try {
    const res = await getGoodsDetail(goodsId)
    goods.value = res.data.goods
    isFavorite.value = res.data.isFavorite
  } finally {
    loading.value = false
  }
}

function ensureLogin() {
  if (!isLoggedIn()) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

function changeQuantity(delta) {
  if (!goods.value) return
  let val = quantity.value + delta
  if (val < 1) val = 1
  if (val > goods.value.stock) val = goods.value.stock
  quantity.value = val
}

async function handleAddCart() {
  if (!ensureLogin()) return
  try {
    await addCart(goodsId, quantity.value)
    toast.success('已加入购物车')
  } catch (e) {
    toast.error(e.message)
  }
}

async function handleBuyNow() {
  if (!ensureLogin()) return
  submitting.value = true
  try {
    await createOrder({ goodsId, quantity: quantity.value })
    toast.success('下单成功')
    router.push({ name: 'orders' })
  } catch (e) {
    toast.error(e.message)
  } finally {
    submitting.value = false
  }
}

async function handleFavorite() {
  if (!ensureLogin()) return
  try {
    const res = await toggleFavorite(goodsId)
    isFavorite.value = res.data.isFavorite
    toast.success(isFavorite.value ? '已收藏' : '已取消收藏')
  } catch (e) {
    toast.error(e.message)
  }
}

onMounted(loadDetail)
</script>

<template>
  <div class="container">
    <div v-if="loading" class="loading-tip" style="padding-top:120px">加载中...</div>

    <template v-else-if="goods">
      <div class="crumb">
        <a @click="router.push('/')">首页</a>
        <span class="sep">/</span>
        <a @click="router.push('/list')">全部商品</a>
        <span class="sep">/</span>
        <span class="cur">{{ goods.goodsName }}</span>
      </div>

      <div class="detail-grid">
        <div class="d-img card">
          <img :src="goods.imageUrl" :alt="goods.goodsName" />
        </div>

        <div class="d-info card">
          <h1 class="d-name">{{ goods.goodsName }}</h1>
          <div class="d-sub">品牌：{{ goods.brand || '—' }} · 已售 {{ goods.sales }} · 库存 {{ goods.stock }}</div>
          <div v-if="tags.length" class="tags">
            <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
          </div>

          <div class="d-price">
            <span class="price"><span class="rmb">¥</span><span class="num">{{ goods.price }}</span></span>
            <span class="p-tip">AI 智选 · 品质好物</span>
          </div>

          <div class="d-qty">
            <span class="q-label">数量</span>
            <div class="stepper">
              <button :disabled="quantity <= 1" @click="changeQuantity(-1)">−</button>
              <input v-model.number="quantity" readonly />
              <button :disabled="quantity >= goods.stock" @click="changeQuantity(1)">+</button>
            </div>
          </div>

          <div class="d-actions">
            <button class="btn-cart" @click="handleAddCart">🛒 加入购物车</button>
            <button class="btn-primary btn-buy" :disabled="submitting" @click="handleBuyNow">
              {{ submitting ? '下单中...' : '立即购买' }}
            </button>
            <button class="fav-btn" :class="{ on: isFavorite }" @click="handleFavorite">
              {{ isFavorite ? '❤️ 已收藏' : '🤍 收藏' }}
            </button>
          </div>

          <div class="d-service">
            <span>🚚 极速发货</span>
            <span>↩️ 7天无理由退货</span>
            <span>✅ 正品保障</span>
          </div>
        </div>
      </div>

      <div class="card desc-card">
        <div class="sec-title">商品介绍</div>
        <div class="desc">{{ goods.description || '暂无介绍' }}</div>
      </div>

      <div class="ai-banner card" @click="router.push('/chat')">
        <span class="ai-icon">🤖</span>
        <span class="ai-text">不知道选哪款？把需求告诉 AI 导购小智，让它帮你对比 & 推荐</span>
        <span class="arrow">立即对话 →</span>
      </div>
    </template>

    <div v-else class="empty"><span class="icon">:(</span>商品不存在或已下架</div>
  </div>
</template>

<style scoped>
.detail-grid {
  display: grid;
  grid-template-columns: 480px 1fr;
  gap: 24px;
  align-items: start;
}

.d-img {
  overflow: hidden;
}

.d-img img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  display: block;
  background: #f2f3f5;
}

.d-info {
  padding: 28px 32px;
}

.d-name {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.45;
}

.d-sub {
  margin-top: 10px;
  font-size: 13px;
  color: var(--text-sub);
}

.tags {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  background: var(--primary-soft);
  color: var(--primary);
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 4px;
}

.d-price {
  margin-top: 18px;
  background: var(--primary-soft);
  border-radius: 10px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price .num {
  font-size: 30px;
}

.p-tip {
  font-size: 12px;
  color: var(--primary);
  border: 1px dashed var(--primary);
  border-radius: 12px;
  padding: 3px 10px;
}

.d-qty {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.q-label {
  font-size: 14px;
  color: var(--text-sub);
}

.stepper {
  display: flex;
  align-items: center;
}

.stepper button {
  width: 34px;
  height: 34px;
  border: 1px solid #dcdfe6;
  background: #fff;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.18s;
}

.stepper button:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary);
}

.stepper button:first-child {
  border-radius: 8px 0 0 8px;
}

.stepper button:last-child {
  border-radius: 0 8px 8px 0;
}

.stepper button:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.stepper input {
  width: 52px;
  height: 34px;
  border: 1px solid #dcdfe6;
  border-left: none;
  border-right: none;
  text-align: center;
  outline: none;
  font-size: 14px;
}

.d-actions {
  margin-top: 26px;
  display: flex;
  gap: 14px;
}

.btn-cart {
  border: 1px solid var(--primary);
  background: var(--primary-soft);
  color: var(--primary);
  border-radius: 8px;
  height: 46px;
  padding: 0 28px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.18s;
}

.btn-cart:hover {
  background: #ffe3d6;
}

.btn-buy {
  height: 46px;
  padding: 0 34px;
  font-size: 15px;
}

.fav-btn {
  border: 1px solid #e3e5ea;
  background: #fff;
  border-radius: 8px;
  height: 46px;
  padding: 0 20px;
  font-size: 14px;
  color: var(--text-sub);
  cursor: pointer;
  transition: all 0.18s;
}

.fav-btn:hover,
.fav-btn.on {
  color: var(--price);
  border-color: var(--price);
  background: #fff5f5;
}

.d-service {
  margin-top: 26px;
  padding-top: 18px;
  border-top: 1px dashed var(--line);
  display: flex;
  gap: 26px;
  font-size: 12px;
  color: var(--text-sub);
}

.desc-card {
  margin-top: 24px;
}

.sec-title {
  font-size: 17px;
  font-weight: 700;
  padding: 20px 28px 6px;
}

.desc {
  padding: 10px 28px 28px;
  font-size: 14px;
  color: #555;
  line-height: 1.8;
  white-space: pre-wrap;
}

.ai-banner {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 28px;
  cursor: pointer;
  background: linear-gradient(90deg, #fff4ef, #fff);
  border: 1px solid #ffe0d0;
  transition: all 0.2s;
}

.ai-banner:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.ai-icon {
  font-size: 30px;
}

.ai-text {
  flex: 1;
  font-size: 14px;
  color: var(--text);
}

.arrow {
  color: var(--primary);
  font-size: 14px;
  font-weight: 600;
}

@media (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>

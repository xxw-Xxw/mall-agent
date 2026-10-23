<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, updateCartQuantity, removeCarts, createOrder } from '@/api'
import { userState } from '@/store/user'
import toast from '@/utils/toast'

const router = useRouter()
const items = ref([])
const loading = ref(true)
const checked = ref([])
const submitting = ref(false)

const selectedItems = computed(() => items.value.filter((i) => checked.value.includes(i.cartId)))
const totalAmount = computed(() =>
  selectedItems.value.reduce((sum, i) => sum + Number(i.subtotal), 0)
)

function toggleAll(e) {
  checked.value = e.target.checked ? items.value.map((i) => i.cartId) : []
}

async function loadCart() {
  loading.value = true
  try {
    const res = await getCartList()
    items.value = res.data || []
    checked.value = items.value.map((i) => i.cartId)
    userState.cartCount = items.value.length
  } finally {
    loading.value = false
  }
}

async function changeQuantity(item, delta) {
  let val = item.quantity + delta
  if (val < 1) val = 1
  if (val > item.stock) {
    toast.error('库存不足')
    return
  }
  try {
    await updateCartQuantity(item.cartId, val)
    item.quantity = val
    item.subtotal = (Number(item.price) * val).toFixed(2)
  } catch (e) {
    toast.error(e.message)
  }
}

async function removeItem(item) {
  if (!confirm('确定将该商品移出购物车吗？')) return
  try {
    await removeCarts([item.cartId])
    toast.success('删除成功')
    loadCart()
  } catch (e) {
    toast.error(e.message)
  }
}

async function removeSelected() {
  if (!checked.value.length) return
  if (!confirm(`确定删除选中的 ${checked.value.length} 件商品吗？`)) return
  try {
    await removeCarts(checked.value)
    toast.success('删除成功')
    loadCart()
  } catch (e) {
    toast.error(e.message)
  }
}

async function checkout() {
  if (!selectedItems.value.length) {
    toast.info('请先勾选要结算的商品')
    return
  }
  submitting.value = true
  try {
    await createOrder({ cartIds: checked.value })
    toast.success('下单成功')
    router.push({ name: 'orders' })
  } catch (e) {
    toast.error(e.message)
  } finally {
    submitting.value = false
  }
}

onMounted(loadCart)
</script>

<template>
  <div class="container">
    <div class="page-head">
      <h1>购物车</h1>
      <div class="sub">共 {{ items.length }} 件商品</div>
    </div>

    <div v-if="loading" class="loading-tip" style="padding-top:60px">加载中...</div>
    <div v-else-if="!items.length" class="empty card">
      <span class="icon">🛒</span>
      购物车还是空的，去逛逛吧
      <div style="margin-top:18px"><button class="btn-primary" @click="router.push('/')">去逛逛</button></div>
    </div>

    <template v-else>
      <div class="card cart-table">
        <div class="ct-row ct-head">
          <label class="ct-check">
            <input type="checkbox" :checked="checked.length === items.length && items.length > 0" @change="toggleAll" />
            全选
          </label>
          <span class="ct-goods">商品信息</span>
          <span class="ct-price">单价</span>
          <span class="ct-qty">数量</span>
          <span class="ct-subtotal">小计</span>
          <span class="ct-op">操作</span>
        </div>

        <div v-for="item in items" :key="item.cartId" class="ct-row">
          <input v-model="checked" type="checkbox" class="ct-check" :value="item.cartId" />
          <div class="ct-goods">
            <img :src="item.imageUrl" @click="router.push(`/detail/${item.goodsId}`)" />
            <span class="cg-name" @click="router.push(`/detail/${item.goodsId}`)">{{ item.goodsName }}</span>
          </div>
          <span class="ct-price">¥{{ item.price }}</span>
          <div class="ct-qty">
            <div class="stepper">
              <button @click="changeQuantity(item, -1)">−</button>
              <span class="qty">{{ item.quantity }}</span>
              <button @click="changeQuantity(item, 1)">+</button>
            </div>
          </div>
          <span class="ct-subtotal"><span class="price"><span class="rmb">¥</span>{{ item.subtotal }}</span></span>
          <span class="ct-op"><a class="rm" @click="removeItem(item)">删除</a></span>
        </div>
      </div>

      <div class="card checkout-bar">
        <label class="all-check">
          <input type="checkbox" :checked="checked.length === items.length && items.length > 0" @change="toggleAll" />
          全选
        </label>
        <a class="rm" @click="removeSelected">删除选中</a>
        <div class="spacer"></div>
        <span class="sel-count">已选 <b>{{ checked.length }}</b> 件</span>
        <span class="total">合计：<span class="price"><span class="rmb">¥</span>{{ totalAmount.toFixed(2) }}</span></span>
        <button class="btn-primary btn-go" :disabled="submitting" @click="checkout">
          {{ submitting ? '结算中...' : '去结算' }}
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.cart-table {
  overflow: hidden;
}

.ct-row {
  display: grid;
  grid-template-columns: 60px 1fr 130px 150px 130px 80px;
  align-items: center;
  padding: 18px 24px;
  border-bottom: 1px solid var(--line);
  gap: 12px;
}

.ct-row:last-child {
  border-bottom: none;
}

.ct-head {
  background: #fafbfc;
  font-size: 13px;
  color: var(--text-sub);
  padding: 12px 24px;
}

.ct-check {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 13px;
}

.ct-check input {
  width: 16px;
  height: 16px;
  accent-color: var(--primary);
  cursor: pointer;
}

.ct-goods {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.ct-goods img {
  width: 76px;
  height: 76px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  cursor: pointer;
  background: #f2f3f5;
}

.cg-name {
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  cursor: pointer;
}

.cg-name:hover {
  color: var(--primary);
}

.ct-price {
  font-size: 13px;
  color: var(--text-sub);
}

.stepper {
  display: flex;
  align-items: center;
}

.stepper button {
  width: 28px;
  height: 28px;
  border: 1px solid #dcdfe6;
  background: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.18s;
}

.stepper button:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.stepper button:first-child {
  border-radius: 6px 0 0 6px;
}

.stepper button:last-child {
  border-radius: 0 6px 6px 0;
}

.qty {
  width: 44px;
  text-align: center;
  font-size: 13px;
  border-top: 1px solid #dcdfe6;
  border-bottom: 1px solid #dcdfe6;
  height: 28px;
  line-height: 28px;
}

.ct-subtotal .price {
  font-size: 15px;
}

.ct-op .rm {
  font-size: 13px;
  color: var(--text-sub);
  cursor: pointer;
}

.ct-op .rm:hover {
  color: var(--price);
}

.checkout-bar {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 14px 24px;
  position: sticky;
  bottom: 16px;
  z-index: 10;
}

.all-check {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  cursor: pointer;
}

.all-check input {
  accent-color: var(--primary);
}

.checkout-bar .rm {
  font-size: 13px;
  color: var(--text-sub);
  cursor: pointer;
}

.checkout-bar .rm:hover {
  color: var(--price);
}

.spacer {
  flex: 1;
}

.sel-count {
  font-size: 13px;
  color: var(--text-sub);
}

.sel-count b {
  color: var(--primary);
  font-size: 15px;
}

.total {
  font-size: 14px;
}

.total .price {
  font-size: 22px;
}

.btn-go {
  height: 46px;
  padding: 0 36px;
  font-size: 15px;
}

@media (max-width: 900px) {
  .ct-row {
    grid-template-columns: 40px 1fr 90px 120px 90px 56px;
    padding: 14px 12px;
  }
}
</style>

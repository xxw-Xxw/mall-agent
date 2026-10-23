<script setup>
defineProps({
  goods: { type: Object, required: true }
})
</script>

<template>
  <div class="goods-item" @click="$router.push(`/detail/${goods.goodsId}`)">
    <img :src="goods.imageUrl" :alt="goods.goodsName" loading="lazy" />
    <div class="info">
      <div class="name">{{ goods.goodsName }}</div>
      <div class="meta">
        <span class="price"><span class="rmb">¥</span>{{ goods.price }}</span>
        <span class="sales">已售{{ goods.sales }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const AUTO_INTERVAL = 5000

const slides = [
  {
    tag: '新品首发',
    title: '数码科技专场',
    sub: '旗舰新品直降 · 以旧换新额外补贴',
    cta: '立即抢购',
    prompt: '电商横幅背景图：深蓝紫科技感场景，悬浮的手机、笔记本电脑、无线耳机产品组合，居中偏右构图，柔和蓝色光效，深色干净背景，无任何文字，专业电商风格，写实摄影质感',
    to: { name: 'list', query: { categoryId: 100 } }
  },
  {
    tag: '限时特惠',
    title: '家电焕新季',
    sub: '全场满 3000 减 300 · 大件免费送装',
    cta: '查看家电',
    prompt: '电商横幅背景图：现代明亮客厅中的智能电视与冰箱，暖橙色调，柔和室内光线，居中偏右构图，干净简洁，无任何文字，写实摄影，专业电商风格',
    to: { name: 'list', query: { categoryId: 200 } }
  },
  {
    tag: 'AI 上线',
    title: 'AI 导购 · 小智',
    sub: '描述你的需求，小智帮你精准选品',
    cta: '和小智聊聊',
    prompt: '科技感横幅背景图：友好的AI机器人助手形象，青绿蓝渐变光效，漂浮的数据粒子，深色背景，简洁大气，居中偏右构图，无任何文字，专业电商风格',
    to: { name: 'chat' }
  },
  {
    tag: '每日更新',
    title: '热销好物榜',
    sub: '大家都在买的爆款清单，一键直达',
    cta: '查看榜单',
    prompt: '电商横幅背景图：热烈喜庆的橙红渐变背景，漂浮的礼盒与购物袋元素，光斑粒子，简洁大气构图，无任何文字，写实电商风格',
    to: { name: 'list', query: { sortField: 'sales' } }
  }
]

const current = ref(0)
let timer = null

function go(i) {
  current.value = (i + slides.length) % slides.length
}

function openSlide(i) {
  router.push(slides[i].to)
}

function start() {
  stop()
  timer = setInterval(() => go(current.value + 1), AUTO_INTERVAL)
}

function stop() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function bannerUrl(prompt) {
  return `https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=${encodeURIComponent(prompt)}&image_size=landscape_16_9`
}

onMounted(start)
onBeforeUnmount(stop)
</script>

<template>
  <div class="banner" @mouseenter="stop" @mouseleave="start">
    <div
      v-for="(s, i) in slides"
      :key="i"
      class="slide"
      :class="{ on: i === current }"
      @click="openSlide(i)"
    >
      <img :src="bannerUrl(s.prompt)" :alt="s.title" :loading="i === 0 ? 'eager' : 'lazy'" />
      <div class="s-wrap">
        <span class="s-tag">{{ s.tag }}</span>
        <div class="s-title">{{ s.title }}</div>
        <div class="s-sub">{{ s.sub }}</div>
        <span class="s-cta">{{ s.cta }} →</span>
      </div>
    </div>

    <button class="arrow prev" aria-label="上一张" @click.stop="go(current - 1)">‹</button>
    <button class="arrow next" aria-label="下一张" @click.stop="go(current + 1)">›</button>

    <div class="dots">
      <span
        v-for="(s, i) in slides"
        :key="i"
        class="dot"
        :class="{ on: i === current }"
        @click="go(i)"
      ></span>
    </div>
  </div>
</template>

<style scoped>
.banner {
  position: relative;
  height: 340px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.slide {
  position: absolute;
  inset: 0;
  opacity: 0;
  transform: translateX(30px);
  transition: opacity 0.5s ease, transform 0.5s ease;
  cursor: pointer;
}

.slide.on {
  opacity: 1;
  transform: none;
  z-index: 1;
}

.slide img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  background: linear-gradient(115deg, #2b3a6b, #6b4fa8);
}

.slide::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(10, 14, 26, 0.62), rgba(10, 14, 26, 0.18) 55%, transparent);
}

.s-wrap {
  position: absolute;
  left: 56px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  color: #fff;
  max-width: 520px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.s-tag {
  display: inline-block;
  font-size: 12px;
  letter-spacing: 2px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  padding: 3px 12px;
}

.s-title {
  font-size: 34px;
  font-weight: 800;
  margin-top: 14px;
  letter-spacing: 1px;
}

.s-sub {
  font-size: 15px;
  opacity: 0.95;
  margin-top: 10px;
}

.s-cta {
  display: inline-block;
  margin-top: 22px;
  background: #fff;
  color: #1f2430;
  border-radius: 8px;
  padding: 10px 22px;
  font-size: 14px;
  font-weight: 700;
  transition: all 0.18s;
}

.slide:hover .s-cta {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.25);
}

.arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.32);
  color: #fff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  z-index: 3;
  opacity: 0;
  transition: opacity 0.2s, background 0.18s;
}

.banner:hover .arrow {
  opacity: 1;
}

.arrow:hover {
  background: rgba(0, 0, 0, 0.55);
}

.arrow.prev {
  left: 14px;
}

.arrow.next {
  right: 14px;
}

.dots {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 14px;
  display: flex;
  justify-content: center;
  gap: 8px;
  z-index: 3;
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.55);
  cursor: pointer;
  transition: all 0.25s;
}

.dot.on {
  width: 22px;
  background: #fff;
}

@media (max-width: 900px) {
  .banner {
    height: 260px;
  }

  .s-wrap {
    left: 24px;
  }

  .s-title {
    font-size: 26px;
  }
}
</style>

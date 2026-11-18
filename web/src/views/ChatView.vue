<script setup>
import { ref, nextTick, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { clearToken, getToken, userState } from '@/store/user'

const agentBase = import.meta.env.VITE_AGENT_BASE || ''

const router = useRouter()
const messages = ref([])
const input = ref('')
const listEl = ref(null)
const sending = ref(false)

const quickPrompts = [
  '预算3000左右送女生的手机',
  '露营需要准备什么装备',
  '有什么热销好物推荐',
  '小米手环9值得买吗'
]

function now() {
  const d = new Date()
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function scrollTop() {
  nextTick(() => {
    if (listEl.value) listEl.value.scrollTop = listEl.value.scrollHeight
  })
}

const ANON_KEY = 'mall_agent_anon_id'

// 匿名会话 ID：localStorage 持久化，由服务端在首次请求时下发或前端自建
function anonId() {
  return localStorage.getItem(ANON_KEY) || ''
}

function setAnonId(id) {
  if (id) localStorage.setItem(ANON_KEY, id)
}

// 已登录时带会员 token，Agent 会据此把会话绑定到 member:{memberId}
function authHeaders() {
  const token = getToken()
  return token ? { Authorization: 'Bearer ' + token } : {}
}

function pushMessage(role, text, goods = null, streaming = false, time = now()) {
  messages.value.push({ role, raw: text, text, goods, streaming, time })
  scrollTop()
  // 必须返回响应式代理而不是原始对象：直接改原始对象不会触发视图更新和 watch，
  // 会导致流式打字机失效、且持久化到本地的 AI 消息内容为空
  return messages.value[messages.value.length - 1]
}

// 本地缓存仅作为后端历史接口不可用时的兜底，按登录态分开，避免匿名与会员内容互串
function historyKey() {
  return getToken() ? 'mall_agent_history_member' : 'mall_agent_history_anon'
}

function saveHistory() {
  try {
    const list = messages.value.slice(-50).map((m) => ({ role: m.role, raw: m.raw, goods: m.goods, time: m.time }))
    localStorage.setItem(historyKey(), JSON.stringify(list))
  } catch { /* 存储满或被禁用时静默跳过 */ }
}

function restoreHistory() {
  try {
    const raw = localStorage.getItem(historyKey())
    if (!raw) return false
    const list = JSON.parse(raw)
    if (!Array.isArray(list) || !list.length) return false
    messages.value = []
    for (const m of list) {
      const msg = pushMessage(m.role, m.raw || '', m.goods || null, false, m.time || '')
      renderStreaming(msg)
    }
    return true
  } catch {
    return false
  }
}

// 优先从后端恢复：会话绑定会员/匿名身份，换设备或重启服务后仍能接上
async function loadHistory() {
  const params = new URLSearchParams({ limit: '50' })
  const anon = anonId()
  if (!getToken() && anon) params.set('anonId', anon)
  try {
    const resp = await fetch(agentBase + '/agent/history?' + params.toString(), { headers: authHeaders() })
    if (resp.status === 401) {
      clearToken()
      return false
    }
    if (!resp.ok) return false
    const data = await resp.json()
    setAnonId(data.anonId)
    const list = data.messages || []
    if (!list.length) return false
    messages.value = []
    for (const item of list) {
      const msg = pushMessage(item.role, item.text || '', item.goods || [], false, '')
      renderStreaming(msg)
    }
    return true
  } catch {
    return false
  }
}

watch(messages, saveHistory, { deep: true })

function stripLinks(text) {
  return (text || '')
    .replace(/!\[[^\]]*\]\([^)\s]*\)?/g, '')
    .replace(/\[([^\]]+)\]\([^)\s]*\)?/g, '$1')
    .replace(/https?:\/\/[^\s)]+/g, '')
}

function renderStreaming(msg) {
  // RECOMMEND 可能独占一行，也可能被模型直接接在正文末尾，
  // 所以只能剥掉标记及其后面的内容，不能整行过滤，否则正文会被一起删掉
  let text = stripLinks(msg.raw).replace(/RECOMMEND\s*[:：].*$/gim, '')
  if (msg.streaming) {
    // 流式过程中标记可能只到了一半，避免闪出 "RECOMM" 之类的残字
    text = text.replace(/RECOMM[A-Z]*\s*[:：]?[^\n]*$/, '')
  }
  msg.text = text
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

function handleEvent(msg, block) {
  let event = 'message'
  let dataStr = ''
  for (const line of block.split('\n')) {
    if (line.startsWith('event:')) event = line.slice(6).trim()
    else if (line.startsWith('data:')) dataStr += line.slice(5).trim()
  }
  if (!dataStr) return
  let data
  try {
    data = JSON.parse(dataStr)
  } catch {
    return
  }

  if (event === 'delta' && data.text) {
    msg.raw += data.text
    renderStreaming(msg)
    scrollTop()
  } else if (event === 'replace' && data.text) {
    // 服务端对脱离检索结果的回答做了纠正重写，以最终正文为准
    msg.raw = data.text
    renderStreaming(msg)
    scrollTop()
  } else if (event === 'goods' && Array.isArray(data)) {
    msg.goods = data
    scrollTop()
  } else if (event === 'error') {
    msg.raw += (msg.raw ? '\n' : '') + (data.message || '服务开小差了，请稍后再试')
    renderStreaming(msg)
  }
}

async function send(preset) {
  const text = (preset || input.value).trim()
  if (!text || sending.value) return
  input.value = ''
  pushMessage('user', text)
  const aiMsg = pushMessage('ai', '', null, true)
  sending.value = true
  scrollTop()

  try {
    const resp = await fetch(agentBase + '/agent/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeaders() },
      body: JSON.stringify({ anonId: anonId(), message: text })
    })
    const newAnon = resp.headers.get('X-Anon-Id')
    if (newAnon) setAnonId(newAnon)
    if (resp.status === 401) {
      clearToken()
      throw new Error('401')
    }
    if (!resp.ok || !resp.body) throw new Error('HTTP ' + resp.status)

    const reader = resp.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      let idx
      while ((idx = buffer.indexOf('\n\n')) !== -1) {
        const block = buffer.slice(0, idx)
        buffer = buffer.slice(idx + 2)
        handleEvent(aiMsg, block)
      }
    }
    if (buffer.trim()) handleEvent(aiMsg, buffer)
    aiMsg.streaming = false
    renderStreaming(aiMsg)
  } catch (e) {
    aiMsg.streaming = false
    if (!aiMsg.raw) {
      aiMsg.text = e.message === '401'
        ? '登录已过期，已切换为游客会话，请重新登录后继续～'
        : '小橙暂时联系不上服务，请确认 AI 导购服务（mall-agent :8090）已启动～'
    }
  } finally {
    sending.value = false
    scrollTop()
  }
}

const GREETING = '您好！我是 AI 导购小橙 🤖\n\n我已接入真实 AI 能力，可以：\n· 理解口语化需求做语义选品（如"露营带什么"）\n· 查真实库存、价格、参数，回答商品咨询\n· 推荐可点击跳转的真实商品\n\n试试下面的快捷提问，或直接描述您的需求～'

async function bootstrap() {
  if (await loadHistory()) return
  if (restoreHistory()) return
  pushMessage('ai', GREETING)
}

// 登录/登出后切换会话身份，重新拉取对应的历史，避免匿名与会员内容混在一起
watch(() => userState.token, async () => {
  messages.value = []
  if (await loadHistory()) return
  pushMessage('ai', GREETING)
})

onMounted(bootstrap)
</script>

<template>
  <div class="container">
    <div class="chat-card card">
      <div class="chat-head">
        <div class="ch-left">
          <span class="avatar">🤖</span>
          <div class="ch-title">
            <div class="t">AI 导购小橙</div>
            <div class="s">🟢 在线 · RAG 商品知识库加持</div>
          </div>
        </div>
        <span class="badge">真 AI 已接入 · 智能选品</span>
      </div>

      <div ref="listEl" class="chat-list">
        <div v-for="(msg, idx) in messages" :key="idx" class="msg-row" :class="msg.role">
          <div v-if="msg.role === 'ai'" class="avatar">🤖</div>
          <div class="bubble-wrap">
            <div class="bubble">
              <div v-if="msg.streaming && !msg.text" class="thinking"><i></i><i></i><i></i></div>
              <div class="text">{{ msg.text }}</div>
              <div v-if="msg.goods && msg.goods.length" class="bubble-goods">
                <div v-for="g in msg.goods" :key="g.goodsId" class="b-goods" @click="router.push(`/detail/${g.goodsId}`)">
                  <img :src="g.imageUrl" />
                  <div class="bg-info">
                    <div class="bg-name">{{ g.goodsName }}</div>
                    <div class="bg-price price"><span class="rmb">¥</span>{{ g.price }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div class="time">{{ msg.time }}</div>
          </div>
          <div v-if="msg.role === 'user'" class="avatar user">🧑</div>
        </div>
      </div>

      <div class="chat-foot">
        <div class="quick-prompts">
          <span
            v-for="qp in quickPrompts"
            :key="qp"
            class="qp-chip"
            :class="{ disabled: sending }"
            @click="send(qp)"
          >{{ qp }}</span>
        </div>
        <div class="chat-input">
          <input v-model="input" :disabled="sending" placeholder="描述您的需求，如：推荐一款适合学生的笔记本" @keyup.enter="send()" />
          <button class="btn-primary send" :disabled="sending" @click="send()">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-card {
  height: calc(100vh - 290px);
  min-height: 520px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--line);
  background: linear-gradient(90deg, var(--primary-soft), #fff);
}

.ch-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
  box-shadow: var(--shadow);
}

.ch-title .t {
  font-size: 16px;
  font-weight: 700;
}

.ch-title .s {
  font-size: 12px;
  color: var(--text-sub);
  margin-top: 2px;
}

.badge {
  font-size: 12px;
  color: var(--primary);
  border: 1px solid var(--primary);
  border-radius: 12px;
  padding: 4px 12px;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #f7f8fa;
}

.msg-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: flex-start;
}

.msg-row.user {
  flex-direction: row-reverse;
}

.bubble-wrap {
  max-width: 640px;
}

.bubble {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 4px 14px 14px 14px;
  padding: 14px 18px;
  font-size: 14px;
  line-height: 1.7;
  box-shadow: var(--shadow);
}

.msg-row.user .bubble {
  background: var(--primary-grad);
  color: #fff;
  border: none;
  border-radius: 14px 4px 14px 14px;
}

.bubble .text {
  white-space: pre-wrap;
  word-break: break-word;
}

.thinking {
  display: flex;
  gap: 5px;
  padding: 4px 0;
}

.thinking i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--primary);
  opacity: 0.4;
  animation: blink 1.2s infinite;
}

.thinking i:nth-child(2) {
  animation-delay: 0.2s;
}

.thinking i:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes blink {
  0%, 80%, 100% { opacity: 0.25; }
  40% { opacity: 1; }
}

.time {
  font-size: 11px;
  color: #a8adb5;
  margin-top: 6px;
  padding-left: 4px;
}

.msg-row.user .time {
  text-align: right;
  padding-right: 4px;
}

.bubble-goods {
  margin-top: 14px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.b-goods {
  display: flex;
  gap: 12px;
  background: #f7f8fa;
  border-radius: 10px;
  padding: 10px;
  cursor: pointer;
  align-items: center;
  transition: all 0.18s;
}

.b-goods:hover {
  background: var(--primary-soft);
}

.b-goods img {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.bg-info {
  min-width: 0;
}

.bg-name {
  font-size: 12px;
  color: var(--text);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 6px;
}

.bg-price {
  font-size: 15px;
}

.chat-foot {
  padding: 14px 20px 18px;
  background: #fff;
  border-top: 1px solid var(--line);
}

.quick-prompts {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.qp-chip {
  font-size: 12px;
  color: var(--text-sub);
  border: 1px solid #e3e5ea;
  border-radius: 16px;
  padding: 5px 14px;
  cursor: pointer;
  transition: all 0.18s;
}

.qp-chip:hover {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-soft);
}

.qp-chip.disabled {
  opacity: 0.55;
  pointer-events: none;
}

.chat-input {
  display: flex;
  gap: 12px;
}

.chat-input input {
  flex: 1;
  border: 1px solid #dcdfe6;
  outline: none;
  background: #f7f8fa;
  border-radius: 10px;
  padding: 0 18px;
  height: 46px;
  font-size: 14px;
  transition: all 0.18s;
  min-width: 0;
}

.chat-input input:focus {
  border-color: var(--primary);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(255, 96, 52, 0.1);
}

.chat-input input:disabled {
  opacity: 0.7;
}

.send {
  height: 46px;
  padding: 0 32px;
}

.send:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>

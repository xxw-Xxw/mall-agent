import { reactive, createApp } from 'vue'
import ToastHost from '@/components/ToastHost.vue'

const state = reactive({ messages: [] })
let seed = 0

const host = createApp(ToastHost, { state })
const div = document.createElement('div')
host.mount(div)
document.body.appendChild(div)

function push(message, type) {
  const id = ++seed
  state.messages.push({ id, message, type })
  setTimeout(() => {
    const idx = state.messages.findIndex((m) => m.id === id)
    if (idx > -1) state.messages.splice(idx, 1)
  }, 2600)
}

export default {
  success: (msg) => push(msg, 'success'),
  error: (msg) => push(msg, 'error'),
  info: (msg) => push(msg, 'info')
}

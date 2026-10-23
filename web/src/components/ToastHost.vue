<script setup>
defineProps({
  state: { type: Object, required: true }
})
</script>

<template>
  <teleport to="body">
    <div class="toast-wrap">
      <div
        v-for="m in state.messages"
        :key="m.id"
        class="toast-item"
        :class="m.type"
      >
        <span class="dot">{{ m.type === 'success' ? '✓' : m.type === 'error' ? '✕' : 'i' }}</span>
        {{ m.message }}
      </div>
    </div>
  </teleport>
</template>

<style scoped>
.toast-wrap {
  position: fixed;
  top: 14%;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  z-index: 9999;
  pointer-events: none;
}

.toast-item {
  background: rgba(30, 32, 38, 0.88);
  color: #fff;
  font-size: 13px;
  padding: 9px 18px;
  border-radius: 22px;
  max-width: 76%;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.18);
  animation: toast-in 0.22s ease;
}

.dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.toast-item.success .dot {
  background: #37c079;
}

.toast-item.error .dot {
  background: #f0564a;
}

.toast-item.info .dot {
  background: #4d90fb;
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
</style>

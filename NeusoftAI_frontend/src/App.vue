<template>
  <div id="app">
    <!-- 全局 Toast 容器 -->
    <div class="toast-container">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="toast-item"
        :class="'toast-' + toast.type"
      >{{ toast.message }}</div>
    </div>

    <!-- 路由视图（带过渡动画） -->
    <router-view v-slot="{ Component, route }">
      <transition :name="transitionName" mode="out-in" @before-leave="onBeforeLeave" @after-enter="onAfterEnter">
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const toasts = ref([])
let toastId = 0
const transitionName = ref('page-slide')

// 全局挂载 showToast 函数
window.showToast = function(message, type = 'success') {
  const id = ++toastId
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }, 2800)
}

function onBeforeLeave(el) {
  el.style.position = 'absolute'
  el.style.width = '100%'
}
</script>

<style>
#app { min-height: 100vh; }

/* ===== 页面切换过渡动画 ===== */
.page-slide-enter-active,
.page-slide-leave-active {
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}
.page-slide-enter-from {
  opacity: 0;
  transform: translateY(18px) scale(0.99);
  filter: blur(2px);
}
.page-slide-leave-to {
  opacity: 0;
  transform: translateY(-12px) scale(0.98);
}

/* 淡入淡出备用 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

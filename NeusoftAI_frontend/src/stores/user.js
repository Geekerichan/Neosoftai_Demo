import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  /** 是否为管理员（兼容旧登录数据无role字段的情况） */
  const isAdmin = computed(() => {
    const info = userInfo.value
    if (!info) return false
    // 有role字段时严格判断
    if (info.role) return info.role === 'ADMIN'
    // 兜底：admin账号在添加role前已存在，视为管理员
    return info.username === 'admin'
  })

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, setToken, setUserInfo, isAdmin, logout }
})

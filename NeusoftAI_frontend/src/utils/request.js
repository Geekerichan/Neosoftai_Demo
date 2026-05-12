import axios from 'axios'
import { useUserStore } from '../stores/user'
import router from '../router'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 120000, // AI诊断可能耗时较长，设置较长超时
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：自动附加 JWT Token
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  response => {
    // 文件下载类响应（blob）直接透传，不做JSON校验
    if (response.config.responseType === 'blob') {
      return response
    }

    const data = response.data
    if (data.code === 200) {
      return data
    } else {
      // 业务错误
      showToast(data.message || '请求失败', 'error')
      return Promise.reject(new Error(data.message))
    }
  },
  error => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        // Token 过期或未登录，跳转登录页
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
        showToast('登录已过期，请重新登录', 'error')
      } else if (status === 403) {
        showToast('没有权限访问', 'error')
      } else {
        showToast(error.response.data?.message || '网络错误', 'error')
      }
    } else {
      showToast('网络连接失败', 'error')
    }
    return Promise.reject(error)
  }
)

// Toast 提示函数（全局挂载）
function showToast(msg, type = 'success') {
  window.showToast?.(msg, type)
}

export default request

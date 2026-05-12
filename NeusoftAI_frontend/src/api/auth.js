import request from '../utils/request'

/**
 * 认证相关 API
 */
export const authApi = {
  /** 登录 */
  login(username, password) {
    const params = new URLSearchParams()
    params.append('username', username)
    params.append('password', password)
    return request.post('/auth/login', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
  },

  /** 注册 */
  register(username, password) {
    const params = new URLSearchParams()
    params.append('username', username)
    params.append('password', password)
    return request.post('/auth/register', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
  },

  /** 登出 */
  logout() {
    return request.post('/auth/logout')
  }
}

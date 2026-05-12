import request from '../utils/request'

/**
 * 故障咨询相关 API
 */
export const faultApi = {
  /** 文本故障诊断 */
  diagnose(description, faultType = 'OTHER') {
    const params = new URLSearchParams()
    params.append('description', description)
    params.append('faultType', faultType)
    return request.post('/fault/diagnose', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
  },

  /** 图片故障诊断 */
  diagnoseByImage(imageFile, description = '', faultType = 'OTHER') {
    const formData = new FormData()
    formData.append('image', imageFile)
    if (description) formData.append('description', description)
    formData.append('faultType', faultType)
    return request.post('/fault/diagnose-image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /** 多轮追问 */
  followUp(recordId, followUpContent) {
    const params = new URLSearchParams()
    params.append('recordId', recordId)
    params.append('followUpContent', followUpContent)
    return request.post('/fault/followUp', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
  },

  /** 方案优化 */
  optimize(recordId, feedback = '') {
    const params = new URLSearchParams()
    params.append('recordId', recordId)
    if (feedback) params.append('feedback', feedback)
    return request.post('/fault/optimize', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
  },

  /** 点赞 */
  like(id) {
    return request.post(`/fault/like/${id}`)
  }
}

/**
 * 记录管理相关 API
 */
export const recordApi = {
  /** 分页查询记录 */
  list(params) {
    const query = new URLSearchParams(params).toString()
    return request.get(`/records/list?${query}`)
  },

  /** 删除记录 */
  delete(id) {
    return request.delete(`/records/${id}`)
  }
}

/**
 * 每日常识 API
 */
export const tipApi = {
  /** 获取运维常识（带时间戳防缓存，每次刷新返回新内容） */
  getToday() {
    return request.get(`/tip/today?t=${Date.now()}`)
  }
}

/**
 * 设备资产库 API
 */
export const deviceApi = {
  list(params) {
    const query = new URLSearchParams(params).toString()
    return request.get(`/device/list?${query}`)
  },
  add(device) {
    return request.post('/device/add', device)
  },
  update(device) {
    return request.put('/device/update', device)
  },
  delete(id) {
    return request.delete(`/device/${id}`)
  },
  detail(id) {
    return request.get(`/device/${id}`)
  }
}

/**
 * 运维知识库 API
 */
export const knowledgeApi = {
  list(params) {
    const query = new URLSearchParams(params).toString()
    return request.get(`/knowledge/list?${query}`)
  },
  detail(id) {
    return request.get(`/knowledge/${id}`)
  },
  add(kb) {
    return request.post('/knowledge/add', kb)
  },
  update(kb) {
    return request.put('/knowledge/update', kb)
  },
  delete(id) {
    return request.delete(`/knowledge/${id}`)
  },
  like(id) {
    return request.post(`/knowledge/like/${id}`)
  }
}

/**
 * 系统公告 API
 */
export const announceApi = {
  list(params) {
    const query = new URLSearchParams(params).toString()
    return request.get(`/announcement/list?${query}`)
  },
  latest() {
    return request.get('/announcement/latest')
  },
  add(announce) {
    return request.post('/announcement/add', announce)
  },
  update(announce) {
    return request.put('/announcement/update', announce)
  },
  delete(id) {
    return request.delete(`/announcement/${id}`)
  }
}

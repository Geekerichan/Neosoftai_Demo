<template>
  <div>
    <!-- 毛玻璃导航栏 -->
    <nav class="navbar">
      <router-link to="/" class="brand">
        <span class="brand-icon">&#9881;</span> 东软智能运维
        <span>AI-Powered O&M</span>
      </router-link>
      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/consult" class="nav-link">故障咨询</router-link>
        <router-link to="/records" class="nav-link active">我的记录</router-link>
        <router-link to="/devices" class="nav-link">设备资产</router-link>
        <router-link to="/knowledge" class="nav-link">知识库</router-link>
        <span class="nav-user-info">&#128100; {{ userStore.userInfo && userStore.userInfo.username }}</span>
        <button class="btn-logout" @click="handleLogout">退出</button>
      </div>
    </nav>

    <!-- 主内容区 -->
    <div class="main-content">
      <div class="card records-card animate-in">
        <div class="card-header">
          <h3>&#128203; 故障咨询记录管理</h3>
          <span class="header-count">{{ total }} 条记录</span>
        </div>
        <div class="card-body">
          <!-- 工具栏 -->
          <div class="toolbar">
            <div class="filter-group">
              <span class="filter-label">筛选类型：</span>
              <button v-for="f in filters" :key="f.value"
                class="filter-btn" :class="{ active: currentFilter === f.value }"
                @click="filterRecords(f.value)"
              >{{ f.label }}</button>
            </div>
            <span class="export-hint">点击每行「导出」按钮可单独导出该条记录为文本文件</span>
          </div>

          <!-- 表格 -->
          <div class="table-wrap">
            <table class="records-table">
              <thead>
                <tr>
                  <th width="60">ID</th>
                  <th width="90">故障类型</th>
                  <th>故障描述</th>
                  <th>解决方案</th>
                  <th width="70">点赞</th>
                  <th width="130">咨询时间</th>
                  <th width="80">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="loading">
                  <td colspan="7">
                    <div class="empty-state"><p style="animation: float 2s ease-in-out infinite;">加载中...</p></div>
                  </td>
                </tr>
                <tr v-else-if="records.length === 0">
                  <td colspan="7">
                    <div class="empty-state"><div class="icon">&#128221;</div><p>暂无符合条件的记录</p></div>
                  </td>
                </tr>
                <template v-else>
                  <tr v-for="(r, idx) in records" :key="r.id" :style="{ animationDelay: (idx * 0.03) + 's' }" class="row-animate">
                    <td><span class="id-num">{{ r.id }}</span></td>
                    <td><span class="badge-sm" :class="'type-' + r.faultType">{{ getTypeName(r.faultType) }}</span></td>
                    <td class="desc-cell">{{ r.faultDescription }}</td>
                    <td class="solution-cell">
                      {{ r.solution }}
                      <div v-if="r.optimized === 1 && r.optimizedSolution" class="optimized-solution">
                        <span class="optimized-label">优化方案:</span> {{ r.optimizedSolution }}
                      </div>
                    </td>
                    <td><span class="like-count">{{ r.likeCount || 0 }}</span></td>
                    <td>{{ formatTime(r.createTime) }}</td>
                    <td>
                      <button class="btn-row-export" @click="exportSingleRecord(r)" title="导出此条记录">&#128190; 导出</button>
                      <button class="btn-delete" @click="deleteRecord(r.id)">删除</button>
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>

          <!-- 分页 -->
          <div class="pagination" v-if="totalPages > 1">
            <span class="page-info">共 {{ total }} 条 · 第 {{ currentPage }}/{{ totalPages }} 页</span>
            <button class="page-btn" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">
              &larr; 上一页
            </button>
            <button v-for="page in pageNumbers" :key="page"
              class="page-btn" :class="{ active: page === currentPage }"
              @click="goPage(page)"
            >{{ page }}</button>
            <button class="page-btn" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">
              下一页 &rarr;
            </button>
          </div>
          <div class="pagination" v-else-if="total > 0">
            <span class="page-info">共 {{ total }} 条记录</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { recordApi } from '../api'
import { getTypeName, formatTime } from '../utils/helpers'

const router = useRouter()
const userStore = useUserStore()

const filters = [
  { label: '全部', value: 'ALL' },
  { label: '服务器', value: 'SERVER' },
  { label: '办公设备', value: 'OFFICE' },
  { label: '其他', value: 'OTHER' }
]

const loading = ref(false)
const currentFilter = ref('ALL')
const currentPage = ref(1)
const pageSize = 8
const records = ref([])
const total = ref(0)

const totalPages = computed(() => Math.ceil(total.value / pageSize))
const pageNumbers = computed(() => {
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  const arr = []
  for (let i = start; i <= end; i++) arr.push(i)
  return arr
})

/* 工具函数已提取至 utils/helpers.js */

function filterRecords(type) { currentFilter.value = type; currentPage.value = 1; loadRecords() }

async function loadRecords() {
  loading.value = true
  try {
    const res = await recordApi.list({ faultType: currentFilter.value, current: currentPage.value, size: pageSize })
    records.value = res.data?.records || []; total.value = res.data?.total || 0
  } catch (e) { records.value = []; total.value = 0 } finally { loading.value = false }
}

function goPage(page) { if (page < 1 || page > totalPages.value) return; currentPage.value = page; loadRecords() }

async function deleteRecord(id) { if (!confirm('确定要删除这条记录吗？')) return; try { await recordApi.delete(id); window.showToast?.('删除成功', 'success'); await loadRecords() } catch (e) {} }

/** 导出单条记录为 txt 文件 */
function exportSingleRecord(r) {
  const lines = [
    '========================================',
    '        东软智能运维 - 故障咨询记录',
    '========================================',
    '',
    `记录编号: ${r.id}`,
    `故障类型: ${getTypeName(r.faultType)}`,
    `咨询时间: ${formatTime(r.createTime)}`,
    `点赞数量: ${r.likeCount || 0}`,
    '',
    '----------------------------------------',
    '故障描述:',
    '----------------------------------------',
    r.faultDescription || '(无)',
    '',
    '----------------------------------------',
    '解决方案:',
    '----------------------------------------',
    (r.solution || '(暂无)').trim(),
    ''
  ]
  if (r.optimized === 1 && r.optimizedSolution) {
    lines.push('----------------------------------------')
    lines.push('优化方案:')
    lines.push('----------------------------------------')
    lines.push(r.optimizedSolution.trim())
    lines.push('')
  }
  lines.push('========================================')
  lines.push(`导出时间: ${new Date().toLocaleString('zh-CN')}`)
  lines.push('========================================')

  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `故障咨询记录_${r.id}_${new Date().toISOString().slice(0,10)}.txt`
  document.body.appendChild(link); link.click(); document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
  window.showToast?.('已导出 #' + r.id + ' 记录', 'success')
}

async function handleLogout() { userStore.logout(); router.push('/login') }
onMounted(() => { loadRecords() })
</script>

<style scoped>
.records-card { overflow: hidden; }

.header-count {
  font-size: 13px; color: var(--text-muted); font-weight: 500;
  padding: 4px 14px; border-radius: 20px; background: var(--bg-warm);
}

.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 22px; flex-wrap: wrap; gap: 12px; }
.filter-group { display: flex; gap: 8px; align-items: center; }
.filter-label { font-size: 14px; color: var(--text-secondary); font-weight: 600; }

.filter-btn {
  padding: 7px 18px; border: 2px solid var(--border); border-radius: 24px; background: var(--card-solid);
  cursor: pointer; font-size: 13px; font-weight: 600; transition: all 0.28s var(--ease-out); color: var(--text-secondary);
}
.filter-btn:hover { border-color: var(--primary-light); color: var(--primary); }
.filter-btn.active { border-color: var(--primary); background: var(--info-light); color: var(--primary-dark); }

.export-hint { font-size: 12px; color: var(--text-muted); font-weight: 500; }

.btn-row-export {
  background: none; border: 1.5px solid var(--success); color: #059669;
  padding: 4px 12px; border-radius: 8px; cursor: pointer; font-size: 12px; font-weight: 600;
  transition: all 0.25s var(--ease-out); margin-right: 4px;
}
.btn-row-export:hover { background: var(--success-light); transform: translateY(-1px); }

.btn-delete {
  background: none; border: 1.5px solid var(--danger); color: var(--danger);
  padding: 4px 12px; border-radius: 8px; cursor: pointer; font-size: 12px; font-weight: 600;
  transition: all 0.25s var(--ease-out);
}
.btn-delete:hover { background: var(--danger-light); transform: translateY(-1px); }

/* 表格 */
.table-wrap { overflow-x: auto; border-radius: var(--radius); border: 1px solid var(--border-light); }
.records-table { width: 100%; border-collapse: collapse; min-width: 700px; }
.records-table th {
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  padding: 13px 16px; text-align: left; font-size: 12px; font-weight: 700;
  color: var(--text-secondary); letter-spacing: 0.5px; text-transform: uppercase;
  border-bottom: 2px solid var(--border);
}
.records-table td {
  padding: 14px 16px; border-bottom: 1px solid var(--border-light);
  font-size: 14px; vertical-align: top; transition: background 0.2s;
}
.row-animate { animation: fadeInUp 0.35s ease both; }
.records-table tr:hover td { background: rgba(79,110,247,0.02); }
.records-table tbody tr:last-child td { border-bottom: none; }

.id-num {
  display: inline-block; font-weight: 700; color: var(--primary);
  font-size: 13px; min-width: 28px; text-align: center;
}
.desc-cell { max-width: 260px; line-height: 1.55; white-space: pre-wrap; word-break: break-all; color: var(--text); }
.solution-cell { max-width: 300px; line-height: 1.65; white-space: pre-wrap; word-break: break-all; font-size: 13px; color: var(--text-secondary); }

/* 分页 */
.pagination { display: flex; justify-content: center; align-items: center; gap: 8px; margin-top: 28px; flex-wrap: wrap; }
.page-btn {
  padding: 8px 15px; border: 1.5px solid var(--border); border-radius: 10px; background: var(--card-solid);
  cursor: pointer; font-size: 13px; font-weight: 600; transition: all 0.25s var(--ease-out); color: var(--text-secondary);
}
.page-btn:hover:not(:disabled):not(.active) { border-color: var(--primary-light); color: var(--primary); }
.page-btn.active { background: linear-gradient(135deg, var(--primary), #6366f1); color: #fff; border-color: transparent; box-shadow: 0 3px 10px rgba(79,110,247,0.3); }
.page-btn:disabled { opacity: 0.4; cursor: default; }
.page-info { font-size: 13px; color: var(--text-muted); font-weight: 500; }

.optimized-solution {
  margin-top: 7px; padding: 9px 12px; background: var(--success-light);
  border-left: 3px solid var(--success); border-radius: 0 8px 8px 0;
  font-size: 12px; line-height: 1.55; color: #047857;
}
.optimized-label { font-size: 11px; font-weight: 800; color: var(--success); }
</style>

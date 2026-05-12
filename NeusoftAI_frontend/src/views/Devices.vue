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
        <router-link to="/records" class="nav-link">我的记录</router-link>
        <router-link to="/devices" class="nav-link active">设备资产</router-link>
        <router-link to="/knowledge" class="nav-link">知识库</router-link>
        <span class="nav-user-info">&#128100; {{ userStore.userInfo && userStore.userInfo.username }}</span>
        <button class="btn-logout" @click="handleLogout">退出</button>
      </div>
    </nav>

    <!-- 主内容区 -->
    <div class="main-content">
      <div class="card devices-card animate-in">
        <div class="card-header">
          <h3>&#128187; 设备资产管理</h3>
          <button v-if="userStore.isAdmin" class="btn-add" @click="openAddModal">&#43; 新增设备</button>
        </div>
        <div class="card-body">
          <!-- 工具栏 -->
          <div class="toolbar">
            <div class="filter-group">
              <input type="text" v-model="searchKeyword" placeholder="搜索设备名称/型号..." class="search-input" @input="doSearch" />
              <select v-model="filterCategory" class="filter-select" @change="loadDevices">
                <option value="ALL">全部分类</option>
                <option value="SERVER">服务器</option>
                <option value="PRINTER">打印机</option>
                <option value="NETWORK">网络设备</option>
                <option value="COMPUTER">办公电脑</option>
                <option value="OTHER">其他</option>
              </select>
              <select v-model="filterStatus" class="filter-select" @change="loadDevices">
                <option value="ALL">全部状态</option>
                <option value="ONLINE">正常</option>
                <option value="OFFLINE">离线</option>
                <option value="MAINTAINING">维修中</option>
                <option value="SCRAPPED">已报废</option>
              </select>
            </div>
          </div>

          <!-- 统计卡片 -->
          <div class="stats-row">
            <div class="stat-card stat-total"><b>{{ stats.total || 0 }}</b><span>总设备</span></div>
            <div class="stat-card stat-online"><b>{{ stats.online || 0 }}</b><span>正常</span></div>
            <div class="stat-card stat-offline"><b>{{ stats.offline || 0 }}</b><span>离线</span></div>
            <div class="stat-card stat-maintain"><b>{{ stats.maintaining || 0 }}</b><span>维修中</span></div>
          </div>

          <!-- 表格 -->
          <table class="data-table">
            <thead>
              <tr>
                <th width="60">ID</th>
                <th width="140">设备名称</th>
                <th width="120">型号</th>
                <th width="90">分类</th>
                <th width="80">状态</th>
                <th width="120">位置</th>
                <th width="100">负责人</th>
                <th width="130">录入时间</th>
                <th width="120">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td :colspan="9"><div class="empty-state"><p>加载中...</p></div></td>
              </tr>
              <tr v-else-if="devices.length === 0">
                <td :colspan="9"><div class="empty-state"><div class="icon">&#128187;</div><p>暂无设备数据</p></div></td>
              </tr>
              <template v-else>
                <tr v-for="d in devices" :key="d.id">
                  <td>{{ d.id }}</td>
                  <td><strong>{{ d.deviceName }}</strong></td>
                  <td>{{ d.modelNumber || '-' }}</td>
                  <td><span class="badge-sm" :class="'cat-' + d.category">{{ getCategoryName(d.category) }}</span></td>
                  <td><span class="status-dot" :class="'status-' + d.status"></span> {{ getStatusName(d.status) }}</td>
                  <td>{{ d.location || '-' }}</td>
                  <td>{{ d.responsiblePerson || '-' }}</td>
                  <td>{{ formatTime(d.createTime) }}</td>
                  <td>
                    <template v-if="userStore.isAdmin">
                      <button class="btn-action btn-edit" @click="openEditModal(d)">编辑</button>
                      <button class="btn-action btn-del" @click="deleteDevice(d.id)">删除</button>
                    </template>
                    <span v-else style="color:#aaa;font-size:12px;">--</span>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>

          <!-- 分页 -->
          <div class="pagination" v-if="totalPages > 1">
            <span class="page-info">共 {{ total }} 条 · 第 {{ currentPage }}/{{ totalPages }} 页</span>
            <button class="page-btn" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">&lt;上一页</button>
            <button v-for="page in pageNumbers" :key="page" class="page-btn" :class="{ active: page === currentPage }" @click="goPage(page)">{{ page }}</button>
            <button class="page-btn" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">下一页&gt;</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div class="modal-overlay" :class="{ show: showModal }">
      <div class="modal-box modal-lg">
        <h3>{{ isEdit ? '&#9998; 编辑设备' : '&#43; 新增设备' }}</h3>
        <form @submit.prevent="submitForm" class="device-form">
          <div class="form-row">
            <div class="form-group">
              <label>设备名称 <span class="required">*</span></label>
              <input type="text" v-model="form.deviceName" placeholder="如：Dell R740服务器" required />
            </div>
            <div class="form-group">
              <label>型号</label>
              <input type="text" v-model="form.modelNumber" placeholder="如：R740-XC42" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>分类</label>
              <select v-model="form.category">
                <option value="SERVER">服务器</option>
                <option value="PRINTER">打印机</option>
                <option value="NETWORK">网络设备</option>
                <option value="COMPUTER">办公电脑</option>
                <option value="OTHER">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label>状态</label>
              <select v-model="form.status">
                <option value="ONLINE">正常</option>
                <option value="OFFLINE">离线</option>
                <option value="MAINTAINING">维修中</option>
                <option value="SCRAPPED">已报废</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>位置/机房编号</label>
              <input type="text" v-model="form.location" placeholder="如：A栋机房-机柜03" />
            </div>
            <div class="form-group">
              <label>负责人</label>
              <input type="text" v-model="form.responsiblePerson" placeholder="负责人姓名" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>购买日期</label>
              <input type="date" v-model="form.purchaseDate" />
            </div>
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea v-model="form.remark" rows="3" placeholder="设备补充信息..."></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn-modal-cancel" @click="closeModal">取消</button>
            <button type="submit" class="btn-modal-ok">{{ isEdit ? '保存修改' : '确认添加' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { deviceApi } from '../api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const devices = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const searchKeyword = ref('')
const filterCategory = ref('ALL')
const filterStatus = ref('ALL')

// 弹窗
const showModal = ref(false)
const isEdit = ref(false)
const form = ref({ deviceName: '', modelNumber: '', category: 'SERVER', status: 'ONLINE', location: '', responsiblePerson: '', purchaseDate: '', remark: '' })
let editId = null

// 统计
const stats = ref({ total: 0, online: 0, offline: 0, maintaining: 0 })

const totalPages = computed(() => Math.ceil(total.value / pageSize))
const pageNumbers = computed(() => {
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  const arr = []
  for (let i = start; i <= end; i++) arr.push(i)
  return arr
})

function getCategoryName(c) {
  return { SERVER: '服务器', PRINTER: '打印机', NETWORK: '网络设备', COMPUTER: '办公电脑', OTHER: '其他' }[c] || c
}
function getStatusName(s) {
  return { ONLINE: '正常', OFFLINE: '离线', MAINTAINING: '维修中', SCRAPPED: '已报废' }[s] || s
}
function formatTime(t) { if (!t) return ''; return t.substring(0, 16).replace('T', ' ') }

let searchTimer = null
function doSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 1; loadDevices() }, 300)
}

async function loadDevices() {
  loading.value = true
  try {
    const res = await deviceApi.list({ keyword: searchKeyword.value, category: filterCategory.value, status: filterStatus.value, current: currentPage.value, size: pageSize })
    devices.value = res.data?.records || []
    total.value = res.data?.total || 0

    // 计算统计（从全量数据）
    const allRes = await deviceApi.list({ keyword: searchKeyword.value, category: filterCategory.value, status: 'ALL', current: 1, size: 1000 })
    const list = allRes.data?.records || []
    stats.value = { total: list.length, online: list.filter(d => d.status === 'ONLINE').length, offline: list.filter(d => d.status === 'OFFLINE').length, maintaining: list.filter(d => d.status === 'MAINTAINING').length }
  } catch (e) {} finally { loading.value = false }
}

function goPage(p) { if (p < 1 || p > totalPages.value) return; currentPage.value = p; loadDevices() }

function openAddModal() {
  isEdit.value = false
  editId = null
  form.value = { deviceName: '', modelNumber: '', category: 'SERVER', status: 'ONLINE', location: '', responsiblePerson: '', purchaseDate: '', remark: '' }
  showModal.value = true
}

function openEditModal(d) {
  isEdit.value = true
  editId = d.id
  form.value = { ...d }
  showModal.value = true
}

function closeModal() { showModal.value = false }

async function submitForm() {
  try {
    if (isEdit.value) {
      await deviceApi.update({ ...form.value, id: editId })
      window.showToast?.('更新成功', 'success')
    } else {
      await deviceApi.add(form.value)
      window.showToast?.('添加成功', 'success')
    }
    closeModal()
    loadDevices()
  } catch (e) {}
}

async function deleteDevice(id) {
  if (!confirm('确定要删除该设备吗？')) return
  try { await deviceApi.delete(id); window.showToast?.('删除成功', 'success'); loadDevices() } catch (e) {}
}

function handleLogout() { userStore.logout(); router.push('/login') }

onMounted(() => loadDevices())
</script>

<style scoped>
.devices-card { overflow: hidden; }

.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:22px; flex-wrap:wrap; gap:12px; }
.filter-group { display:flex; gap:10px; align-items:center; flex-wrap:wrap; }
.search-input { padding:8px 14px; border:2px solid var(--border); border-radius:8px; font-size:14px; width:220px; outline:none; transition:border-color 0.3s; }
.search-input:focus { border-color:var(--primary); }
.filter-select { padding:8px 12px; border:2px solid var(--border); border-radius:8px; font-size:14px; outline:none; cursor:pointer; background:#fff; }
.btn-add { padding:8px 20px; background:var(--primary); color:#fff; border:none; border-radius:8px; font-size:14px; font-weight:600; cursor:pointer; transition:all 0.3s; }
.btn-add:hover { background:var(--primary-hover); transform:translateY(-1px); }

.stats-row { display:grid; grid-template-columns:repeat(4,1fr); gap:14px; margin-bottom:22px; }
.stat-card { background:linear-gradient(135deg,#f5f7fa,#fff); border-radius:12px; padding:16px 18px; text-align:center; border:1px solid #eef0f2; display:flex; flex-direction:column; gap:4px; }
.stat-card b { font-size:28px; color:var(--primary); }
.stat-card span { font-size:12px; color:var(--text-secondary); }
.stat-total b { color:#1a73e8; }
.stat-online b { color:#34a853; }
.stat-offline b { color:#ea4335; }
.stat-maintain b { color:#f9ab00; }

.data-table { width:100%; border-collapse:collapse; }
.data-table th { background:#f8f9fa; padding:12px 14px; text-align:left; font-size:13px; font-weight:600; color:var(--text-secondary); border-bottom:2px solid var(--border); }
.data-table td { padding:12px 14px; border-bottom:1px solid #eee; font-size:14px; vertical-align:middle; }
.data-table tr:hover { background:#fafbfd; }

.badge-sm { display:inline-block; padding:3px 10px; border-radius:10px; font-size:11px; font-weight:600; }
.cat-SERVER { background:#e8f0fe; color:#1967d2; }
.cat-PRINTER { background:#fef7e0; color:#b06000; }
.cat-NETWORK { background:#e6f4ea; color:#137333; }
.cat-COMPUTER { background:#fce8e6; color:#c5221f; }
.cat-OTHER { background:#f3e8fd; color:#8e24aa; }

.status-dot { display:inline-block; width:8px; height:8px; border-radius:50%; margin-right:5px; }
.status-ONLINE { background:#34a853; }
.status-OFFLINE { background:#ea4335; }
.status-MAINTAINING { background:#f9ab00; }
.status-SCRAPPED { background:#9aa0a6; }

.btn-action { padding:4px 12px; border-radius:5px; font-size:12px; cursor:pointer; border:1px solid transparent; margin-right:4px; transition:all 0.2s; }
.btn-edit { background:#e8f0fe; color:#1967d2; border-color:#d2e3fc; }
.btn-edit:hover { background:#d2e3fc; }
.btn-del { background:#fce8e6; color:#c5221f; border-color:#fad2cf; }
.btn-del:hover { background:#fad2cf; }

.pagination { display:flex; justify-content:center; align-items:center; gap:8px; margin-top:24px; }
.page-btn { padding:7px 14px; border:1px solid var(--border); border-radius:6px; background:#fff; cursor:pointer; font-size:13px; transition:all 0.3s; }
.page-btn.active { background:var(--primary); color:#fff; border-color:var(--primary); }
.page-btn.disabled { opacity:0.4; cursor:not-allowed; }
.page-info { font-size:13px; color:var(--text-secondary); }

/* 弹窗 */
.modal-overlay { position:fixed; top:0; left:0; right:0; bottom:0; background:rgba(0,0,0,0.45); z-index:999; display:none; justify-content:center; align-items:center; }
.modal-overlay.show { display:flex; overflow-y:auto; padding:40px 20px; }
.modal-box { background:#fff; border-radius:12px; padding:28px; width:520px; max-width:95vw; box-shadow:0 20px 60px rgba(0,0,0,0.3); }
.modal-box h3 { margin-bottom:20px; font-size:18px; }
.modal-lg { width:600px; }
.device-form .form-row { display:grid; grid-template-columns:1fr 1fr; gap:14px; }
.device-form .form-group { margin-bottom:14px; }
.device-form label { display:block; font-size:13px; font-weight:500; color:#444; margin-bottom:5px; }
.device-form .required { color:#ea4335; }
.device-form input, .device-form select, .device-form textarea { width:100%; padding:10px 14px; border:2px solid var(--border); border-radius:8px; font-size:14px; outline:none; box-sizing:border-box; font-family:inherit; }
.device-form input:focus, .device-form select:focus, .device-form textarea:focus { border-color:var(--primary); }
.device-form textarea { resize:vertical; }
.modal-actions { display:flex; justify-content:flex-end; gap:8px; margin-top:20px; }
.btn-modal-cancel { padding:8px 18px; border:1px solid var(--border); border-radius:6px; background:#fff; cursor:pointer; font-size:14px; }
.btn-modal-ok { padding:8px 18px; border:none; border-radius:6px; background:var(--primary); color:#fff; cursor:pointer; font-size:14px; font-weight:600; }
</style>

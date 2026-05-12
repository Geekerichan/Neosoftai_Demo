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
        <router-link to="/devices" class="nav-link">设备资产</router-link>
        <router-link to="/knowledge" class="nav-link">知识库</router-link>
        <span class="nav-user-info">&#128100; {{ userStore.userInfo && userStore.userInfo.username }}</span>
        <button class="btn-logout" @click="handleLogout">退出</button>
      </div>
    </nav>

    <!-- 主内容区 -->
    <div class="main-content">
      <div class="card">
        <div class="card-header">
          <h3>&#128227; 系统公告中心</h3>
          <button v-if="userStore.isAdmin" class="btn-add" @click="openAddModal">&#43; 发布公告</button>
        </div>
        <div class="card-body">
          <!-- 类型筛选 -->
          <div class="toolbar">
            <div class="filter-group">
              <button v-for="t in typeFilters" :key="t.value"
                      class="filter-btn" :class="{ active: currentType === t.value }"
                      @click="filterByType(t.value)">
                {{ t.label }}
              </button>
            </div>
          </div>

          <!-- 公告列表 -->
          <div class="announce-list">
            <div v-if="loading" class="loading-state"><p>加载中...</p></div>
            <div v-else-if="announcements.length === 0" class="empty-state">
              <div class="icon">&#128227;</div><p>暂无公告</p>
            </div>
            <template v-else>
              <div v-for="item in announcements" :key="item.id" class="announce-card"
                   :class="getCardClass(item)">
                <div class="announce-header">
                  <span v-if="item.isTop === 1" class="top-badge">&#128205; 置顶</span>
                  <span class="type-badge" :class="'tb-' + item.announceType">{{ getTypeName(item.announceType) }}</span>
                  <span class="announce-time">{{ formatTime(item.createTime) }}</span>
                </div>
                <h3 class="announce-title">{{ item.title }}</h3>
                <p class="announce-content">{{ item.content }}</p>
                <div v-if="userStore.isAdmin" class="announce-actions">
                  <button class="btn-action btn-edit" @click="openEditModal(item)">编辑</button>
                  <button class="btn-action btn-del" @click="deleteAnnounce(item.id)">删除</button>
                </div>
              </div>
            </template>
          </div>

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

    <!-- 发布/编辑弹窗 -->
    <div class="modal-overlay" :class="{ show: showModal }">
      <div class="modal-box">
        <h3>{{ isEdit ? '编辑公告' : '发布公告' }}</h3>
        <form @submit.prevent="submitForm" class="announce-form">
          <div class="form-group">
            <label>公告标题 <span class="required">*</span></label>
            <input type="text" v-model="form.title" placeholder="请输入标题" required maxlength="100" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>公告类型</label>
              <select v-model="form.announceType">
                <option value="NOTICE">通知</option>
                <option value="WARNING">警告</option>
                <option value="MAINTAIN">维护</option>
                <option value="UPGRADE">升级</option>
              </select>
            </div>
            <div class="form-group" style="display:flex;align-items:center;padding-top:20px;">
              <label style="margin:0;margin-right:10px;display:flex;align-items:center;cursor:pointer;">
                <input type="checkbox" v-model="isTopCheck" style="width:18px;height:18px;cursor:pointer;" />
                <span style="font-size:14px;color:#444;">置顶公告</span>
              </label>
            </div>
          </div>
          <div class="form-group">
            <label>公告内容 <span class="required">*</span></label>
            <textarea v-model="form.content" rows="6" placeholder="请输入公告详细内容..." required></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn-modal-cancel" @click="closeModal">取消</button>
            <button type="submit" class="btn-modal-ok">{{ isEdit ? '保存修改' : '发布' }}</button>
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
import { announceApi } from '../api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const announcements = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const currentType = ref('ALL')

const typeFilters = [
  { label: '全部', value: 'ALL' },
  { label: '📋 通知', value: 'NOTICE' },
  { label: '⚠️ 警告', value: 'WARNING' },
  { label: '🔧 维护', value: 'MAINTAIN' },
  { label: '✨ 升级', value: 'UPGRADE' }
]

// 弹窗
const showModal = ref(false)
const isEdit = ref(false)
let editId = null
const form = ref({ title: '', content: '', announceType: 'NOTICE' })
const isTopCheck = ref(false)

const totalPages = computed(() => Math.ceil(total.value / pageSize))
const pageNumbers = computed(() => {
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  const arr = []
  for (let i = start; i <= end; i++) arr.push(i)
  return arr
})

function getTypeName(t) {
  return { NOTICE: '通知', WARNING: '警告', MAINTAIN: '维护', UPGRADE: '升级' }[t] || t
}
function getCardClass(item) {
  const cls = []
  if (item.isTop === 1) cls.push('is-top')
  const t = (item.announceType || '').toLowerCase()
  if (t) cls.push('type-' + t)
  return cls
}
function formatTime(t) { if (!t) return ''; return t.substring(0, 16).replace('T', ' ') }

async function loadList() {
  loading.value = true
  try {
    const res = await announceApi.list({ announceType: currentType.value, current: currentPage.value, size: pageSize })
    announcements.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {} finally { loading.value = false }
}

function filterByType(t) { currentType.value = t; currentPage.value = 1; loadList() }
function goPage(p) { if (p < 1 || p > totalPages.value) return; currentPage.value = p; loadList() }

function openAddModal() {
  isEdit.value = false; editId = null
  form.value = { title: '', content: '', announceType: 'NOTICE' }
  isTopCheck.value = false
  showModal.value = true
}
function openEditModal(item) {
  isEdit.value = true; editId = item.id
  form.value = { ...item }
  isTopCheck.value = item.isTop === 1
  showModal.value = true
}
function closeModal() { showModal.value = false }

async function submitForm() {
  const data = { ...form.value, isTop: isTopCheck.value ? 1 : 0 }
  try {
    if (isEdit.value) {
      await announceApi.update({ ...data, id: editId })
      window.showToast?.('更新成功', 'success')
    } else {
      await announceApi.add(data)
      window.showToast?.('发布成功', 'success')
    }
    closeModal()
    loadList()
  } catch (e) {}
}

async function deleteAnnounce(id) {
  if (!confirm('确定删除该公告？')) return
  try { await announceApi.delete(id); window.showToast?.('删除成功', 'success'); loadList() } catch (e) {}
}

function handleLogout() { userStore.logout(); router.push('/login') }

onMounted(() => loadList())
</script>

<style scoped>
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; flex-wrap:wrap; gap:12px; }
.filter-group { display:flex; gap:8px; flex-wrap:wrap; }
.filter-btn { padding:8px 18px; border:2px solid var(--border); border-radius:20px; background:#fff; cursor:pointer; font-size:13px; font-weight:500; transition:all 0.3s; }
.filter-btn.active { border-color:var(--primary); background:#e8f0fe; color:var(--primary); }
.btn-add { padding:8px 20px; background:var(--primary); color:#fff; border:none; border-radius:8px; font-size:14px; font-weight:600; cursor:pointer; transition:all 0.3s; }
.btn-add:hover { background:var(--primary-hover); transform:translateY(-1px); }

.announce-list { display:flex; flex-direction:column; gap:16px; }

.announce-card {
  background:#fff; border-radius:12px; padding:22px 24px;
  border-left:4px solid #1a73e8;
  box-shadow:0 2px 8px rgba(0,0,0,0.05);
  transition:all 0.25s;
}
.announce-card:hover { box-shadow:0 6px 20px rgba(0,0,0,0.08); transform:translateX(3px); }
.announce-card.is-top { border-left-color:#ea4335; background:linear-gradient(135deg,#fffaf5,#fff); }
.announce-card.type-warning { border-left-color:#f9ab00; }
.announce-card.type-maintain { border-left-color:#34a853; }
.announce-card.type-upgrade { border-left-color:#8e24aa; }

.announce-header { display:flex; align-items:center; gap:10px; margin-bottom:10px; }
.top-badge { background:linear-gradient(135deg,#ea4335,#f9ab00); color:#fff; padding:3px 12px; border-radius:12px; font-size:11px; font-weight:700; }
.type-badge { padding:3px 12px; border-radius:12px; font-size:11px; font-weight:600; }
.tb-NOTICE { background:#e8f0fe; color:#1967d2; }
.tb-WARNING { background:#fef7e0; color:#b06000; }
.tb-MAINTAIN { background:#e6f4ea; color:#137333; }
.tb-UPGRADE { background:#f3e8fd; color:#8e24aa; }
.announce-time { margin-left:auto; font-size:13px; color:#888; }

.announce-title { font-size:18px; font-weight:700; color:var(--text); margin-bottom:10px; line-height:1.4; }
.announce-content { font-size:14px; color:#555; line-height:1.85; white-space:pre-wrap; margin-bottom:14px; }
.announce-actions { display:flex; gap:8px; justify-content:flex-end; }
.btn-action { padding:5px 14px; border-radius:6px; font-size:12px; cursor:pointer; transition:all 0.2s; }
.btn-edit { background:#e8f0fe; color:#1967d2; border:1px solid #d2e3fc; }
.btn-edit:hover { background:#d2e3fc; }
.btn-del { background:#fce8e6; color:#c5221f; border:1px solid #fad2cf; }
.btn-del:hover { background:#fad2cf; }

.pagination { display:flex; justify-content:center; align-items:center; gap:8px; margin-top:24px; }
.page-btn { padding:7px 14px; border:1px solid var(--border); border-radius:6px; background:#fff; cursor:pointer; font-size:13px; transition:all 0.3s; }
.page-btn.active { background:var(--primary); color:#fff; border-color:var(--primary); }
.page-btn.disabled { opacity:0.4; cursor:not-allowed; }
.page-info { font-size:13px; color:var(--text-secondary); }

/* 弹窗 */
.modal-overlay { position:fixed; top:0; left:0; right:0; bottom:0; background:rgba(0,0,0,0.45); z-index:999; display:none; justify-content:center; align-items:center; overflow-y:auto; padding:40px 20px; }
.modal-overlay.show { display:flex; }
.modal-box { background:#fff; border-radius:12px; padding:28px; width:520px; max-width:95vw; box-shadow:0 20px 60px rgba(0,0,0,0.3); }
.modal-box h3 { margin-bottom:20px; font-size:18px; }
.announce-form .form-row { display:grid; grid-template-columns:1fr 1fr; gap:14px; }
.form-group { margin-bottom:14px; }
.form-group label { display:block; font-size:13px; font-weight:500; color:#444; margin-bottom:5px; }
.required { color:#ea4335; }
.form-group input, .form-group select, .form-group textarea { width:100%; padding:10px 14px; border:2px solid var(--border); border-radius:8px; font-size:14px; outline:none; box-sizing:border-box; font-family:inherit; }
.form-group input:focus, .form-group select:focus, .form-group textarea:focus { border-color:var(--primary); }
.form-group textarea { resize:vertical; }
.modal-actions { display:flex; justify-content:flex-end; gap:8px; margin-top:20px; }
.btn-modal-cancel { padding:8px 18px; border:1px solid var(--border); border-radius:6px; background:#fff; cursor:pointer; font-size:14px; }
.btn-modal-ok { padding:8px 18px; border:none; border-radius:6px; background:var(--primary); color:#fff; cursor:pointer; font-size:14px; font-weight:600; }
</style>

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
        <router-link to="/knowledge" class="nav-link active">知识库</router-link>
        <span class="nav-user-info">&#128100; {{ userStore.userInfo && userStore.userInfo.username }}</span>
        <button class="btn-logout" @click="handleLogout">退出</button>
      </div>
    </nav>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 今日运维常识 -->
      <section v-if="dailyTip" class="daily-tip-banner">
        <div class="tip-banner-inner">
          <span class="tip-icon-big">&#128161;</span>
          <div class="tip-body">
            <div class="tip-header">
              <h3>今日运维常识</h3>
              <span class="tip-category-badge">{{ dailyTip.category }}</span>
            </div>
            <p class="tip-text">{{ dailyTip.content }}</p>
          </div>
        </div>
      </section>

      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <div class="search-input-wrap">
          &#128269;
          <input type="text" v-model="keyword" placeholder="搜索知识标题、标签或内容..." @keyup.enter="doSearch" />
        </div>
        <div class="cat-tags">
          <button v-for="c in categories" :key="c.value" class="cat-tag"
                  :class="{ active: currentCategory === c.value }" @click="filterByCategory(c.value)">
            {{ c.label }}
          </button>
        </div>
        <button v-if="userStore.isAdmin" class="btn-add-kb" @click="openAddModal">&#43; 发布知识</button>
      </div>

      <!-- 知识列表 -->
      <div class="kb-list">
        <div v-if="loading" class="loading-state"><p>加载中...</p></div>
        <div v-else-if="kbList.length === 0" class="empty-state">
          <div class="icon">&#128218;</div>
          <p>暂无知识内容</p><span style="font-size:13px;color:#aaa;">发布你的第一条运维知识吧</span>
        </div>
        <template v-else>
          <div v-for="item in kbList" :key="item.id" class="kb-card" @click="viewDetail(item)">
            <div class="kb-card-header">
              <span class="badge-sm" :class="'cat-' + item.category">{{ getCategoryName(item.category) }}</span>
              <span class="kb-meta"><span>&#128337; {{ item.viewCount || 0 }} 阅读</span> &middot; <span>&#10084; {{ item.likeCount || 0 }}</span></span>
            </div>
            <h3 class="kb-title">{{ item.title }}</h3>
            <p class="kb-summary">{{ getSummary(item.content) }}</p>
            <div class="kb-footer">
              <span class="kb-author">&#9998; {{ item.creatorName || '系统' }}</span>
              <span class="kb-time">{{ formatTime(item.createTime) }}</span>
              <div class="kb-actions" @click.stop>
                <button class="btn-like-sm" @click="doLike(item.id, $event)">&#128077; 赞</button>
                <template v-if="userStore.isAdmin">
                  <button class="btn-del-sm" @click="deleteKb(item.id, $event)">删除</button>
                </template>
              </div>
            </div>
            <div v-if="item.tags" class="kb-tags">
              <span v-for="tag in item.tags.split(',')" :key="tag" class="tag-pill">#{{ tag.trim() }}</span>
            </div>
          </div>
        </template>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <span class="page-info">共 {{ total }} 条 · 第 {{ currentPage }}/{{ totalPages }} 页</span>
        <button class="page-btn" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">&lt;</button>
        <button v-for="page in pageNumbers" :key="page" class="page-btn" :class="{ active: page === currentPage }" @click="goPage(page)">{{ page }}</button>
        <button class="page-btn" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">&gt;</button>
      </div>
    </div>

    <!-- 知识详情弹窗 -->
    <div class="modal-overlay" :class="{ show: showDetail }">
      <div class="modal-box modal-xl">
        <div class="detail-header">
          <h2>{{ currentKb?.title }}</h2>
          <button class="btn-close" @click="showDetail = false">&times;</button>
        </div>
        <div class="detail-meta">
          <span class="badge-sm" :class="'cat-' + currentKb?.category">{{ getCategoryName(currentKb?.category) }}</span>
          <span>{{ formatTime(currentKb?.createTime) }} · 作者: {{ currentKb?.creatorName || '系统' }}</span>
          <span>{{ currentKb?.viewCount || 0 }} 阅读 · {{ currentKb?.likeCount || 0 }} 点赞</span>
        </div>
        <div class="detail-body" v-html="renderMarkdown(currentKb?.content)"></div>
        <div v-if="currentKb?.tags" class="detail-tags">
          <span v-for="tag in currentKb.tags.split(',')" :key="tag" class="tag-pill">#{{ tag.trim() }}</span>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div class="modal-overlay" :class="{ show: showAddModal }">
      <div class="modal-box modal-lg">
        <h3>{{ isEdit ? '编辑知识' : '发布新知识' }}</h3>
        <form @submit.prevent="submitForm" class="kb-form">
          <div class="form-group">
            <label>标题 <span class="required">*</span></label>
            <input type="text" v-model="form.title" placeholder="请输入知识标题" required />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>分类</label>
              <select v-model="form.category">
                <option value="SERVER">服务器运维</option>
                <option value="OFFICE">办公设备</option>
                <option value="NETWORK">网络安全</option>
                <option value="DATA">数据管理</option>
                <option value="MAINTAIN">日常维护</option>
                <option value="OTHER">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label>标签(逗号分隔)</label>
              <input type="text" v-model="form.tags" placeholder="如：Linux,CPU,排查" />
            </div>
          </div>
          <div class="form-group">
            <label>正文内容(Markdown支持) <span class="required">*</span></label>
            <textarea v-model="form.content" rows="12" placeholder="支持Markdown格式，可使用 # 标题、**加粗**、`代码`、列表等语法..." required></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn-modal-cancel" @click="showAddModal = false">取消</button>
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
import { knowledgeApi, tipApi } from '../api'
import { getCategoryName, formatTime, renderMarkdown } from '../utils/helpers'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const kbList = ref([])
// 每日运维常识
const dailyTip = ref(null)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const keyword = ref('')
const currentCategory = ref('ALL')

const categories = [
  { label: '全部', value: 'ALL' },
  { label: '服务器', value: 'SERVER' },
  { label: '办公设备', value: 'OFFICE' },
  { label: '网络', value: 'NETWORK' },
  { label: '数据管理', value: 'DATA' },
  { label: '日常维护', value: 'MAINTAIN' },
  { label: '其他', value: 'OTHER' }
]

// 详情弹窗
const showDetail = ref(false)
const currentKb = ref(null)

// 编辑弹窗
const showAddModal = ref(false)
const isEdit = ref(false)
let editId = null
const form = ref({ title: '', content: '', category: 'SERVER', tags: '' })

const totalPages = computed(() => Math.ceil(total.value / pageSize))
const pageNumbers = computed(() => {
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  const arr = []
  for (let i = start; i <= end; i++) arr.push(i)
  return arr
})

/* getCategoryName / formatTime / renderMarkdown 已提取至 utils/helpers.js */

function getSummary(content) {
  if (!content) return ''
  let text = content.replace(/[#*`\[\]]/g, '').replace(/\n/g, ' ').trim()
  return text.length > 120 ? text.substring(0, 120) + '...' : text
}

/* renderMarkdown 已提取至 utils/helpers.js */

async function loadList() {
  loading.value = true
  try {
    const res = await knowledgeApi.list({ keyword: keyword.value, category: currentCategory.value, current: currentPage.value, size: pageSize })
    kbList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {} finally { loading.value = false }
}

function doSearch() { currentPage.value = 1; loadList() }
function filterByCategory(c) { currentCategory.value = c; currentPage.value = 1; loadList() }
function goPage(p) { if (p < 1 || p > totalPages.value) return; currentPage.value = p; loadList() }

async function viewDetail(item) {
  try {
    const res = await knowledgeApi.detail(item.id)
    currentKb.value = res.data
    showDetail.value = true
  } catch (e) {}
}

async function doLike(id, e) {
  e.stopPropagation()
  try { await knowledgeApi.like(id); window.showToast?.('点赞成功', 'success'); loadList() } catch (e) {}
}

async function deleteKb(id, e) {
  e.stopPropagation()
  if (!confirm('确定删除该知识？')) return
  try { await knowledgeApi.delete(id); window.showToast?.('删除成功', 'success'); loadList() } catch (e) {}
}

function openAddModal() {
  isEdit.value = false; editId = null
  form.value = { title: '', content: '', category: 'SERVER', tags: '' }
  showAddModal.value = true
}

async function submitForm() {
  try {
    if (isEdit.value) {
      await knowledgeApi.update({ ...form.value, id: editId })
      window.showToast?.('更新成功', 'success')
    } else {
      await knowledgeApi.add(form.value)
      window.showToast?.('发布成功', 'success')
    }
    showAddModal.value = false
    loadList()
  } catch (e) {}
}

function handleLogout() { userStore.logout(); router.push('/login') }

onMounted(() => {
  loadList()
  // 加载今日运维常识
  tipApi.getToday().then(res => {
    if (res.data) dailyTip.value = res.data
  }).catch(() => {})
})
</script>

<style scoped>
/* 今日运维常识横幅 */
.daily-tip-banner { margin: 20px 0 24px; }
.tip-banner-inner {
  display: flex; align-items: center; gap: 18px;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  border-left: 5px solid #667eea;
  border-radius: 12px; padding: 22px 26px;
}
.tip-icon-big { font-size: 34px; flex-shrink: 0; }
.tip-body { flex: 1; }
.tip-header {
  display: flex; align-items: center; gap: 10px; margin-bottom: 6px;
}
.tip-header h3 { font-size: 17px; color: #5b6cb8; margin: 0; }
.tip-category-badge {
  background: #e8f0fe; color: #1967d2; padding: 2px 12px;
  border-radius: 10px; font-size: 11px; font-weight: 700;
}
.tip-text { font-size: 14.5px; line-height: 1.7; color: var(--text); margin: 0; }

.search-bar { display:flex; align-items:center; gap:14px; margin-bottom:24px; flex-wrap:wrap; padding:18px 22px; background:#fff; border-radius:12px; box-shadow:0 2px 12px rgba(0,0,0,0.06); }
.search-input-wrap { flex:1; min-width:250px; position:relative; display:flex; align-items:center; background:#f5f7fa; border-radius:10px; padding:10px 16px; border:2px solid transparent; transition:border-color 0.3s; }
.search-input-wrap:focus-within { border-color:var(--primary); background:#fff; }
.search-input-wrap::before { content:''; font-size:18px; color:#888; margin-right:8px; flex-shrink:0; }
.search-input-wrap input { flex:1; border:none; outline:none; background:transparent; font-size:15px; }
.cat-tags { display:flex; gap:6px; flex-wrap:wrap; }
.cat-tag { padding:6px 14px; border:2px solid var(--border); border-radius:20px; background:#fff; cursor:pointer; font-size:13px; font-weight:500; transition:all 0.25s; }
.cat-tag.active { border-color:var(--primary); background:#e8f0fe; color:var(--primary); }
.btn-add-kb { padding:9px 20px; background:var(--primary); color:#fff; border:none; border-radius:10px; font-size:14px; font-weight:600; cursor:pointer; white-space:nowrap; transition:all 0.3s; }
.btn-add-kb:hover { background:var(--primary-hover); transform:translateY(-1px); }

.kb-list { display:grid; grid-template-columns:repeat(auto-fill,minmax(380px,1fr)); gap:18px; }
.kb-card { background:#fff; border-radius:12px; padding:20px; box-shadow:0 2px 10px rgba(0,0,0,0.05); cursor:pointer; transition:all 0.25s; border:1px solid #f0f0f0; }
.kb-card:hover { transform:translateY(-3px); box-shadow:0 8px 30px rgba(0,0,0,0.1); border-color:#d2e3fc; }
.kb-card-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; }
.badge-sm { display:inline-block; padding:2px 10px; border-radius:10px; font-size:11px; font-weight:600; }
.cat-SERVER { background:#e8f0fe; color:#1967d2; } .cat-OFFICE { background:#fef7e0; color:#b06000; }
.cat-NETWORK { background:#e6f4ea; color:#137333; } .cat-DATA { background:#fce8e6; color:#c5221f; }
.cat-MAINTAIN { background:#f3e8fd; color:#8e24aa; } .cat-OTHER { background:#f1f3f4; color:#5f6368; }
.kb-meta { font-size:12px; color:#888; }
.kb-title { font-size:17px; font-weight:700; color:var(--text); margin-bottom:8px; line-height:1.4; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; }
.kb-summary { font-size:13px; color:#666; line-height:1.7; margin-bottom:14px; display:-webkit-box; -webkit-line-clamp:3; -webkit-box-orient:vertical; overflow:hidden; }
.kb-footer { display:flex; align-items:center; gap:14px; font-size:12px; color:#888; padding-top:12px; border-top:1px solid #f0f0f0; }
.kb-footer .kb-author { color:var(--primary); font-weight:500; }
.kb-actions { margin-left:auto; display:flex; gap:6px; }
.btn-like-sm { padding:4px 10px; background:#e6f4ea; color:#137333; border:none; border-radius:5px; font-size:11px; cursor:pointer; }
.btn-like-sm:hover { background:#ceead6; }
.btn-del-sm { padding:4px 10px; background:#fce8e6; color:#c5221f; border:none; border-radius:5px; font-size:11px; cursor:pointer; }
.btn-del-sm:hover { background:#fad2cf; }
.kb-tags { margin-top:10px; display:flex; flex-wrap:wrap; gap:5px; }
.tag-pill { background:#e8f0fe; color:#1967d2; padding:2px 8px; border-radius:10px; font-size:11px; }

.pagination { display:flex; justify-content:center; align-items:center; gap:8px; margin-top:28px; }
.page-btn { padding:7px 14px; border:1px solid var(--border); border-radius:6px; background:#fff; cursor:pointer; font-size:13px; transition:all 0.3s; }
.page-btn.active { background:var(--primary); color:#fff; border-color:var(--primary); }
.page-btn.disabled { opacity:0.4; cursor:not-allowed; }
.page-info { font-size:13px; color:var(--text-secondary); }

/* 详情弹窗 */
.modal-overlay { position:fixed; top:0; left:0; right:0; bottom:0; background:rgba(0,0,0,0.45); z-index:999; display:none; justify-content:center; align-items:center; overflow-y:auto; padding:40px 20px; }
.modal-overlay.show { display:flex; }
.modal-box { background:#fff; border-radius:12px; padding:28px; width:680px; max-width:95vw; box-shadow:0 20px 60px rgba(0,0,0,0.3); }
.modal-xl { width:800px; max-width:95vw; }
.detail-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:14px; }
.detail-header h2 { font-size:22px; color:var(--text); flex:1; margin-right:20px; }
.btn-close { background:none; border:none; font-size:28px; cursor:pointer; color:#888; line-height:1; }
.detail-meta { display:flex; gap:12px; align-items:center; font-size:13px; color:#888; margin-bottom:20px; padding-bottom:14px; border-bottom:1px solid #eee; }
.detail-body { font-size:15px; line-height:1.85; color:var(--text); max-height:60vh; overflow-y:auto; padding-right:10px; }
.detail-body :deep(h1) { font-size:20px; font-weight:700; color:#1a73e8; margin:16px 0 10px; }
.detail-body :deep(h2) { font-size:17px; font-weight:700; margin:14px 0 8px; }
.detail-body :deep(h3) { font-size:15px; font-weight:600; margin:12px 0 6px; }
.detail-body :deep(p) { margin:8px 0; }
.detail-body :deep(ul), .detail-body :deep(ol) { padding-left:22px; margin:10px 0; }
.detail-body :deep(li) { margin:4px 0; line-height:1.75; }
.detail-body :deep(strong) { color:#d93025; font-weight:700; }
.detail-body :deep(code.md-inline-code) { background:#f1f3f4; padding:2px 7px; border-radius:4px; font-family:'Cascadia Code','Consolas',monospace; font-size:13px; color:#c7254e; word-break:break-all; }
.detail-body :deep(pre.md-code) { background:#282c34; color:#abb2bf; padding:14px 18px; border-radius:8px; overflow-x:auto; margin:12px 0; }
.detail-body :deep(pre.md-code code) { background:none; padding:0; color:inherit; font-size:13px; line-height:1.6; }
.detail-body :deep(blockquote) { border-left:4px solid #667eea; background:#f5f7ff; padding:10px 16px; margin:12px 0; border-radius:0 8px 8px 0; color:#555; font-style:italic; }
.detail-body :deep(hr) { border:none; height:1px; background:#e2e6ea; margin:14px 0; }
.detail-body :deep(table) { width:100%; border-collapse:collapse; margin:10px 0; }
.detail-body :deep(th), .detail-body :deep(td) { border:1px solid #ddd; padding:8px 12px; font-size:13px; text-align:left; }
.detail-body :deep(th) { background:#f8f9fa; font-weight:600; }
.detail-tags { margin-top:16px; display:flex; flex-wrap:wrap; gap:6px; }

/* 编辑表单 */
.modal-lg { width:620px; max-width:95vw; }
.kb-form .form-row { display:grid; grid-template-columns:1fr 1fr; gap:14px; }
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

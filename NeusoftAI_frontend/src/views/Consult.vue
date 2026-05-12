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
        <router-link to="/consult" class="nav-link active">故障咨询</router-link>
        <router-link to="/records" class="nav-link">我的记录</router-link>
        <router-link to="/devices" class="nav-link">设备资产</router-link>
        <router-link to="/knowledge" class="nav-link">知识库</router-link>
        <span class="nav-user-info">&#128100; {{ userStore.userInfo && userStore.userInfo.username }}</span>
        <button class="btn-logout" @click="handleLogout">退出</button>
      </div>
    </nav>

    <!-- 主内容区 -->
    <div class="main-content">
      <div class="page-grid">
        <!-- 左侧：咨询区域 -->
        <div class="card consult-card animate-in">
          <div class="card-header">
            <h3>&#127911; 设备故障智能诊断</h3>
            <span class="header-badge">&#129302; 基于 Ollama 大模型</span>
          </div>
          <div class="card-body">
            <!-- 故障描述输入 -->
            <textarea
              id="faultDescription"
              v-model="description"
              placeholder="请描述您遇到的设备故障问题，例如：&#10;&#10;- 服务器无法启动，电源灯闪烁&#10;- 办公电脑蓝屏显示错误代码0x0000007A&#10;- 打印机打印出来全白或卡纸&#10;- 局域网内部分电脑无法连接网络..."
            ></textarea>

            <!-- 图片上传 -->
            <div class="upload-row">
              <input
                type="file"
                ref="imageInputRef"
                accept="image/jpeg,image/png,image/gif,image/webp"
                style="display:none"
                @change="handleImageSelect"
              />
              <button
                v-if="!selectedImageFile"
                type="button"
                class="btn-upload"
                @click="$refs.imageInputRef.click()"
              >&#128247; 上传故障图片</button>
              <div v-if="selectedImageFile" class="upload-preview-inline" style="display:flex">
                <img :src="imagePreviewUrl" alt="预览" />
                <span class="image-filename">{{ imageFileName }}</span>
                <button type="button" class="btn-remove-image" @click="removeImage" title="移除图片">&times;</button>
              </div>
              <span v-if="!selectedImageFile" class="upload-hint-text">支持 JPG/PNG/GIF/WebP，最大 10MB</span>
            </div>

            <!-- 故障类型选择 -->
            <div class="fault-type-row">
              <label>故障类型：</label>
              <button
                v-for="t in faultTypes"
                :key="t.value"
                type="button"
                class="type-btn"
                :class="{ active: selectedType === t.value }"
                @click="selectedType = t.value"
              >{{ t.label }}</button>
            </div>

            <!-- 发起诊断按钮 -->
            <div class="action-bar">
              <button class="btn-consult" :disabled="diagnosing" @click="doDiagnose">
                <template v-if="!diagnosing">&#128640; 发起故障咨询</template>
                <template v-else><span class="loading-spinner"></span> AI 正在分析...</template>
              </button>
            </div>

            <!-- 结果展示 -->
            <div v-if="showResultArea" class="result-area animate-scale" style="display:block">
              <div class="result-header">
                <span id="resultMeta">{{ resultMetaText }}</span>
                <span id="resultTime" class="time-success">{{ resultTimeText }}</span>
              </div>
              <div class="result-body" v-html="solutionHtml"></div>
              <div class="result-actions">
                <button class="btn-sm btn-like" :class="{ liked: hasLiked }" @click="doLike">&#128077; 点赞 ({{ likeCount }})</button>
                <button class="btn-sm btn-follow" @click="toggleFollowUp">&#128172; 追问补充信息</button>
              </div>

              <!-- 聊天式追问对话区 -->
              <div v-if="showFollowUp" class="chat-section">
                <div class="chat-messages" ref="chatMessagesRef">
                  <div v-for="(item, idx) in followUpHistory" :key="idx" class="chat-msg" :class="'msg-' + item.role">
                    <div class="msg-avatar">{{ item.role === 'user' ? '&#128100;' : '&#129302;' }}</div>
                    <div class="msg-bubble">
                      <div v-if="item.loading" class="typing-row">
                        <span class="typing-dot"></span>
                        <span class="typing-dot"></span>
                        <span class="typing-dot"></span>
                        <span class="typing-text">AI 正在思考...</span>
                      </div>
                      <div v-else-if="item.role === 'assistant'" v-html="renderMarkdown(item.content)"></div>
                      <template v-else>{{ item.content }}</template>
                    </div>
                    <div class="msg-time">{{ item.time }}</div>
                  </div>
                  <div v-if="!followUpHistory.length" class="chat-placeholder">
                    &#128172; 补充故障细节，AI 将给出更精准的方案
                  </div>
                </div>
                <div class="chat-input-bar">
                  <input v-model="followUpInput" placeholder="输入追问内容..." @keydown.enter.exact="doFollowUp" :disabled="followUpLoading" />
                  <button class="btn-send-chat" @click="doFollowUp" :disabled="followUpLoading || !followUpInput.trim()">
                    {{ followUpLoading ? '思考中...' : '发送' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：历史记录 -->
        <div class="card history-card animate-in delay-2">
          <div class="card-header">
            <h3>&#128221; 咨询历史</h3>
            <router-link to="/records" class="view-all-sm">查看全部 &rarr;</router-link>
          </div>
          <div class="history-list">
            <div v-if="historyRecords.length === 0" class="empty-state">
              <div class="icon">&#128270;</div>
              <p>暂无咨询记录<br><span style="font-size:13px;color:#aaa;">发起故障咨询后记录将在此显示</span></p>
            </div>
            <div v-for="(r, idx) in historyRecords" :key="r.id"
                 class="history-item animate-in" :style="{ animationDelay: (idx * 0.05) + 's' }">
              <span class="type-tag" :class="'type-' + r.faultType">{{ getTypeName(r.faultType) }}</span>
              <span v-if="r.optimized === 1" class="optimized-badge">已优化</span>
              <div class="desc">{{ escapeHtml(r.faultDescription) }}</div>
              <div class="meta">
                <span>{{ r.createTime ? r.createTime.substring(0,16) : '' }}</span>
                <button
                  class="btn-history-like"
                  :class="{ 'liked': likedSet.has(r.id), 'liking': likingIds.has(r.id) }"
                  @click="doHistoryLike(r)"
                  :disabled="likingIds.has(r.id)"
                >
                  <span class="heart-icon">&#10084;</span> {{ r.likeCount || 0 }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { faultApi, recordApi } from '../api'
import { getTypeName, renderMarkdown, formatFileSize } from '../utils/helpers'

const router = useRouter()
const userStore = useUserStore()

// ==================== 状态 ====================
const description = ref('')
const selectedImageFile = ref(null)
const imagePreviewUrl = ref('')
const imageFileName = ref('')
const imageInputRef = ref(null)

const faultTypes = [
  { label: '其他/自动识别', value: 'OTHER' },
  { label: '服务器', value: 'SERVER' },
  { label: '办公设备', value: 'OFFICE' }
]
const selectedType = ref('OTHER')

const diagnosing = ref(false)
const showResultArea = ref(false)
const solutionHtml = ref('')
const resultMetaText = ref('AI 解决方案')
const resultTimeText = ref('')
const currentRecordId = ref(null)
const currentSessionId = ref(null)
const likeCount = ref(0)
const hasLiked = ref(false)

const showFollowUp = ref(false)
const followUpInput = ref('')
const followUpHistory = ref([])
const followUpLoading = ref(false)
const chatMessagesRef = ref(null)

const historyRecords = ref([])
const likedSet = ref(new Set())       // 历史记录中已点赞的 ID 集合
const likingIds = ref(new Set())      // 正在请求中的点赞 ID 集合（防重复点击）

/* ===== 方法 ===== */
// renderMarkdown / getTypeName / formatFileSize 已提取至 utils/helpers.js

function escapeHtml(str) {
  if (!str) return ''
  const div = document.createElement('div')
  div.textContent = str
  return div.innerHTML.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function handleImageSelect(event) {
  const file = event.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) { window.showToast?.('请选择图片文件', 'error'); event.target.value = ''; return }
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) { window.showToast?.('图片大小不能超过10MB', 'error'); event.target.value = ''; return }
  selectedImageFile.value = file
  const reader = new FileReader()
  reader.onload = (e) => { imagePreviewUrl.value = e.target.result; imageFileName.value = `${file.name} (${formatFileSize(file.size)})` }
  reader.readAsDataURL(file)
}
function removeImage() { selectedImageFile.value = null; imagePreviewUrl.value = ''; imageFileName.value = ''; if (imageInputRef.value) imageInputRef.value = '' }

async function doDiagnose() {
  if (!selectedImageFile.value && !description.value.trim()) { window.showToast?.('请输入故障描述或上传图片', 'error'); return }
  diagnosing.value = true
  try {
    let data
    if (selectedImageFile.value) data = await faultApi.diagnoseByImage(selectedImageFile.value, description.value, selectedType.value)
    else data = await faultApi.diagnose(description.value, selectedType.value)
    const r = data.data
    currentRecordId.value = r.recordId; currentSessionId.value = r.sessionId; likeCount.value = 0; hasLiked.value = false
    solutionHtml.value = renderMarkdown(r.solution); resultMetaText.value = r.mode === 'vision' ? 'AI 视觉诊断' : 'AI 解决方案'
    resultTimeText.value = `响应耗时 ${r.costMs}ms`; showResultArea.value = true
    await loadHistory()
  } catch (e) { window.showToast?.('请求失败，请检查 Ollama 服务是否启动', 'error') } finally { diagnosing.value = false }
}

async function doLike() {
  if (!currentRecordId.value) return
  try { await faultApi.like(currentRecordId.value); likeCount.value++; hasLiked.value = true; window.showToast?.('感谢您的点赞！', 'success'); await loadHistory() } catch (e) {}
}

/** 历史记录列表中的点赞（所有用户可用） */
async function doHistoryLike(record) {
  if (!record?.id || likingIds.value.has(record.id)) return
  // 已点赞过则取消点赞
  if (likedSet.value.has(record.id)) {
    try { await faultApi.like(record.id); likedSet.value.delete(record.id); record.likeCount = Math.max(0, (record.likeCount || 0) - 1) } catch (e) {}
    return
  }
  // 点赞
  likingIds.value.add(record.id)
  try {
    await faultApi.like(record.id)
    likedSet.value.add(record.id)
    record.likeCount = (record.likeCount || 0) + 1
  } catch (e) { window.showToast?.('操作失败', 'error') } finally { likingIds.value.delete(record.id) }
}
function toggleFollowUp() { showFollowUp.value = !showFollowUp.value }

async function doFollowUp() {
  const content = followUpInput.value.trim()
  if (!content || followUpLoading.value) return
  if (!currentRecordId.value) { window.showToast?.('请先发起咨询', 'error'); return }
  followUpInput.value = ''; followUpLoading.value = true
  followUpHistory.value.push({ role: 'user', content, time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
  followUpHistory.value.push({ role: 'assistant', content: '', loading: true, time: '' })
  scrollToChatBottom()
  try {
    const res = await faultApi.followUp(currentRecordId.value, content)
    const aiMsg = followUpHistory.value[followUpHistory.value.length - 1]
    if (aiMsg) { aiMsg.content = res.data.solution; aiMsg.loading = false; aiMsg.time = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }
    solutionHtml.value = renderMarkdown(res.data.solution); currentRecordId.value = res.data.recordId; resultMetaText.value = `追问回复`
    await loadHistory()
  } catch (e) {
    const failMsg = followUpHistory.value[followUpHistory.value.length - 1]
    if (failMsg) { failMsg.content = '[追问失败，请重试]'; failMsg.loading = false }
  } finally { followUpLoading.value = false; scrollToChatBottom() }
}

function scrollToChatBottom() { setTimeout(() => { if (chatMessagesRef.value) chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight }, 50) }
async function loadHistory() { try { const res = await recordApi.list({ faultType: 'ALL', current: 1, size: 20 }); historyRecords.value = res.data?.records || [] } catch (e) {} }
async function handleLogout() { userStore.logout(); router.push('/login') }

onMounted(() => { loadHistory() })
</script>

<style scoped>
.page-grid {
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 24px;
}
@media (max-width: 900px) { .page-grid { grid-template-columns: 1fr; } }

.consult-card { animation-delay: 0.08s; }
.history-card { animation-delay: 0.18s; }

.header-badge {
  font-size: 12px; color: var(--text-muted);
  padding: 4px 12px; border-radius: 20px;
  background: var(--bg-warm);
  font-weight: 500;
}
.view-all-sm {
  font-size: 13px; color: var(--primary); font-weight: 600; text-decoration: none;
  transition: all 0.25s var(--ease-out);
}
.view-all-sm:hover { text-decoration: underline; }

/* 上传区域 */
.upload-row {
  display: flex; align-items: center; gap: 12px;
  margin-top: 16px; flex-wrap: wrap;
}
.btn-upload {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 10px 22px; border: 2px dashed var(--border);
  border-radius: var(--radius);
  background: rgba(79,110,247,0.04);
  color: var(--primary); font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all 0.28s var(--ease-out);
}
.btn-upload:hover {
  border-color: var(--primary); background: var(--info-light);
  transform: translateY(-1px);
}
.upload-hint-text { font-size: 12px; color: var(--text-muted); }
.upload-preview-inline {
  align-items: center; gap: 10px;
  background: var(--bg-warm); border: 1px solid var(--border);
  border-radius: var(--radius); padding: 8px 14px; max-width: 100%;
  transition: all 0.25s var(--ease-out);
}
.upload-preview-inline img { max-width: 160px; max-height: 80px; border-radius: 8px; object-fit: contain; border: 1px solid var(--border-light); }
.upload-preview-inline .image-filename { font-size: 13px; color: var(--text-secondary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 180px; }
.btn-remove-image {
  width: 24px; height: 24px; border-radius: 50%; background: var(--danger); color: #fff; border: none; cursor: pointer;
  font-size: 15px; line-height: 1; display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  transition: transform 0.2s var(--ease-spring);
}
.btn-remove-image:hover { transform: scale(1.15); }

/* 输入框 */
textarea#faultDescription {
  width: 100%; height: 110px; padding: 15px 17px;
  border: 2px solid var(--border); border-radius: var(--radius);
  font-size: 15px; resize: vertical; outline: none; font-family: inherit; line-height: 1.65;
  background: var(--bg-warm); transition: all 0.28s var(--ease-out);
}
textarea#faultDescription:focus { border-color: var(--primary); box-shadow: 0 0 0 4px var(--primary-glow); background: #fff; }

.fault-type-row { display: flex; gap: 10px; margin-top: 14px; align-items: center; flex-wrap: wrap; }
.fault-type-row label { font-size: 14px; font-weight: 600; color: var(--text-secondary); }
.type-btn {
  padding: 7px 17px; border: 2px solid var(--border); border-radius: 24px; background: #fff;
  cursor: pointer; font-size: 13px; font-weight: 600; transition: all 0.28s var(--ease-out); color: var(--text-secondary);
}
.type-btn:hover { border-color: var(--primary-light); color: var(--primary); }
.type-btn.active { border-color: var(--primary); background: var(--info-light); color: var(--primary-dark); }

.action-bar { margin-top: 20px; display: flex; gap: 10px; }
.btn-consult {
  flex: 1; padding: 14px; background: linear-gradient(135deg, var(--primary), #6366f1); color: #fff;
  border: none; border-radius: var(--radius); font-size: 16px; font-weight: 700; cursor: pointer;
  transition: all 0.32s var(--ease-out); letter-spacing: 0.5px;
  box-shadow: 0 4px 16px rgba(79,110,247,0.35);
}
.btn-consult:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 8px 26px rgba(79,110,247,0.45); }
.btn-consult:active:not(:disabled) { transform: translateY(0); }
.btn-consult:disabled { opacity: 0.55; cursor: not-allowed; transform: none !important; box-shadow: none !important; }

/* 结果区域 */
.result-area {
  margin-top: 22px; border: 1.5px solid var(--border); border-radius: var(--radius-lg);
  overflow: hidden; transition: all 0.35s var(--ease-out);
  background: var(--bg-warm);
}
.result-header {
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  padding: 13px 20px; display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid var(--border-light); font-size: 13px; color: var(--text-secondary); font-weight: 500;
}
.time-success { color: var(--success); font-weight: 600; }
.result-body { padding: 20px; font-size: 15px; line-height: 1.85; color: var(--text); }
.result-actions { display: flex; gap: 8px; margin-top: 18px; flex-wrap: wrap; }

.btn-sm {
  padding: 8px 16px; border: none; border-radius: 24px; font-size: 13px; cursor: pointer; font-weight: 600;
  transition: all 0.28s var(--ease-out); letter-spacing: 0.2px;
}
.btn-like { background: var(--info-light); color: var(--primary-dark); }
.btn-like:hover { background: #bfdbfe; transform: translateY(-1px); }
.btn-like.liked { background: linear-gradient(135deg, var(--primary), #6366f1); color: #fff; }
.btn-follow { background: var(--warning-light); color: #b45309; }
.btn-follow:hover { background: #fde68a; transform: translateY(-1px); }

/* 追问聊天区 */
.chat-section { margin-top: 18px; padding: 18px 20px; border-top: 1px solid var(--border-light); display: flex; flex-direction: column; gap: 14px; }
.chat-messages { max-height: 380px; overflow-y: auto; padding-right: 8px; }
.chat-messages::-webkit-scrollbar { width: 5px; }
.chat-messages::-webkit-scrollbar-track { background: transparent; border-radius: 4px; }
.chat-messages::-webkit-scrollbar-thumb { background: rgba(148,163,184,0.3); border-radius: 4px; }

.chat-placeholder { text-align: center; padding: 22px 0; font-size: 13px; color: var(--text-muted); letter-spacing: 0.5px; }
.chat-msg { display: flex; flex-direction: column; gap: 4px; margin-bottom: 16px; animation: fadeInUp 0.3s ease both; }
.chat-msg.msg-user { align-items: flex-end; }
.chat-msg.msg-assistant { align-items: flex-start; }

.msg-avatar { font-size: 22px; line-height: 1; margin-bottom: 2px; filter: grayscale(20%); }
.msg-bubble { max-width: 85%; padding: 11px 16px; border-radius: 18px; font-size: 14px; line-height: 1.68; word-break: break-word; }
.msg-user .msg-bubble {
  background: linear-gradient(135deg, var(--primary), #6366f1); color: #fff; border-bottom-right-radius: 4px;
  box-shadow: 0 3px 12px rgba(79,110,247,0.25);
}
.msg-user .msg-bubble :deep(strong) { color: #fff; opacity: 0.92; }
.msg-assistant .msg-bubble {
  background: #fff; color: var(--text); border: 1px solid var(--border-light); border-bottom-left-radius: 4px;
  box-shadow: var(--shadow-sm);
}

/* AI 回复内的 Markdown 样式 */
.msg-bubble :deep(h1) { font-size: 15px; color: var(--primary); margin: 8px 0 4px; }
.msg-bubble :deep(h2) { font-size: 14px; font-weight: 700; margin: 6px 0 3px; }
.msg-bubble :deep(p) { margin: 4px 0; font-size: 13.5px; }
.msg-bubble :deep(ul), .msg-bubble :deep(ol) { padding-left: 18px; margin: 6px 0; }
.msg-bubble :deep(li) { margin: 2px 0; font-size: 13px; }
.msg-bubble :deep(code) { background: #f1f5f9; padding: 2px 6px; border-radius: 4px; font-size: 12px; color: var(--danger); }
.msg-bubble :deep(pre) { background: #1e293b; color: #e2e8f0; padding: 11px 14px; border-radius: 10px; font-size: 12px; overflow-x: auto; margin: 6px 0; }
.msg-bubble :deep(strong) { color: var(--danger); font-weight: 700; }
.msg-time { font-size: 11px; color: var(--text-muted); padding: 0 4px; }

/* 打字动画 */
.typing-row { display: inline-flex; gap: 5px; align-items: center; font-size: 13px; color: var(--text-secondary); }
.typing-dot { width: 6px; height: 6px; background: var(--primary); border-radius: 50%; animation: typingBounce 1.2s infinite; }
.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }

.chat-input-bar { display: flex; gap: 8px; padding-top: 10px; border-top: 1px solid var(--border-light); }
.chat-input-bar input {
  flex: 1; padding: 11px 18px; border: 2px solid var(--border); border-radius: 26px; font-size: 14px; outline: none;
  transition: all 0.25s var(--ease-out); background: var(--bg-warm);
}
.chat-input-bar input:focus { border-color: var(--primary); background: #fff; box-shadow: 0 0 0 4px var(--primary-glow); }
.btn-send-chat {
  padding: 11px 22px; background: linear-gradient(135deg, var(--primary), #6366f1); color: #fff; border: none;
  border-radius: 26px; cursor: pointer; font-size: 14px; font-weight: 700; white-space: nowrap;
  transition: all 0.28s var(--ease-out);
}
.btn-send-chat:hover:not(:disabled) { box-shadow: 0 4px 14px rgba(79,110,247,0.35); transform: translateY(-1px); }
.btn-send-chat:disabled { opacity: 0.5; cursor: not-allowed; }

/* 历史列表 */
.history-list { max-height: calc(100vh - 200px); overflow-y: auto; }
.history-item {
  border-bottom: 1px solid var(--border-light); padding: 16px 20px; cursor: pointer;
  transition: all 0.25s var(--ease-out);
}
.history-item:hover { background: var(--bg-warm); }
.history-item:last-child { border-bottom: none; }
.history-item .type-tag { display: inline-block; padding: 3px 11px; border-radius: 20px; font-size: 11px; font-weight: 700; margin-bottom: 6px; }

.optimized-badge {
  background: var(--success-light); color: #047857;
  padding: 2px 9px; border-radius: 10px; font-size: 10px; margin-left: 6px; font-weight: 700;
}

.history-item .desc { font-size: 14px; color: var(--text); margin-bottom: 6px; line-height: 1.52; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; white-space: pre-wrap; word-break: break-word; }
.history-item .meta { font-size: 12px; color: var(--text-secondary); display: flex; gap: 12px; align-items: center; }

/* 历史记录点赞按钮 */
.btn-history-like {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 3px 12px; border: 1.5px solid #fecaca; border-radius: 20px;
  background: #fff0f0; color: var(--danger); cursor: pointer;
  font-size: 12px; font-weight: 700; transition: all 0.28s var(--ease-out);
}
.btn-history-like:hover:not(:disabled) {
  border-color: var(--danger); background: var(--danger-light);
  transform: translateY(-1px);
}
.btn-history-like.liked {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #fff; border-color: transparent;
  box-shadow: 0 3px 10px rgba(239,68,68,0.35);
}
.btn-history-like.liked .heart-icon { animation: heartPop 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
.btn-history-like.liking { opacity: 0.6; cursor: wait; }
.btn-history-like:disabled { cursor: not-allowed; }

@keyframes heartPop {
  0% { transform: scale(1); }
  40% { transform: scale(1.35); }
  70% { transform: scale(0.9); }
  100% { transform: scale(1); }
}

/* Markdown 渲染样式 */
.result-body :deep(h1) { font-size: 20px; font-weight: 800; color: var(--primary); margin: 16px 0 10px; padding-bottom: 8px; border-bottom: 2px solid var(--info-light); }
.result-body :deep(h2) { font-size: 17px; font-weight: 700; color: var(--text); margin: 14px 0 8px; }
.result-body :deep(h3) { font-size: 15px; font-weight: 600; color: var(--text-secondary); margin: 12px 0 6px; }
.result-body :deep(p) { margin: 8px 0; line-height: 1.88; }
.result-body :deep(ul), .result-body :deep(ol) { padding-left: 22px; margin: 10px 0; }
.result-body :deep(li) { margin: 4px 0; line-height: 1.78; list-style-position: outside; }
.result-body :deep(ul li) { list-style-type: disc; }
.result-body :deep(ol li) { list-style-type: decimal; }
.result-body :deep(strong) { color: var(--danger); font-weight: 700; }
.result-body :deep(code.md-inline-code) { background: #f1f5f9; padding: 2px 8px; border-radius: 5px; font-family: 'Cascadia Code', Consolas, monospace; font-size: 13px; color: var(--danger); word-break: break-all; }
.result-body :deep(pre.md-code) { background: #1e293b; color: #e2e8f0; padding: 16px 20px; border-radius: 12px; overflow-x: auto; margin: 14px 0; border: 1px solid rgba(255,255,255,0.06); }
.result-body :deep(pre.md-code code) { background: none; padding: 0; color: inherit; font-size: 13px; line-height: 1.65; }
.result-body :deep(blockquote) { border-left: 4px solid var(--primary); background: rgba(79,110,247,0.04); padding: 12px 18px; margin: 14px 0; border-radius: 0 10px 10px 0; color: var(--text-secondary); font-size: 14px; font-style: italic; }
.result-body :deep(hr) { border: none; height: 1px; background: linear-gradient(90deg, transparent, var(--border), transparent); margin: 18px 0; }
</style>

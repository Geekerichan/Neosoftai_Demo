<template>
  <div class="home-page">
    <!-- ========== 毛玻璃导航栏 ========== -->
    <nav class="navbar">
      <router-link to="/" class="brand">
        <span class="brand-icon">&#9881;</span> 东软智能运维
        <span>AI-Powered O&M</span>
      </router-link>
      <div class="nav-links">
        <router-link to="/" class="nav-link active">首页</router-link>
        <router-link to="/consult" class="nav-link">故障咨询</router-link>
        <router-link to="/records" class="nav-link">我的记录</router-link>
        <router-link to="/devices" class="nav-link">设备资产</router-link>
        <router-link to="/knowledge" class="nav-link">知识库</router-link>
        <span class="nav-user-info">&#128100; {{ userStore.userInfo && userStore.userInfo.username }}</span>
        <button class="btn-logout" @click="handleLogout">退出</button>
      </div>
    </nav>

    <!-- ========== Hero 区域（深色科技风） ========== -->
    <section class="hero-section">
      <!-- 动态背景层 -->
      <div class="hero-bg-layer">
        <div class="hero-glow hero-glow-1"></div>
        <div class="hero-glow hero-glow-2"></div>
        <div class="hero-grid"></div>
        <div class="hero-particles"></div>
      </div>

      <div class="hero-content">
        <div class="hero-text animate-in">
          <div class="hero-badge">&#128172; AI-Powered Intelligent O&M Platform</div>
          <h1 class="hero-title">
            东软智能<br><span class="highlight-text">运维咨询平台</span>
          </h1>
          <p class="hero-subtitle">
            基于 Ollama 大模型的轻量化智能运维解决方案，提供 7×24 小时设备故障快速诊断与精准方案推荐
          </p>
          <div class="hero-actions">
            <router-link to="/consult" class="btn-hero-main">
              <span>&#128640;</span> 立即诊断
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 8h10M9 4l4 4-4 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </router-link>
            <a href="#features" class="btn-hero-ghost">了解更多 &darr;</a>
          </div>

          <div class="hero-stats animate-in delay-2">
            <div class="stat-item">
              <span class="stat-num">6+</span>
              <span class="stat-label">功能模块</span>
            </div>
            <div class="stat-dot"></div>
            <div class="stat-item">
              <span class="stat-num">AI</span>
              <span class="stat-label">智能驱动</span>
            </div>
            <div class="stat-dot"></div>
            <div class="stat-item">
              <span class="stat-num">7&times;24</span>
              <span class="stat-label">在线服务</span>
            </div>
          </div>
        </div>

        <!-- 右侧视觉卡片堆叠 -->
        <div class="hero-visual animate-in delay-1">
          <div class="visual-stack">
            <div class="visual-card v-card-1">
              <span class="vc-icon">&#128187;</span>
              <span>服务器监控诊断</span>
              <span class="vc-status vc-ok">在线</span>
            </div>
            <div class="visual-card v-card-2">
              <span class="vc-icon">&#128436;</span>
              <span>打印机维护排查</span>
              <span class="vc-status vc-warn">待检</span>
            </div>
            <div class="visual-card v-card-3">
              <span class="vc-icon">&#128279;</span>
              <span>网络连接分析</span>
              <span class="vc-status vc-ok">正常</span>
            </div>
            <div class="center-orb"></div>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== 系统公告（醒目置顶） ========== -->
    <section v-if="latestAnnouncements.length > 0" class="section announce-top-section">
      <div class="section-container">
        <div class="announce-header animate-in">
          <h2 class="announce-main-title">
            <span class="announce-icon">&#128227;</span> 系统公告
          </h2>
          <router-link to="/announcements" class="view-all-btn">
            查看全部
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 7h8m-3-3l3 3-3 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </router-link>
        </div>
        <div class="announce-grid">
          <div
            v-for="(item, idx) in latestAnnouncements"
            :key="item.id"
            :class="['announce-card', 'animate-in', 'delay-' + (idx + 1)]"
          >
            <div class="announce-top">
              <span class="announce-type-tag" :class="'tag-' + (item.announceType || '').toLowerCase()">
                {{ typeMap[item.announceType] || '通知' }}
              </span>
              <span v-if="item.isTop === 1" class="top-badge">&#11014; 置顶</span>
            </div>
            <h3 class="announce-title">{{ item.title }}</h3>
            <p class="announce-content">{{ item.content.length > 120 ? item.content.substring(0, 120) + '...' : item.content }}</p>
            <span class="announce-time">{{ formatDate(item.createTime) }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== 核心特性 ========== -->
    <section id="features" class="section features-section">
      <div class="section-container">
        <div class="section-header animate-in">
          <h2>核心特性</h2>
          <p>六大功能模块，覆盖运维全场景，让设备管理更高效、更智能</p>
        </div>
        <div class="feature-grid">
          <div class="feature-card animate-in delay-1" @click="$router.push('/consult')">
            <div class="feature-icon icon-diagnose">&#127911;</div>
            <h3>AI 故障诊断</h3>
            <p>基于大模型的多模态故障分析，支持文本描述和图片上传双重输入方式。</p>
            <ul class="feature-list">
              <li>多轮追问深度定位问题</li>
              <li>支持方案优化迭代</li>
              <li>自动保存完整对话记录</li>
            </ul>
            <span class="feature-arrow">&rarr;</span>
          </div>

          <div class="feature-card animate-in delay-2" @click="$router.push('/devices')">
            <div class="feature-icon icon-device">&#128187;</div>
            <h3>设备资产管理</h3>
            <p>统一管理公司所有IT设备资产，涵盖服务器、打印机、网络设备等全品类。</p>
            <ul class="feature-list">
              <li>分类筛选与关键词搜索</li>
              <li>实时状态追踪</li>
              <li>设备详情与负责人关联</li>
            </ul>
            <span class="feature-arrow">&rarr;</span>
          </div>

          <div class="feature-card animate-in delay-3" @click="$router.push('/knowledge')">
            <div class="feature-icon icon-knowledge">&#128218;</div>
            <h3>运维知识库</h3>
            <p>积累常见故障排查经验与最佳实践，构建团队专属知识体系。</p>
            <ul class="feature-list">
              <li>分类浏览与全文搜索</li>
              <li>点赞排序发现优质内容</li>
              <li>持续积累运维智慧</li>
            </ul>
            <span class="feature-arrow">&rarr;</span>
          </div>

          <div class="feature-card animate-in delay-4" @click="$router.push('/records')">
            <div class="feature-icon icon-record">&#128203;</div>
            <h3>历史记录管理</h3>
            <p>完整的故障咨询档案，多维筛选、一键导出为文本报告。</p>
            <ul class="feature-list">
              <li>多维筛选与排序</li>
              <li>点赞标记重要记录</li>
              <li>文本导出方便归档</li>
            </ul>
            <span class="feature-arrow">&rarr;</span>
          </div>

          <div class="feature-card feature-static animate-in delay-5">
            <div class="feature-icon icon-daily">&#128161;</div>
            <h3>每日常识推送</h3>
            <p>系统每日自动推送一条运维常识，帮助运维人员持续学习。</p>
            <ul class="feature-list">
              <li>每日自动更新内容</li>
              <li>涵盖六大运维领域</li>
              <li>碎片时间高效学习</li>
            </ul>
          </div>

          <div class="feature-card animate-in delay-6" @click="$router.push('/announcements')">
            <div class="feature-icon icon-announce">&#128227;</div>
            <h3>系统公告中心</h3>
            <p>管理员发布的官方通知，统一展示渠道。</p>
            <ul class="feature-list">
              <li>置顶公告优先展示</li>
              <li>类型分级管理</li>
              <li>首页即时触达用户</li>
            </ul>
            <span class="feature-arrow">&rarr;</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== 使用指南 ========== -->
    <section class="section guide-section">
      <div class="section-container">
        <div class="section-header animate-in">
          <h2>使用指南</h2>
          <p>简单四步，轻松完成设备故障诊断</p>
        </div>
        <div class="guide-steps">
          <div class="step-card animate-in delay-1">
            <div class="step-number"><span>1</span></div>
            <div class="step-content">
              <h3>登录系统</h3>
              <p>使用账号密码登录平台。密码采用 MD5 加密存储确保安全。</p>
            </div>
            <span class="step-badge badge-blue">&#128274; 认证安全</span>
          </div>
          <div class="step-line"></div>
          <div class="step-card animate-in delay-2">
            <div class="step-number"><span>2</span></div>
            <div class="step-content">
              <h3>描述故障</h3>
              <p>详细描述遇到的问题，可选择性上传截图辅助 AI 分析。</p>
            </div>
            <span class="step-badge badge-purple">&#128247; 多模态输入</span>
          </div>
          <div class="step-line"></div>
          <div class="step-card animate-in delay-3">
            <div class="step-number"><span>3</span></div>
            <div class="step-content">
              <h3>获取方案</h3>
              <p>AI 大模型在数秒内返回结构化解决方案。</p>
            </div>
            <span class="step-badge badge-green">&#9889; 秒级响应</span>
          </div>
          <div class="step-line"></div>
          <div class="step-card animate-in delay-4">
            <div class="step-number"><span>4</span></div>
            <div class="step-content">
              <h3>优化归档</h3>
              <p>不满意可触发优化，满意后点赞收藏并导出。</p>
            </div>
            <span class="step-badge badge-orange">&#8635; 持续改进</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== 页脚 ========== -->
    <footer class="home-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <strong>&#9881; 东软智能运维咨询平台</strong>
          <span>NeusoftAI Intelligent O&amp;M Platform</span>
        </div>
        <div class="footer-nav">
          <router-link to="/">首页</router-link>
          <router-link to="/consult">故障咨询</router-link>
          <router-link to="/devices">设备资产</router-link>
          <router-link to="/knowledge">知识库</router-link>
        </div>
        <p class="footer-copy">&copy; 2026 NeusoftAI. Built with SpringBoot + Vue3 + MyBatis-Plus + Ollama</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { announceApi } from '../api'

const router = useRouter()
const userStore = useUserStore()

const latestAnnouncements = ref([])
const typeMap = {
  NOTICE: '通知',
  WARNING: '警告',
  MAINTAIN: '维护',
  UPGRADE: '升级'
}

function handleLogout() { userStore.logout(); router.push('/login') }
function formatDate(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

onMounted(() => {
  announceApi.latest().then(res => {
    if (res.data?.records) latestAnnouncements.value = res.data.records.slice(0, 3)
  }).catch(() => {})
})
</script>

<style scoped>
/* ===== 基础布局 ===== */
.home-page { min-height: 100vh; background: var(--bg); }

.section { padding: 80px 0; }
.section-container { max-width: 1160px; margin: 0 auto; padding: 0 28px; }
.section-header { text-align: center; margin-bottom: 56px; }
.section-header h2 {
  font-size: 34px;
  font-weight: 800;
  color: var(--text);
  margin-bottom: 10px;
  letter-spacing: -0.5px;
}
.section-header p { font-size: 16px; color: var(--text-secondary); max-width: 520px; margin: 0 auto; line-height: 1.65; }

/* ===== Hero 区域 ===== */
.hero-section {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0c1222 0%, #162033 35%, #1e2945 70%, #1a1a3e 100%);
  min-height: 560px;
  display: flex;
  align-items: center;
}
.hero-bg-layer {
  position: absolute; inset: 0;
}
.hero-glow {
  position: absolute; border-radius: 50%; filter: blur(90px);
}
.hero-glow-1 {
  width: 500px; height: 500px;
  background: rgba(79,110,247,0.18);
  top: -15%; left: -5%;
  animation: floatSlow 12s ease-in-out infinite;
}
.hero-glow-2 {
  width: 400px; height: 400px;
  background: rgba(139,92,246,0.13);
  bottom: -15%; right: -5%;
  animation: floatSlow 15s ease-in-out infinite reverse;
}
.hero-grid {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(79,110,247,0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(79,110,247,0.04) 1px, transparent 1px);
  background-size: 50px 50px;
}
.hero-particles {
  position: absolute; inset: 0;
  background-image:
    radial-gradient(1px 1px at 20% 30%, rgba(255,255,255,0.25) 50%, transparent 50%),
    radial-gradient(1px 1px at 60% 20%, rgba(255,255,255,0.2) 50%, transparent 50%),
    radial-gradient(1px 1px at 80% 60%, rgba(255,255,255,0.22) 50%, transparent 50%),
    radial-gradient(1px 1px at 30% 80%, rgba(255,255,255,0.18) 50%, transparent 50%);
  background-size: 200px 200px;
  animation: subtleBreath 6s ease-in-out infinite alternate;
}

.hero-content {
  position: relative;
  max-width: 1160px;
  margin: 0 auto;
  padding: 64px 28px 56px;
  display: flex;
  align-items: center;
  gap: 56px;
}

.hero-text { flex: 1; max-width: 550px; }
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 30px;
  background: rgba(79,110,247,0.15);
  border: 1px solid rgba(79,110,247,0.2);
  color: var(--primary-light);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  margin-bottom: 22px;
}
.hero-title {
  font-size: 44px;
  font-weight: 900;
  color: #fff;
  line-height: 1.15;
  margin-bottom: 18px;
  letter-spacing: -1px;
}
.highlight-text {
  background: linear-gradient(135deg, #60a5fa, #a78bfa, #f472b6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  background-size: 200% auto;
  animation: gradientShift 4s linear infinite;
}
.hero-subtitle {
  font-size: 16px;
  color: rgba(255,255,255,0.58);
  line-height: 1.75;
  margin-bottom: 32px;
}

/* Hero 按钮 */
.hero-actions { display: flex; gap: 14px; align-items: center; margin-bottom: 42px; flex-wrap: wrap; }
.btn-hero-main {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 14px 30px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--primary), #6366f1);
  color: #fff; font-size: 16px; font-weight: 700;
  text-decoration: none;
  box-shadow: 0 6px 24px rgba(79,110,247,0.4);
  transition: all 0.35s var(--ease-out);
  letter-spacing: 0.3px;
}
.btn-hero-main:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 10px 36px rgba(79,110,247,0.5);
}
.btn-hero-ghost {
  display: inline-flex; align-items: center;
  padding: 13px 24px;
  border-radius: var(--radius-lg);
  background: rgba(255,255,255,0.07);
  color: rgba(255,255,255,0.75);
  font-size: 15px; font-weight: 600;
  text-decoration: none;
  border: 1px solid rgba(255,255,255,0.12);
  transition: all 0.3s var(--ease-out);
}
.btn-hero-ghost:hover {
  background: rgba(255,255,255,0.14);
  color: #fff; border-color: rgba(255,255,255,0.22);
}

/* 统计数字 */
.hero-stats {
  display: flex; align-items: center; gap: 28px;
  padding-top: 30px;
  border-top: 1px solid rgba(255,255,255,0.08);
}
.stat-item { display: flex; flex-direction: column; }
.stat-num {
  font-size: 26px; font-weight: 900;
  background: linear-gradient(135deg, #60a5fa, #a78bfa);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
.stat-label { font-size: 12px; color: rgba(255,255,255,0.38); margin-top: 2px; letter-spacing: 0.5px; }
.stat-dot {
  width: 3px; height: 3px; border-radius: 50%;
  background: rgba(255,255,255,0.15);
  align-self: center;
  margin-bottom: 18px;
}

/* 右侧视觉元素 */
.hero-visual { flex: 1; display: flex; justify-content: center; }
.visual-stack {
  position: relative; width: 340px; height: 360px;
}
.visual-card {
  position: absolute;
  padding: 16px 22px;
  background: rgba(255,255,255,0.08);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 16px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  display: flex; align-items: center; gap: 10px;
  white-space: nowrap;
  transition: all 0.35s var(--ease-out);
  cursor: default;
}
.visual-card:hover {
  transform: translateY(-4px) scale(1.02) !important;
  background: rgba(255,255,255,0.14);
  border-color: rgba(255,255,255,0.22);
  box-shadow: 0 12px 36px rgba(0,0,0,0.3);
}
.vc-icon { font-size: 20px; }
.vc-status {
  margin-left: auto;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
}
.vc-ok { background: rgba(16,185,129,0.2); color: #34d399; }
.vc-warn { background: rgba(245,158,11,0.2); color: #fbbf24; }

.v-card-1 { top: 10px; left: 10px; animation: floatCard 5s ease-in-out infinite; }
.v-card-2 { top: 45%; right: 0; animation: floatCard 5s ease-in-out infinite -1.6s; }
.v-card-3 { bottom: 10px; left: 25%; animation: floatCard 5s ease-in-out infinite -3.2s; }

.center-orb {
  position: absolute; top: 48%; left: 52%;
  transform: translate(-50%, -50%);
  width: 160px; height: 160px; border-radius: 50%;
  background: radial-gradient(circle, rgba(96,165,250,0.2) 0%, rgba(167,139,250,0.1) 50%, transparent 70%);
  filter: blur(20px);
  animation: pulseGlow 4s ease-in-out infinite;
}

/* ===== 特性网格 ===== */
.features-section { background: var(--bg); }
.feature-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 22px;
}
.feature-card {
  background: var(--card-solid);
  border-radius: var(--radius-lg);
  padding: 30px 26px;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
}
.feature-card::before {
  content: '';
  position: absolute; top: 0; left: 0; right: 0; height: 3px;
  background: linear-gradient(90deg, var(--primary), #8b5cf6);
  opacity: 0;
  transition: opacity 0.3s;
}
.feature-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg), var(--shadow-glow);
  border-color: rgba(79,110,247,0.2);
}
.feature-card:hover::before { opacity: 1; }

.feature-icon {
  width: 54px; height: 54px; border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; margin-bottom: 20px;
  transition: transform 0.3s var(--ease-spring);
}
.feature-card:hover .feature-icon { transform: scale(1.1) rotate(-3deg); }
.icon-diagnose   { background: linear-gradient(135deg, #dbeafe, #bfdbfe); color: #2563eb; }
.icon-device     { background: linear-gradient(135deg, #fef3c7, #fde68a); color: #d97706; }
.icon-knowledge  { background: linear-gradient(135deg, #d1fae5, #a7f3d0); color: #059669; }
.icon-record     { background: linear-gradient(135deg, #fce7f3, #fbcfe8); color: #db2777; }
.icon-daily      { background: linear-gradient(135deg, #ede9fe, #ddd6fe); color: #7c3aed; }
.icon-announce   { background: linear-gradient(135deg, #ffedd5, #fed7aa); color: #ea580c; }

.feature-card h3 { font-size: 17px; margin-bottom: 8px; color: var(--text); font-weight: 700; }
.feature-card p { font-size: 13.5px; line-height: 1.68; color: var(--text-secondary); margin-bottom: 14px; }

.feature-list {
  list-style: none; padding: 0; margin: 0 0 14px 0;
}
.feature-list li {
  position: relative; padding-left: 20px; font-size: 13px;
  color: var(--text-secondary); line-height: 1.8;
}
.feature-list li::before {
  content: '\2713'; position: absolute; left: 0;
  color: var(--success); font-weight: 800; font-size: 11px;
  top: 1px;
}
.feature-arrow {
  position: absolute; bottom: 22px; right: 22px;
  font-size: 22px; color: var(--primary);
  opacity: 0; transform: translateX(-8px);
  transition: all 0.3s var(--ease-out);
}
.feature-card:hover .feature-arrow { opacity: 1; transform: translateX(0); }

/* ===== 使用指南 ===== */
.guide-section {
  background: linear-gradient(180deg, #fafbff, var(--bg));
}
.guide-steps {
  display: flex; justify-content: center; gap: 0; align-items: stretch;
}
.step-card {
  flex: 1; max-width: 240px;
  background: var(--card-solid);
  border-radius: var(--radius-lg);
  padding: 30px 22px; text-align: center;
  box-shadow: var(--shadow);
  border: 1px solid var(--border-light);
  transition: all 0.35s var(--ease-out);
  position: relative;
  overflow: hidden;
}
.step-card::after {
  content: '';
  position: absolute; bottom: 0; left: 0; right: 0; height: 3px;
  background: linear-gradient(90deg, var(--primary), #8b5cf6);
  opacity: 0; transition: opacity 0.3s;
}
.step-card:hover { transform: translateY(-5px); box-shadow: var(--shadow-md); }
.step-card:hover::after { opacity: 1; }

.step-number {
  width: 48px; height: 48px; border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), #6366f1);
  color: #fff; font-size: 20px; font-weight: 900;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 18px;
  box-shadow: 0 6px 20px rgba(79,110,247,0.3);
  transition: transform 0.3s var(--ease-spring);
}
.step-card:hover .step-number { transform: scale(1.1); }
.step-card h3 { font-size: 16px; margin-bottom: 8px; color: var(--text); font-weight: 700; }
.step-card p { font-size: 13px; line-height: 1.65; color: var(--text-secondary); }
.step-badge {
  display: inline-block; margin-top: 14px; padding: 4px 14px;
  border-radius: 20px; font-size: 11px; font-weight: 600;
}
.badge-blue   { background: var(--info-light); color: #0369a1; }
.badge-purple { background: #ede9fe; color: #7c3aed; }
.badge-green  { background: var(--success-light); color: #047857; }
.badge-orange { background: var(--warning-light); color: #b45309; }

.step-line {
  width: 36px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  position: relative;
}
.step-line::before {
  content: '';
  width: 24px; height: 2px;
  background: repeating-linear-gradient(
    90deg,
    var(--border) 0, var(--border) 4px,
    transparent 4px, transparent 8px
  );
}

/* ===== 公告区域（顶部醒目） ===== */
.announce-top-section {
  background: linear-gradient(135deg, #eff6ff 0%, #f0f4ff 50%, #faf5ff 100%);
  padding: 60px 0;
  border-bottom: 1px solid rgba(79,110,247,0.1);
  position: relative;
  overflow: hidden;
}
.announce-top-section::before {
  content: '';
  position: absolute; top: -60px; right: -40px;
  width: 240px; height: 240px; border-radius: 50%;
  background: radial-gradient(circle, rgba(79,110,247,0.08), transparent 70%);
}
.announce-top-section::after {
  content: '';
  position: absolute; bottom: -80px; left: -30px;
  width: 200px; height: 200px; border-radius: 50%;
  background: radial-gradient(circle, rgba(139,92,246,0.06), transparent 70%);
}

.announce-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 32px;
}
.announce-main-title {
  font-size: 28px; margin: 0; font-weight: 800;
  color: var(--text); display: flex; align-items: center; gap: 10px;
}
.announce-icon { font-size: 26px; }
.view-all-btn {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 14px; color: var(--primary); font-weight: 600; text-decoration: none;
  padding: 9px 18px; border-radius: var(--radius-sm);
  border: 1.5px solid var(--border);
  background: var(--card-solid);
  transition: all 0.28s var(--ease-out);
}
.view-all-btn:hover {
  background: var(--info-light);
  border-color: var(--primary);
  box-shadow: 0 2px 10px var(--primary-glow);
}

.announce-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px;
}
.announce-card {
  background: var(--card-solid);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  border-left: 4px solid var(--border);
  transition: all 0.35s var(--ease-out);
  display: flex; flex-direction: column;
  cursor: default;
}
.announce-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
  border-color: var(--info);
}
.announce-card:nth-child(1) { border-left-color: var(--primary); }
.announce-card:nth-child(2) { border-left-color: var(--danger); }
.announce-card:nth-child(3) { border-left-color: var(--success); }

.announce-top { display: flex; gap: 8px; align-items: center; margin-bottom: 12px; }
.announce-type-tag {
  padding: 3px 12px; border-radius: 10px; font-size: 11px; font-weight: 700;
}
.tag-notice   { background: var(--info-light); color: #0369a1; }
.tag-warning { background: var(--danger-light); color: #dc2626; }
.tag-maintain{ background: var(--success-light); color: #047857; }
.tag-upgrade { background: #ede9fe; color: #7c3aed; }

.top-badge {
  background: var(--danger); color: #fff; padding: 2px 9px;
  border-radius: 10px; font-size: 10px; font-weight: 700;
}
.announce-title {
  font-size: 16px; font-weight: 700; color: var(--text);
  margin-bottom: 8px; line-height: 1.45;
}
.announce-content {
  font-size: 13.5px; color: var(--text-secondary);
  line-height: 1.7; flex: 1; margin: 0;
}
.announce-time {
  font-size: 12px; color: var(--text-muted);
  margin-top: 14px; padding-top: 12px;
  border-top: 1px solid var(--border-light);
}

/* ===== 页脚 ===== */
.home-footer {
  background: linear-gradient(135deg, #0c1222, #162033);
  color: rgba(255,255,255,0.55);
  padding: 48px 0 32px;
}
.footer-inner {
  max-width: 1160px; margin: 0 auto; padding: 0 28px;
  text-align: center;
}
.footer-brand strong {
  display: block; font-size: 19px; color: #fff; margin-bottom: 4px; font-weight: 800;
}
.footer-brand span { font-size: 12px; opacity: 0.4; letter-spacing: 0.5px; }
.footer-nav {
  display: flex; justify-content: center; gap: 28px; margin: 24px 0; flex-wrap: wrap;
}
.footer-nav a {
  color: rgba(255,255,255,0.5); font-size: 14px; text-decoration: none;
  transition: all 0.25s var(--ease-out); font-weight: 500;
  padding: 4px 0;
  position: relative;
}
.footer-nav a::after {
  content: '';
  position: absolute; bottom: 0; left: 0; width: 0; height: 2px;
  background: var(--primary-light);
  transition: width 0.3s var(--ease-out);
}
.footer-nav a:hover { color: #fff; }
.footer-nav a:hover::after { width: 100%; }

.footer-copy { font-size: 12px; opacity: 0.3; }

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .hero-content { flex-direction: column; text-align: center; }
  .hero-text { max-width: 100%; }
  .hero-actions { justify-content: center; }
  .hero-stats { justify-content: center; flex-wrap: wrap; }
  .hero-visual { display: none; }
  .feature-grid { grid-template-columns: repeat(2, 1fr); }
  .guide-steps { flex-direction: column; gap: 16px; max-width: 480px; margin: 0 auto; }
  .step-line { display: none; }
  .step-card { max-width: 100%; }
  .announce-grid { grid-template-columns: 1fr; }
}
@media (max-width: 600px) {
  .hero-title { font-size: 32px; }
  .feature-grid { grid-template-columns: 1fr; }
  .section { padding: 50px 0; }
}
</style>

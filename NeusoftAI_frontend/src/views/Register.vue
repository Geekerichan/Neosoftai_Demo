<template>
  <div class="auth-page">
    <!-- 动态背景 -->
    <div class="auth-bg">
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>
      <div class="bg-orb orb-3"></div>
      <div class="bg-grid"></div>
    </div>

    <!-- 注册卡片 -->
    <div class="auth-card animate-scale">
      <div class="auth-card-accent"></div>

      <div class="auth-logo">
        <div class="logo-icon">&#10010;</div>
      </div>

      <h2 class="auth-title">创建账号</h2>
      <p class="auth-subtitle">加入东软智能运维咨询平台</p>

      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group animate-in delay-1">
          <label>账号（6-12位）</label>
          <div class="input-wrap">
            <span class="input-icon">&#128100;</span>
            <input
              type="text"
              v-model="form.username"
              placeholder="设置你的登录账号"
              required
              minlength="6"
              maxlength="12"
            />
          </div>
          <small class="input-hint">支持字母、数字、下划线组合</small>
        </div>

        <div class="form-group animate-in delay-2">
          <label>密码（≥6位）</label>
          <div class="input-wrap">
            <span class="input-icon">&#128274;</span>
            <input
              type="password"
              v-model="form.password"
              placeholder="设置登录密码"
              required
              minlength="6"
              maxlength="32"
            />
          </div>
          <small class="input-hint">密码将加密存储</small>
        </div>

        <div class="form-group animate-in delay-3">
          <label>确认密码</label>
          <div class="input-wrap">
            <span class="input-icon">&#128274;</span>
            <input
              type="password"
              v-model="form.confirmPwd"
              placeholder="再次输入密码"
              required
              minlength="6"
            />
          </div>
        </div>

        <button type="submit" class="btn btn-primary btn-login animate-in delay-4" :disabled="loading">
          <template v-if="!loading">
            <span>注 册</span>
            <span class="btn-shine"></span>
          </template>
          <template v-else>
            <span class="loading-spinner"></span>
            注册中...
          </template>
        </button>
      </form>

      <div class="auth-footer animate-in delay-5">
        已有账号？
        <router-link to="/login" class="auth-link">返回登录</router-link>
      </div>
    </div>

    <p class="auth-copy">NeusoftAI &copy; 2026 &middot; 智能运维平台</p>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '', confirmPwd: '' })

async function handleRegister() {
  if (form.username.length < 6 || form.username.length > 12) {
    window.showToast?.('账号长度必须在6-12位之间', 'error')
    return
  }
  if (form.password.length < 6) {
    window.showToast?.('密码不能少于6位', 'error')
    return
  }
  if (form.password !== form.confirmPwd) {
    window.showToast?.('两次密码不一致', 'error')
    return
  }

  loading.value = true
  try {
    const res = await authApi.register(form.username, form.password)
    window.showToast?.(res.message || '注册成功！', 'success')
    setTimeout(() => router.push('/login'), 1000)
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 40%, #312e81 100%);
}

/* ===== 动态背景效果 ===== */
.auth-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}
.bg-grid {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: floatSlow 8s ease-in-out infinite;
}
.orb-1 {
  width: 400px; height: 400px;
  background: rgba(139,92,246,0.18);
  top: -10%; right: -5%;
  animation-delay: -2s;
}
.orb-2 {
  width: 350px; height: 350px;
  background: rgba(79,110,247,0.14);
  bottom: -10%; left: -5%;
  animation-delay: -5s;
}
.orb-3 {
  width: 200px; height: 200px;
  background: rgba(16,185,129,0.12);
  top: 40%; left: 20%;
  animation-delay: -3s;
}

.auth-card {
  position: relative;
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: var(--radius-xl);
  padding: 36px 40px 32px;
  width: 420px;
  box-shadow:
    0 25px 60px rgba(0,0,0,0.25),
    0 0 0 1px rgba(255,255,255,0.1) inset;
  z-index: 10;
  overflow: hidden;
}
.auth-card-accent {
  position: absolute; top: 0; left: 0; right: 0; height: 4px;
  background: linear-gradient(90deg, #8b5cf6, #ec4899, var(--primary));
  background-size: 200% 100%;
  animation: gradientShift 4s ease infinite;
}

.auth-logo { text-align: center; margin-bottom: 20px; }
.logo-icon {
  display: inline-flex; align-items: center; justify-content: center;
  width: 60px; height: 60px; border-radius: 20px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  color: #fff; font-size: 26px;
  box-shadow: 0 8px 24px rgba(139,92,246,0.35);
  animation: pulseGlow 3s ease-in-out infinite;
}

.auth-title {
  text-align: center;
  font-size: 26px;
  font-weight: 800;
  color: var(--text);
  margin-bottom: 6px;
  letter-spacing: -0.5px;
}
.auth-subtitle {
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 28px;
}

.input-wrap { position: relative; }
.input-icon {
  position: absolute;
  left: 15px;
  top: 50%; transform: translateY(-50%);
  font-size: 16px; opacity: 0.45;
  transition: opacity 0.25s;
  pointer-events: none; z-index: 1;
}
.input-wrap input { padding-left: 42px; }
.input-wrap:focus-within .input-icon { opacity: 0.75; }

.input-hint {
  display: block;
  color: var(--text-muted);
  font-size: 12px;
  margin-top: 6px;
  letter-spacing: 0.2px;
}

.btn-login {
  margin-top: 8px;
  position: relative;
  overflow: hidden;
}
.btn-shine {
  position: absolute; top: 0; left: -80px;
  width: 60px; height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.25), transparent);
  transform: skewX(-20deg);
  animation: shine 3s infinite;
}

@keyframes shine {
  0%, 100% { left: -80px; }
  15%, 45% { left: calc(100% + 80px); }
  46%, 99% { left: calc(100% + 80px); }
}

.auth-footer {
  text-align: center;
  margin-top: 22px;
  font-size: 14px;
  color: var(--text-secondary);
}
.auth-link {
  color: var(--primary);
  font-weight: 700;
  margin-left: 4px;
  transition: all 0.25s var(--ease-out);
}
.auth-link:hover {
  color: var(--primary-dark);
  text-shadow: 0 0 12px var(--primary-glow);
}

.auth-copy {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: rgba(255,255,255,0.25);
  z-index: 10;
  letter-spacing: 0.5px;
}
</style>

<template>
  <div class="login-page">
    <!-- 背景图：用 img 标签确保 Vite 能正确处理资源路径 -->
    <img class="bg-image" src="@/assets/pexels-lukas-hartmann-304281-880675.jpg" alt="" />
    <!-- 深色遮罩层 -->
    <div class="bg-overlay" />

    <!-- 顶部导航栏 -->
    <nav class="top-nav">
      <div class="nav-logo">
        <div class="nav-logo-mark">KN</div>
        <span class="nav-logo-text">KeepNote</span>
      </div>
      <div class="nav-links">
        <span>功能</span>
        <span>关于</span>
        <span>帮助</span>
      </div>
    </nav>

    <!-- 主体内容 -->
    <div class="main-content">
      <!-- 左侧大字标题区 -->
      <div class="hero-section">
        <p class="hero-label">PERSONAL KNOWLEDGE BASE</p>
        <h1 class="hero-title">
          Capture<br />
          Every Thought
        </h1>
        <p class="hero-desc">
          KeepNote helps you organize ideas, notes, and knowledge<br />
          in one elegant, distraction-free workspace.
        </p>
      </div>

      <!-- 右侧登录卡片 -->
      <div class="login-card">
        <div class="card-header">
          <div class="logo">
            <div class="logo-mark">KN</div>
            <div class="logo-text">Keep<span>Note</span></div>
          </div>
          <p class="card-subtitle">{{ activeTab === 'login' ? 'Welcome back.' : 'Create your account.' }}</p>
        </div>

        <!-- Tab 切换 -->
        <div class="tab-switcher">
          <button
            :class="['tab-btn', activeTab === 'login' ? 'active' : '']"
            @click="activeTab = 'login'"
          >登录</button>
          <button
            :class="['tab-btn', activeTab === 'register' ? 'active' : '']"
            @click="activeTab = 'register'"
          >注册</button>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
          <el-form-item prop="username">
            <div class="field-label">用户名</div>
            <el-input
              v-model="form.username"
              placeholder="Enter your username"
              size="large"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="password">
            <div class="field-label">密码</div>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="Enter your password"
              size="large"
              class="custom-input"
              show-password
              @keyup.enter="submit"
            />
          </el-form-item>

          <button
            class="submit-btn"
            :disabled="loading"
            @click.prevent="submit"
          >
            <span v-if="!loading">{{ activeTab === 'login' ? '登录 →' : '注册 →' }}</span>
            <span v-else class="loading-dot">...</span>
          </button>
        </el-form>

        <p class="card-footer">
          {{ activeTab === 'login' ? '还没有账户？' : '已有账户？' }}
          <a @click="activeTab = activeTab === 'login' ? 'register' : 'login'">
            {{ activeTab === 'login' ? '立即注册' : '去登录' }}
          </a>
        </p>
      </div>
    </div>

    <!-- 底部 -->
    <footer class="page-footer">
      <span>© 2026 KeepNote</span>
      <span>Personal · Private · Powerful</span>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('login')
const loading = ref(false)
const formRef = ref(null)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    if (activeTab.value === 'login') {
      await userStore.login(form.username, form.password)
    } else {
      await userStore.register(form.username, form.password)
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== 页面基础 ===== */
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', 'PingFang SC', system-ui, sans-serif;
}

/* ① 背景图：用 img 标签铺满，filter blur 不会被 overflow:hidden 影响 */
.bg-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transform: scale(1);
  z-index: 0;
}

/* ② 深橄榄绿半透明遮罩 ~30% */
.bg-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(45, 62, 51, 0.42) 0%,
    rgba(45, 62, 51, 0.25) 45%,
    rgba(20, 30, 20, 0.62) 100%
  );
  pointer-events: none;
  z-index: 1;
}

/* ===== 顶部导航 ===== */
.top-nav {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 48px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-logo-mark {
  width: 28px;
  height: 28px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.25);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 0.5px;
}

.nav-logo-text {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255,255,255,0.9);
  letter-spacing: 0.3px;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-links span {
  font-size: 13px;
  color: rgba(255,255,255,0.55);
  cursor: pointer;
  letter-spacing: 0.3px;
  transition: color 0.2s;
}

.nav-links span:hover {
  color: rgba(255,255,255,0.9);
}

/* ===== 主体区域 ===== */
.main-content {
  position: relative;
  z-index: 10;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 60px 48px 40px;
  gap: 40px;
  max-width: 1100px;
  margin: 0 auto;
  width: 100%;
}

/* ===== 左侧 Hero ===== */
.hero-section {
  flex: 1;
  padding-right: 40px;
}

.hero-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 2.5px;
  color: rgba(255,255,255,0.4);
  margin-bottom: 20px;
  text-transform: uppercase;
}

.hero-title {
  font-size: clamp(42px, 5vw, 64px);
  font-weight: 300;
  line-height: 1.15;
  color: rgba(255,255,255,0.92);
  margin: 0 0 24px 0;
  letter-spacing: -1px;
}

.hero-desc {
  font-size: 14px;
  line-height: 1.8;
  color: rgba(255,255,255,0.45);
  max-width: 400px;
}

/* ===== 右侧登录卡片 ===== */
.login-card {
  width: 380px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 4px;
  padding: 36px 32px 28px;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.card-header {
  margin-bottom: 28px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.logo-mark {
  width: 30px;
  height: 30px;
  background: rgba(255,255,255,0.12);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 0.5px;
}

.logo-text {
  font-size: 15px;
  font-weight: 700;
  color: rgba(255,255,255,0.88);
  letter-spacing: 0.2px;
}

.logo-text span {
  color: rgba(180, 210, 130, 0.9);
}

.card-subtitle {
  font-size: 22px;
  font-weight: 300;
  color: rgba(255,255,255,0.75);
  margin: 0;
  letter-spacing: -0.3px;
}

/* ===== Tab 切换 ===== */
.tab-switcher {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.tab-btn {
  flex: 1;
  background: none;
  border: none;
  padding: 10px 0;
  font-size: 13px;
  font-weight: 500;
  color: rgba(255,255,255,0.35);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: all 0.2s;
  letter-spacing: 0.3px;
}

.tab-btn.active {
  color: rgba(255,255,255,0.9);
  border-bottom-color: rgba(180, 210, 130, 0.8);
}

.tab-btn:hover:not(.active) {
  color: rgba(255,255,255,0.6);
}

/* ===== 表单 ===== */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1.5px;
  color: rgba(255,255,255,0.4);
  margin-bottom: 6px;
  text-transform: uppercase;
}

/* 覆盖 Element Plus input 样式 */
.login-form :deep(.el-input__wrapper) {
  background: transparent !important;
  border: none !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.35) !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  padding: 4px 0 !important;
  transition: border-color 0.2s;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-bottom-color: rgba(255, 255, 255, 0.6) !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-bottom-color: rgba(180, 210, 130, 0.85) !important;
  box-shadow: none !important;
}

.login-form :deep(.el-input__inner) {
  background: transparent !important;
  color: var(--text) !important;
  font-size: 14px !important;
  letter-spacing: 0.2px;
  caret-color: rgba(180, 210, 130, 0.9);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.3) !important;
}

.login-form :deep(.el-input__suffix .el-icon) {
  color: rgba(255, 255, 255, 0.4) !important;
}

.login-form :deep(.el-form-item__error) {
  color: rgba(255, 160, 130, 0.85);
  font-size: 11px;
}

/* ===== 提交按钮 ===== */
.submit-btn {
  width: 100%;
  margin-top: 12px;
  padding: 13px 0;
  background: rgba(180, 210, 130, 0.15);
  border: 1px solid rgba(180, 210, 130, 0.4);
  border-radius: 3px;
  color: rgba(200, 230, 150, 0.95);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: rgba(180, 210, 130, 0.25);
  border-color: rgba(180, 210, 130, 0.65);
  color: #fff;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-dot {
  letter-spacing: 3px;
}

/* ===== 底部切换链接 ===== */
.card-footer {
  margin-top: 18px;
  text-align: center;
  font-size: 12px;
  color: rgba(255,255,255,0.3);
}

.card-footer a {
  color: rgba(180, 210, 130, 0.75);
  cursor: pointer;
  margin-left: 4px;
  text-decoration: none;
  transition: color 0.2s;
}

.card-footer a:hover {
  color: rgba(200, 230, 150, 1);
}

/* ===== 页脚 ===== */
.page-footer {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 48px;
  border-top: 1px solid rgba(255,255,255,0.06);
  font-size: 11px;
  color: rgba(255,255,255,0.22);
  letter-spacing: 0.5px;
}
</style>

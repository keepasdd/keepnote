<template>
  <aside class="sidebar">
    <div class="logo">
      <div class="logo-mark">KN</div>
      <div class="logo-text">Keep<span>Note</span></div>
    </div>

    <!-- 搜索 -->
    <div class="search-wrap">
      <el-input
          v-model="searchKeyword"
          placeholder="搜索笔记…"
          :prefix-icon="Search"
          clearable
          @input="emit('search', searchKeyword)"
      />
    </div>

    <!-- 导航 -->
    <div class="nav-section">
      <div class="nav-label">导航</div>
      <div
          v-for="item in navItems" :key="item.key"
          class="nav-item"
          :class="{ active: activeNav === item.key }"
          @click="emit('nav-change', item.key)"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        {{ item.label }}
        <span class="badge">{{ item.count }}</span>
      </div>
    </div>

    <div class="sidebar-divider" />

    <!-- 分类 -->
    <div class="nav-section">
      <div class="nav-label">分类</div>
    </div>
    <div class="category-list">
      <div
          v-for="cat in categories" :key="cat.id"
          class="category-item"
          :class="{ active: activeCategoryId === cat.id }"
          @click="emit('category-change', cat.id)"
      >
        <span class="category-dot" :style="{ background: cat.color || '#aaa' }" />
        {{ cat.name }}
        <span class="count">{{ cat.noteCount ?? 0 }}</span>
      </div>
      <div class="category-item add-btn" @click="showAddCategory = true">
        <el-icon><Plus /></el-icon> 新增分类
      </div>
    </div>

    <!-- 用户区 -->
    <div class="user-area" @click="openProfile">
      <div class="avatar">
        <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" class="avatar-img" />
        <span v-else>{{ avatarChar }}</span>
      </div>
      <div class="user-info">
        <div class="user-name">{{ displayName }}</div>
        <div class="user-role">{{ userStore.userInfo?.email || '点击编辑资料' }}</div>
      </div>
      <el-icon style="color:var(--text-dim)"><Setting /></el-icon>
    </div>

    <!-- 新增分类弹窗 -->
    <el-dialog v-model="showAddCategory" title="新增分类" width="320px" append-to-body>
      <el-form :model="categoryForm">
        <el-form-item label="名称">
          <el-input v-model="categoryForm.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="颜色">
          <div class="color-options">
            <span
                v-for="c in colorOptions" :key="c"
                class="color-dot"
                :style="{ background: c, outline: categoryForm.color === c ? `2px solid ${c}` : 'none' }"
                @click="categoryForm.color = c"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddCategory = false">取消</el-button>
        <el-button type="primary" @click="submitCategory">确定</el-button>
      </template>
    </el-dialog>

    <!-- 使用 SettingsDrawer 显示个人资料与界面设置 -->
    <SettingsDrawer v-model:visible="settingsVisible">
      <template #profile>
        <!-- 把原来 profile-panel 的主要内容放入此插槽 -->
        <div class="profile-panel-slot">
          <div class="profile-bg-glow" />

          <div class="profile-topbar">
            <div class="profile-topbar-logo">
              <div class="nav-logo-mark">KN</div>
              <span class="nav-logo-text">KeepNote</span>
            </div>
            <button class="profile-close-btn" @click="settingsVisible = false">✕</button>
          </div>

          <div class="profile-body">
            <div class="profile-hero">
              <p class="profile-hero-label">ACCOUNT SETTINGS</p>
              <h2 class="profile-hero-title">个人资料</h2>
              <p class="profile-hero-desc">管理你的头像、昵称与联系方式。</p>

              <div class="profile-avatar-display" @click="triggerFileInput" title="点击更换头像">
                <img v-if="profileForm.avatar" :src="profileForm.avatar" class="profile-avatar-img" />
                <span v-else class="profile-avatar-char">{{ avatarChar }}</span>
                <div class="profile-avatar-overlay">
                  <el-icon size="20"><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </div>
              <input ref="fileInputRef" type="file" accept="image/*" style="display:none" @change="onFileChange" />
              <p class="profile-avatar-hint">点击头像上传图片，或在右侧填写 URL</p>
            </div>

            <div class="profile-card">
              <div class="profile-field">
                <div class="profile-field-label">头像 URL</div>
                <input
                  v-model="profileForm.avatar"
                  class="profile-input"
                  placeholder="https://example.com/avatar.png"
                />
              </div>

              <div class="profile-field">
                <div class="profile-field-label">昵称</div>
                <input
                  v-model="profileForm.nickname"
                  class="profile-input"
                  placeholder="你想被怎么称呼？"
                />
              </div>

              <div class="profile-field">
                <div class="profile-field-label">邮箱</div>
                <input
                  v-model="profileForm.email"
                  class="profile-input"
                  type="email"
                  placeholder="your@email.com"
                />
              </div>

              <button
                class="profile-save-btn"
                :disabled="savingProfile"
                @click="saveProfile"
              >
                <span v-if="!savingProfile">保存更改 →</span>
                <span v-else class="loading-dot">···</span>
              </button>

              <div class="profile-footer-links">
                <button class="profile-cancel-link" @click="settingsVisible = false">取消</button>
                <button class="profile-logout-link" @click="userStore.logout()">退出登录</button>
              </div>
            </div>
          </div>

          <footer class="profile-footer">
            <span>© 2026 KeepNote</span>
            <span>Personal · Private · Powerful</span>
          </footer>
        </div>
      </template>
    </SettingsDrawer>

  </aside>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Search, Plus, Setting, Camera } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { addCategory } from '../api/category'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import SettingsDrawer from './SettingsDrawer.vue'

const props = defineProps({
  categories: { type: Array, default: () => [] },
  activeNav: { type: String, default: 'all' },
  activeCategoryId: { type: Number, default: null },
  totalCount: { type: Number, default: 0 },
  favoriteCount: { type: Number, default: 0 },
})
const emit = defineEmits(['search', 'nav-change', 'category-change', 'category-added'])

const userStore = useUserStore()
const searchKeyword = ref('')
const showAddCategory = ref(false)
const settingsVisible = ref(false)
const savingProfile = ref(false)
const categoryForm = reactive({ name: '', color: '#4ecdc4' })
const fileInputRef = ref(null)

function triggerFileInput() {
  fileInputRef.value?.click()
}

async function onFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) return ElMessage.warning('图片不能超过 5MB')
  const formData = new FormData()
  formData.append('file', file)
  try {
    const url = await request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    profileForm.avatar = typeof url === 'string' ? url : (url.url ?? url.data ?? url)
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败')
  }
  e.target.value = ''
}
const profileForm = reactive({ nickname: '', email: '', avatar: '' })
const colorOptions = ['#ff6b6b', '#4ecdc4', '#ffe66d', '#a78bfa', '#fb923c', '#3a7d3f']

const displayName = computed(() => {
  if (userStore.userInfo?.nickname) return userStore.userInfo.nickname
  // fallback: decode from token
  const token = sessionStorage.getItem('token')
  if (!token) return '用户'
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.username || '用户'
  } catch { return '用户' }
})

const avatarChar = computed(() => displayName.value.charAt(0))

const navItems = computed(() => [
  { key: 'all', label: '全部笔记', icon: 'HomeFilled', count: props.totalCount },
  { key: 'favorite', label: '收藏夹', icon: 'StarFilled', count: props.favoriteCount },
])

function openProfile() {
  initProfileForm()
  settingsVisible.value = true
}

function initProfileForm() {
  const info = userStore.userInfo
  profileForm.nickname = info?.nickname || ''
  profileForm.email = info?.email || ''
  profileForm.avatar = info?.avatar || ''
}

async function saveProfile() {
  savingProfile.value = true
  try {
    await userStore.updateProfile({
      nickname: profileForm.nickname,
      email: profileForm.email,
      avatar: profileForm.avatar,
    })
    ElMessage.success('保存成功')
    settingsVisible.value = false
  } finally {
    savingProfile.value = false
  }
}

async function submitCategory() {
  if (!categoryForm.name.trim()) return ElMessage.warning('请输入分类名称')
  await addCategory({ name: categoryForm.name, color: categoryForm.color })
  ElMessage.success('添加成功')
  showAddCategory.value = false
  categoryForm.name = ''
  emit('category-added')
}
</script>

<style scoped>
/* ===== 侧边栏基础 ===== */
.sidebar {
  background: var(--surface);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  position: relative;
  width: 240px;
  flex-shrink: 0;
  font-family: 'Inter', 'PingFang SC', system-ui, sans-serif;
}

/* ===== Logo ===== */
.logo {
  padding: 26px 22px 18px;
  display: flex; align-items: center; gap: 10px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 8px;
}

.logo-mark {
  width: 30px; height: 30px;
  background: var(--surface2);
  border: 1px solid var(--border);
  border-radius: 7px;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; font-weight: 800; color: var(--text-h); letter-spacing: 0.5px;
}

.logo-text {
  font-size: 15px; font-weight: 700;
  color: var(--text);
  letter-spacing: 0.2px;
}
.logo-text span { color: var(--accent); }

/* ===== 搜索 ===== */
.search-wrap { padding: 0 14px 14px; }

:deep(.search-wrap .el-input__wrapper) {
  background: var(--surface2) !important;
  border: 1px solid var(--border) !important;
  border-radius: 6px !important;
  box-shadow: none !important;
  transition: border-color 0.2s;
}
:deep(.search-wrap .el-input__wrapper:hover) {
  border-color: var(--border-active) !important;
}
:deep(.search-wrap .el-input__wrapper.is-focus) {
  border-color: rgba(var(--accent-rgb),0.65) !important;
  box-shadow: none !important;
}
:deep(.search-wrap .el-input__inner) {
  background: transparent !important;
  color: var(--text) !important;
  font-size: 13px !important;
  caret-color: rgba(var(--accent-rgb),0.9);
}
:deep(.search-wrap .el-input__inner::placeholder) {
  color: var(--text-dim) !important;
}
:deep(.search-wrap .el-input__prefix .el-icon) {
  color: var(--text-dim) !important;
}

/* ===== 导航标签 ===== */
.nav-section { padding: 0 10px; margin-bottom: 2px; }

.nav-label {
  font-size: 10px; font-weight: 600;
  color: var(--text-dim);
  letter-spacing: 1.8px; text-transform: uppercase;
  padding: 4px 12px 6px;
}

.nav-item {
  display: flex; align-items: center; gap: 9px;
  padding: 8px 12px; border-radius: 6px;
  cursor: pointer;
  color: var(--text);
  font-size: 13px;
  transition: background 0.15s, color 0.15s;
}
.nav-item:hover { background: var(--surface3); color: var(--text); }
.nav-item.active {
  background: rgba(var(--accent-rgb),0.16);
  color: var(--accent);
}

.badge {
  margin-left: auto;
  background: var(--surface2);
  color: var(--text-dim);
  font-family: var(--font-mono); font-size: 10px;
  padding: 2px 6px; border-radius: 20px;
}
.nav-item.active .badge {
  background: rgba(var(--accent-rgb),0.20);
  color: rgba(var(--accent-rgb),0.95);
}

/* ===== 分割线 ===== */
.sidebar-divider { height: 1px; background: var(--border); margin: 8px 14px; }

/* ===== 分类列表 ===== */
.category-list { flex: 1; overflow-y: auto; padding: 0 10px; }
.category-list::-webkit-scrollbar { width: 3px; }
.category-list::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.15); border-radius: 3px; }

.category-item {
  display: flex; align-items: center; gap: 8px;
  padding: 7px 12px; border-radius: 6px;
  cursor: pointer;
  color: var(--text);
  font-size: 12.5px;
  transition: background 0.15s, color 0.15s;
}
.category-item:hover { background: var(--surface3); color: var(--text); }
.category-item.active {
  background: rgba(var(--accent-rgb),0.16);
  color: var(--accent);
}

.category-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.count { margin-left: auto; font-family: var(--font-mono); font-size: 11px; color: var(--text-dim); }
.category-item.active .count {
  color: rgba(var(--accent-rgb),0.95);
}

.add-btn { color: var(--accent); font-size: 12px; }
.add-btn:hover { color: var(--accent-hover) !important; }

/* ===== 用户区 ===== */
.user-area {
  padding: 12px 16px;
  border-top: 1px solid var(--border);
  display: flex; align-items: center; gap: 10px;
  cursor: pointer; transition: background 0.15s;
}
.user-area:hover { background: var(--surface3); }

.avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: linear-gradient(135deg, rgba(var(--accent-rgb),0.7), rgba(var(--accent-rgb),0.95));
  border: 1px solid var(--border);
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600; color: #fff; flex-shrink: 0;
  overflow: hidden;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; border-radius: 50%; }

.user-info { flex: 1; min-width: 0; }
.user-name { font-size: 13px; font-weight: 500; color: var(--text); }
.user-role { font-size: 11px; color: var(--text-dim); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* ===== 颜色选择点 ===== */
.color-options { display: flex; gap: 8px; flex-wrap: wrap; }
.color-dot {
  width: 22px; height: 22px; border-radius: 50%; cursor: pointer;
  outline-offset: 3px; transition: transform 0.15s;
}
.color-dot:hover { transform: scale(1.15); }

/* ===== 头像上传区（用于新增分类弹窗内，已不用于资料页）===== */
.avatar-upload { display: flex; align-items: center; gap: 12px; }
.avatar-preview {
  width: 56px; height: 56px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, rgba(var(--accent-rgb),0.5), rgba(var(--accent-rgb),0.9));
  border: 1px solid rgba(255,255,255,0.15);
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; font-weight: 600; color: #fff; overflow: hidden;
  position: relative; cursor: pointer;
}
.avatar-preview:hover .avatar-overlay { opacity: 1; }
.avatar-overlay {
  position: absolute; inset: 0; border-radius: 50%;
  background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 18px; opacity: 0; transition: opacity 0.2s;
}
.avatar-placeholder { font-size: 20px; font-weight: 600; color: #fff; }
.avatar-actions { display: flex; flex-direction: column; gap: 6px; }
.avatar-divider { font-size: 11px; color: rgba(255,255,255,0.48); text-align: center; }

/* ===== 新增分类弹窗暗色覆盖 ===== */
:deep(.el-dialog) {
  background: rgba(36,46,37,0.98) !important;
  border: 1px solid rgba(255,255,255,0.14) !important;
  border-radius: 8px !important;
  backdrop-filter: blur(20px);
}
:deep(.el-dialog__title) { color: rgba(255,255,255,0.95) !important; font-size: 15px !important; }
:deep(.el-dialog__header) { border-bottom: 1px solid rgba(255,255,255,0.10) !important; padding-bottom: 14px !important; }
:deep(.el-dialog__body) { color: rgba(255,255,255,0.80) !important; }
:deep(.el-dialog__footer) {
  border-top: 1px solid rgba(255,255,255,0.10) !important;
  padding-top: 14px !important;
  display: flex; align-items: center;
}
:deep(.el-form-item__label) { color: rgba(255,255,255,0.70) !important; font-size: 12px !important; }
:deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.07) !important;
  border: 1px solid rgba(255,255,255,0.18) !important;
  box-shadow: none !important;
}
:deep(.el-input__wrapper.is-focus) {
  border-color: rgba(var(--accent-rgb),0.65) !important;
  box-shadow: none !important;
}
:deep(.el-input__inner) {
  color: var(--text) !important;
  background: transparent !important;
}
:deep(.el-select__wrapper) {
  background: rgba(255,255,255,0.07) !important;
  border: 1px solid rgba(255,255,255,0.18) !important;
  box-shadow: none !important;
  color: rgba(255,255,255,0.95) !important;
}
:deep(.el-overlay) { z-index: 3000 !important; }
:deep(.el-dialog__wrapper) { z-index: 3001 !important; }

/* ===================================================
   个人资料全屏面板（迁移到 SettingsDrawer 的插槽）
   ================================================= */

/* 过渡动画 */
.profile-fade-enter-active,
.profile-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s cubic-bezier(0.22,1,0.36,1);
}
.profile-fade-enter-from,
.profile-fade-leave-to {
  opacity: 0;
  transform: scale(0.98) translateY(8px);
}

/* 遮罩层（插槽中外层 SettingsDrawer 已处理） */

/* 主面板样式保留以匹配插槽内部结构 */
.profile-panel-slot {
  position: relative;
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  background: transparent;
  overflow: visible;
  max-height: none;
  min-height: 0;
}

/* 背景光晕装饰 */
.profile-bg-glow {
  position: absolute;
  top: -120px; left: -80px;
  width: 500px; height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(var(--accent-rgb),0.08) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

/* 顶部导航栏 */
.profile-topbar {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 48px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}
.profile-topbar-logo {
  display: flex; align-items: center; gap: 10px;
}
.nav-logo-mark {
  width: 28px; height: 28px;
  background: var(--surface2);
  border: 1px solid var(--border);
  border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; font-weight: 800; color: var(--text-h); letter-spacing: 0.5px;
}
.nav-logo-text {
  font-size: 14px; font-weight: 600;
  color: var(--text-h); letter-spacing: 0.3px;
}
.profile-close-btn {
  background: none; border: none;
  color: var(--text-dim);
  font-size: 16px; cursor: pointer;
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 6px;
  transition: color 0.2s, background 0.2s;
}
.profile-close-btn:hover {
  color: var(--text-h);
  background: var(--surface2);
}

/* 主体区域 */
.profile-body {
  position: relative;
  z-index: 10;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 52px 48px 36px;
  gap: 52px;
  max-width: 860px;
  margin: 0 auto;
  width: 100%;
  min-height: 0;
}

/* 左侧 Hero */
.profile-hero {
  flex: 1;
}
.profile-hero-label {
  font-size: 11px; font-weight: 600;
  letter-spacing: 2.5px;
  color: var(--text-dim);
  margin-bottom: 16px;
  text-transform: uppercase;
}
.profile-hero-title {
  font-size: clamp(32px,4vw,48px);
  font-weight: 300;
  line-height: 1.15;
  color: var(--text-h);
  margin: 0 0 16px 0;
  letter-spacing: -1px;
}
.profile-hero-desc {
  font-size: 13px; line-height: 1.8;
  color: var(--text-muted);
  margin-bottom: 32px;
}

/* 头像展示（左侧大头像） */
.profile-avatar-display {
  position: relative;
  width: 88px; height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(var(--accent-rgb),0.55), rgba(30,56,34,0.9));
  border: 2px solid var(--border);
  display: flex; align-items: center; justify-content: center;
  overflow: hidden;
  cursor: pointer;
  margin-bottom: 12px;
  transition: border-color 0.2s;
}
.profile-avatar-display:hover { border-color: rgba(var(--accent-rgb),0.50); }
.profile-avatar-display:hover .profile-avatar-overlay { opacity: 1; }
.profile-avatar-img {
  width: 100%; height: 100%; object-fit: cover; border-radius: 50%;
}
.profile-avatar-char {
  font-size: 32px; font-weight: 700; color: #fff;
}
.profile-avatar-overlay {
  position: absolute; inset: 0; border-radius: 50%;
  background: rgba(0,0,0,0.52);
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 4px;
  color: #fff; font-size: 11px; font-weight: 500;
  opacity: 0; transition: opacity 0.2s;
  letter-spacing: 0.3px;
}
.profile-avatar-hint {
  font-size: 11px; color: var(--text-dim);
  line-height: 1.6; max-width: 200px;
}

/* 右侧表单卡片 */
.profile-card {
  width: 340px;
  flex-shrink: 0;
  background: var(--surface2);
  border: 1px solid var(--border);
  border-radius: 4px;
  padding: 32px 28px 24px;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.profile-field {
  margin-bottom: 22px;
}
.profile-field-label {
  font-size: 11px; font-weight: 600;
  letter-spacing: 1.5px;
  color: var(--text-dim);
  margin-bottom: 8px;
  text-transform: uppercase;
}
.profile-input {
  width: 100%;
  background: transparent;
  border: none;
  border-bottom: 1px solid var(--border);
  border-radius: 0;
  padding: 6px 0;
  font-size: 14px;
  font-family: 'Inter', 'PingFang SC', system-ui, sans-serif;
  color: var(--text);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
  caret-color: rgba(var(--accent-rgb),0.9);
}
.profile-input::placeholder { color: var(--text-dim); }
.profile-input:hover { border-bottom-color: var(--border-active); }
.profile-input:focus { border-bottom-color: rgba(var(--accent-rgb),0.80); }

/* 保存按钮 */
.profile-save-btn {
  width: 100%;
  margin-top: 8px;
  padding: 13px 0;
  background: rgba(var(--accent-rgb),0.14);
  border: 1px solid rgba(var(--accent-rgb),0.38);
  border-radius: 3px;
  color: rgba(var(--accent-rgb),0.95);
  font-size: 14px; font-weight: 600;
  letter-spacing: 0.8px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.profile-save-btn:hover:not(:disabled) {
  background: rgba(var(--accent-rgb),0.24);
  border-color: rgba(var(--accent-rgb),0.62);
  color: #fff;
}
.profile-save-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.loading-dot { letter-spacing: 3px; }

/* 底部链接行 */
.profile-footer-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}
.profile-cancel-link,
.profile-logout-link {
  background: none; border: none;
  font-size: 12px; cursor: pointer;
  font-family: inherit;
  transition: color 0.2s;
  padding: 0;
}
.profile-cancel-link {
  color: var(--text-dim);
}
.profile-cancel-link:hover { color: var(--text-muted); }
.profile-logout-link {
  color: rgba(255,120,100,0.60);
}
.profile-logout-link:hover { color: rgba(255,120,100,1); }

/* 底部页脚 */
.profile-footer {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 48px;
  border-top: 1px solid var(--border);
  font-size: 11px;
  color: var(--text-dim);
  letter-spacing: 0.5px;
  flex-shrink: 0;
}
</style>

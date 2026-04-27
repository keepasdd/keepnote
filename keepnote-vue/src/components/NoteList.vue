<template>
  <div class="note-list-panel">
    <!-- 头部 -->
    <div class="list-header">
      <div>
        <div class="list-title">{{ title }}</div>
        <div class="list-subtitle">共 {{ total }} 篇</div>
      </div>
      <div class="header-actions">
        <el-button
          class="btn-pin"
          :disabled="!activeNoteId"
          @click="handlePin"
        >
          <el-icon><Top /></el-icon> {{ isSelectedPinned ? '取消置顶' : '置顶' }}
        </el-button>
        <el-button class="btn-new-tag" @click="openTagDialog(null)">
          <el-icon><PriceTag /></el-icon> 新建标签
        </el-button>
        <el-button
            v-if="currentCategory"
            class="btn-delete-category"
            @click="openDeleteCategoryDialog"
        >
          <el-icon><Delete /></el-icon> 删除分类
        </el-button>
        <el-button type="primary" class="btn-new" @click="emit('new-note')">
          <el-icon><Plus /></el-icon> 新建笔记
        </el-button>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="dateFilter" size="small" @change="onFilterChange">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="today">今天</el-radio-button>
        <el-radio-button value="week">本周</el-radio-button>
        <el-radio-button value="month">本月</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 多级标签筛选区 -->
    <div class="tags-tree" v-if="tags.length">
      <template v-for="tag in tags" :key="tag.id">
        <!-- 父标签行 -->
        <div class="tag-row-wrap">
          <span
              class="tag-pill"
              :class="{ active: activeTagId === tag.id }"
              :style="tagPillStyle(tag)"
              @click="toggleTag(tag.id)"
          >
            <!-- 展开/折叠箭头（仅有子标签时显示） -->
            <span
                v-if="tag.children && tag.children.length"
                class="expand-arrow"
                :class="{ expanded: expandedTags.has(tag.id) }"
                @click.stop="toggleExpand(tag.id)"
            >▶</span>
            <span class="tag-dot" :style="tagDotStyle(tag, activeTagId === tag.id)"></span>
            {{ tag.name }}
          </span>
          <!-- 操作按钮 -->
          <span class="tag-actions">
            <el-tooltip content="添加子标签" placement="top">
              <el-icon class="tag-action-icon" @click.stop="openTagDialog(tag.id)"><Plus /></el-icon>
            </el-tooltip>
            <el-tooltip content="编辑" placement="top">
              <el-icon class="tag-action-icon" @click.stop="openEditDialog(tag)"><Edit /></el-icon>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-icon class="tag-action-icon danger" @click.stop="removeTag(tag)"><Delete /></el-icon>
            </el-tooltip>
          </span>
        </div>

        <!-- 子标签（折叠/展开） -->
        <template v-if="tag.children && tag.children.length && expandedTags.has(tag.id)">
          <div
              v-for="child in tag.children"
              :key="child.id"
              class="tag-row-wrap child-row"
          >
            <span
                class="tag-pill child-pill"
                :class="{ active: activeTagId === child.id }"
                :style="tagPillStyle(child)"
                @click="toggleTag(child.id)"
            >
              <span class="tag-dot" :style="tagDotStyle(child, activeTagId === child.id)"></span>
              {{ child.name }}
            </span>
            <span class="tag-actions">
              <el-tooltip content="编辑" placement="top">
                <el-icon class="tag-action-icon" @click.stop="openEditDialog(child)"><Edit /></el-icon>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-icon class="tag-action-icon danger" @click.stop="removeTag(child)"><Delete /></el-icon>
              </el-tooltip>
            </span>
          </div>
        </template>
      </template>
    </div>

    <!-- 列表 -->
    <div class="list-body" v-loading="loading">
      <div v-if="notes.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无笔记" />
      </div>

      <div
          v-for="note in notes" :key="note.id"
          class="note-card"
          :class="{ active: activeNoteId === note.id }"
          @click="emit('select', note.id)"
          @dblclick="emit('open-note', note.id)"
      >
        <div class="card-header">
          <div class="card-title">
            <span v-if="note.isPinned" class="pin-icon" title="已置顶">📌</span>
            {{ note.title }}
          </div>
          <div class="card-date">{{ note.updatedAt }}</div>
        </div>
        <div class="card-preview">{{ note.content?.replace(/<[^>]+>/g, '').slice(0, 80) }}…</div>
        <div class="card-footer">
          <span
              v-for="tag in note.tags" :key="tag.id"
              class="note-tag"
              :style="{ background: tag.color + '18', color: tag.color }"
          >{{ tag.name }}</span>
          <span class="note-category" v-if="note.categoryName">
            <span class="dot" :style="{ background: note.categoryColor }" />
            {{ note.categoryName }}
          </span>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          small
          @current-change="onPageChange"
      />
    </div>
  </div>

  <!-- 新建标签弹窗 -->
  <el-dialog v-model="tagDialogVisible" title="新建标签" width="360px" align-center>
    <el-form :model="tagForm" label-width="70px" @submit.prevent>
      <el-form-item label="父标签">
        <el-select
            v-model="tagForm.parentId"
            placeholder="不选则为顶级标签"
            clearable
            style="width:100%"
        >
          <el-option
              v-for="t in flatTags"
              :key="t.id"
              :label="t._label"
              :value="t.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="tagForm.name" placeholder="请输入标签名称" maxlength="20" show-word-limit />
      </el-form-item>
      <el-form-item label="颜色">
        <div class="color-options">
          <span
              v-for="c in colorPresets" :key="c"
              class="color-dot"
              :style="{ background: c, outline: tagForm.color === c ? `2px solid ${c}` : 'none' }"
              @click="tagForm.color = c"
          />
          <el-color-picker v-model="tagForm.color" size="small" />
        </div>
      </el-form-item>
      <el-form-item label="预览">
        <span
            class="tag-pill"
            :style="{ color: tagForm.color, background: tagForm.color + '22', borderColor: tagForm.color }"
        >
          <span class="tag-dot" :style="{ background: tagForm.color, borderColor: tagForm.color }"></span>
          {{ tagForm.name || '标签名称' }}
        </span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="tagDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="tagSaving" @click="submitTag">确定</el-button>
    </template>
  </el-dialog>

  <!-- 编辑标签弹窗 -->
  <el-dialog v-model="editDialogVisible" title="编辑标签" width="360px" align-center>
    <el-form :model="editForm" label-width="60px" @submit.prevent>
      <el-form-item label="名称">
        <el-input v-model="editForm.name" placeholder="请输入标签名称" maxlength="20" show-word-limit />
      </el-form-item>
      <el-form-item label="颜色">
        <div class="color-options">
          <span
              v-for="c in colorPresets" :key="c"
              class="color-dot"
              :style="{ background: c, outline: editForm.color === c ? `2px solid ${c}` : 'none' }"
              @click="editForm.color = c"
          />
          <el-color-picker v-model="editForm.color" size="small" />
        </div>
      </el-form-item>
      <el-form-item label="预览">
        <span
            class="tag-pill"
            :style="{ color: editForm.color, background: editForm.color + '22', borderColor: editForm.color }"
        >
          <span class="tag-dot" :style="{ background: editForm.color, borderColor: editForm.color }"></span>
          {{ editForm.name || '标签名称' }}
        </span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="editSaving" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="deleteCategoryDialogVisible" title="删除分类" width="420px" align-center>
    <div class="delete-category-tip">
      删除分类「{{ currentCategory?.name }}」前，请先选择该分类下笔记的新去向。
    </div>
    <el-form label-width="88px" @submit.prevent>
      <el-form-item label="处理方式">
        <el-radio-group v-model="deleteCategoryForm.mode">
          <el-radio :disabled="!availableTargetCategories.length" value="existing">转移到已有分类</el-radio>
          <el-radio value="new">新建分类后转移</el-radio>
        </el-radio-group>
      </el-form-item>

      <template v-if="deleteCategoryForm.mode === 'existing'">
        <el-form-item label="目标分类">
          <el-select
              v-model="deleteCategoryForm.targetCategoryId"
              placeholder="请选择目标分类"
              style="width: 100%"
          >
            <el-option
                v-for="category in availableTargetCategories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
            />
          </el-select>
        </el-form-item>
      </template>

      <template v-else>
        <el-form-item label="分类名称">
          <el-input
              v-model="deleteCategoryForm.newCategoryName"
              maxlength="20"
              show-word-limit
              placeholder="请输入新分类名称"
          />
        </el-form-item>
        <el-form-item label="分类颜色">
          <div class="color-options">
            <span
                v-for="c in categoryColorPresets" :key="c"
                class="color-dot"
                :style="{ background: c, outline: deleteCategoryForm.newCategoryColor === c ? `2px solid ${c}` : 'none' }"
                @click="deleteCategoryForm.newCategoryColor = c"
            />
            <el-color-picker v-model="deleteCategoryForm.newCategoryColor" size="small" />
          </div>
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <el-button @click="deleteCategoryDialogVisible = false">取消</el-button>
      <el-button type="danger" :loading="deleteCategorySaving" @click="submitDeleteCategory">
        确认删除
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, PriceTag, Edit, Delete, Top } from '@element-plus/icons-vue'
import { addTag, updateTag, deleteTag } from '../api/tag'
import { addCategory, deleteCategory, getCategoryList } from '../api/category'
import { getNoteList, updateNote, pinNote } from '../api/note'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const props = defineProps({
  notes: { type: Array, default: () => [] },
  tags: { type: Array, default: () => [] },  // 树形结构（含 children）
  categories: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  pageSize: { type: Number, default: 20 },
  loading: { type: Boolean, default: false },
  activeNoteId: { type: Number, default: null },
  activeCategoryId: { type: Number, default: null },
  title: { type: String, default: '全部笔记' },
})
const emit = defineEmits(['select', 'new-note', 'filter-change', 'tag-added', 'category-deleted', 'open-note', 'pin-note'])

const dateFilter = ref('')
const activeTagId = ref(null)
const currentPage = ref(1)
const pinLoading = ref(false)

const isSelectedPinned = computed(() => {
  const note = props.notes.find(n => n.id === props.activeNoteId)
  return !!(note && note.isPinned)
})

async function handlePin() {
  if (!props.activeNoteId) return
  pinLoading.value = true
  try {
    emit('pin-note', props.activeNoteId)
  } finally {
    pinLoading.value = false
  }
}

// ---- 展开/折叠 ----
const expandedTags = ref(new Set())

function toggleExpand(id) {
  if (expandedTags.value.has(id)) {
    expandedTags.value.delete(id)
  } else {
    expandedTags.value.add(id)
  }
}

// ---- 标签样式辅助 ----
function tagPillStyle(tag) {
  const isActive = activeTagId.value === tag.id
  return {
    color: tag.color,
    background: isActive ? tag.color + '22' : 'transparent',
    borderColor: tag.color,
  }
}

function tagDotStyle(tag, isActive) {
  return {
    background: isActive ? tag.color : 'transparent',
    borderColor: tag.color,
  }
}

// ---- 平铺标签列表（用于父标签选择下拉）----
const flatTags = computed(() => {
  const result = []
  for (const tag of props.tags) {
    result.push({ ...tag, _label: tag.name })
    if (tag.children) {
      for (const child of tag.children) {
        result.push({ ...child, _label: `└ ${child.name}` })
      }
    }
  }
  return result
})

const currentCategory = computed(() =>
    props.categories.find(category => category.id === props.activeCategoryId) || null,
)

const availableTargetCategories = computed(() =>
    props.categories.filter(category => category.id !== props.activeCategoryId),
)

// ---- 新建标签 ----
const tagDialogVisible = ref(false)
const tagSaving = ref(false)
const colorPresets = ['#3a7d3f', '#e67e22', '#2980b9', '#8e44ad', '#e74c3c', '#16a085', '#f39c12', '#7f8c8d']
const tagForm = ref({ name: '', color: '#3a7d3f', parentId: null })
const categoryColorPresets = ['#4ecdc4', '#3a7d3f', '#ff6b6b', '#ffe66d', '#a78bfa', '#fb923c', '#2980b9', '#7f8c8d']

function openTagDialog(parentId = null) {
  tagForm.value = { name: '', color: '#3a7d3f', parentId }
  tagDialogVisible.value = true
}

async function submitTag() {
  if (!tagForm.value.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  tagSaving.value = true
  try {
    await addTag({
      name: tagForm.value.name.trim(),
      color: tagForm.value.color,
      parentId: tagForm.value.parentId || null,
    })
    ElMessage.success('标签创建成功')
    tagDialogVisible.value = false
    emit('tag-added')
  } catch {
    ElMessage.error('创建失败，请重试')
  } finally {
    tagSaving.value = false
  }
}

// ---- 编辑标签 ----
const editDialogVisible = ref(false)
const editSaving = ref(false)
const editForm = ref({ id: null, name: '', color: '#3a7d3f' })

function openEditDialog(tag) {
  editForm.value = { id: tag.id, name: tag.name, color: tag.color }
  editDialogVisible.value = true
}

async function submitEdit() {
  if (!editForm.value.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  editSaving.value = true
  try {
    await updateTag({
      id: editForm.value.id,
      name: editForm.value.name.trim(),
      color: editForm.value.color,
    })
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    emit('tag-added')  // 刷新标签列表
  } catch {
    ElMessage.error('修改失败，请重试')
  } finally {
    editSaving.value = false
  }
}

// ---- 删除标签 ----
async function removeTag(tag) {
  const hasChildren = tag.children && tag.children.length > 0
  const msg = hasChildren
      ? `确定删除标签「${tag.name}」及其 ${tag.children.length} 个子标签吗？`
      : `确定删除标签「${tag.name}」吗？`
  try {
    await ElMessageBox.confirm(msg, '删除标签', { type: 'warning' })
    await deleteTag(tag.id)
    // 若删除的是当前筛选标签，取消筛选
    if (activeTagId.value === tag.id || tag.children?.some(c => c.id === activeTagId.value)) {
      activeTagId.value = null
      emitFilter()
    }
    ElMessage.success('已删除')
    emit('tag-added')  // 刷新列表
  } catch {
    // 用户取消，忽略
  }
}

const deleteCategoryDialogVisible = ref(false)
const deleteCategorySaving = ref(false)
const deleteCategoryForm = ref({
  mode: 'existing',
  targetCategoryId: null,
  newCategoryName: '',
  newCategoryColor: '#4ecdc4',
})

function resetDeleteCategoryForm() {
  deleteCategoryForm.value = {
    mode: availableTargetCategories.value.length ? 'existing' : 'new',
    targetCategoryId: availableTargetCategories.value[0]?.id ?? null,
    newCategoryName: '',
    newCategoryColor: '#4ecdc4',
  }
}

async function fetchCategoryNotes(categoryId) {
  const pageSize = 100
  let page = 1
  let allNotes = []
  let total = 0

  do {
    const data = await getNoteList({ page, pageSize, categoryId })
    allNotes = allNotes.concat(data.records || [])
    total = data.total || 0
    page += 1
  } while (allNotes.length < total)

  return allNotes
}

async function deleteCategoryOnly(category) {
  deleteCategorySaving.value = true
  try {
    await deleteCategory(category.id)
    ElMessage.success(`分类「${category.name}」已删除`)
    deleteCategoryDialogVisible.value = false
    emit('category-deleted', {
      deletedCategoryId: category.id,
      targetCategoryId: null,
    })
  } catch {
    ElMessage.error('删除分类失败，请重试')
  } finally {
    deleteCategorySaving.value = false
  }
}

async function openDeleteCategoryDialog() {
  const category = currentCategory.value
  if (!category) return

  const noteCount = typeof category.noteCount === 'number'
      ? category.noteCount
      : (await getNoteList({ page: 1, pageSize: 1, categoryId: category.id })).total || 0

  if (noteCount === 0) {
    try {
      await ElMessageBox.confirm(
          `分类「${category.name}」下暂无笔记，确定直接删除吗？`,
          '删除分类',
          { type: 'warning' },
      )
      await deleteCategoryOnly(category)
    } catch {
      // 用户取消，忽略
    }
    return
  }

  resetDeleteCategoryForm()
  deleteCategoryDialogVisible.value = true
}

async function submitDeleteCategory() {
  const category = currentCategory.value
  if (!category) return

  let targetCategoryId = deleteCategoryForm.value.targetCategoryId

  if (deleteCategoryForm.value.mode === 'existing') {
    if (!targetCategoryId) {
      ElMessage.warning('请选择目标分类')
      return
    }
  } else {
    const name = deleteCategoryForm.value.newCategoryName.trim()
    if (!name) {
      ElMessage.warning('请输入新分类名称')
      return
    }
    deleteCategorySaving.value = true
    try {
      const created = await addCategory({
        name,
        color: deleteCategoryForm.value.newCategoryColor,
      })
      targetCategoryId = created?.id
      if (!targetCategoryId) {
        const latestCategories = await getCategoryList()
        const matchedCategory = latestCategories
            .filter(item => item.id !== category.id)
            .find(item => item.name === name && item.color === deleteCategoryForm.value.newCategoryColor)
        targetCategoryId = matchedCategory?.id ?? null
      }
      if (!targetCategoryId) {
        throw new Error('新分类创建失败')
      }
    } catch {
      ElMessage.error('新分类创建失败，请重试')
      deleteCategorySaving.value = false
      return
    }
  }

  deleteCategorySaving.value = true
  try {
    const notesInCategory = await fetchCategoryNotes(category.id)
    await Promise.all(
        notesInCategory.map(note => updateNote({ id: note.id, categoryId: targetCategoryId })),
    )
    await deleteCategory(category.id)
    ElMessage.success(`分类「${category.name}」已删除，笔记已转移`)
    deleteCategoryDialogVisible.value = false
    emit('category-deleted', {
      deletedCategoryId: category.id,
      targetCategoryId,
    })
  } catch {
    ElMessage.error('删除分类失败，请重试')
  } finally {
    deleteCategorySaving.value = false
  }
}

// ---- 筛选 ----
function toggleTag(id) {
  activeTagId.value = activeTagId.value === id ? null : id
  emitFilter()
}

function onFilterChange() {
  currentPage.value = 1
  emitFilter()
}

function onPageChange(page) {
  currentPage.value = page
  emitFilter()
}

function emitFilter() {
  emit('filter-change', {
    page: currentPage.value,
    dateRange: dateFilter.value,
    tagId: activeTagId.value,
  })
}
</script>

<style scoped>
/* ===== 面板基础 ===== */
.note-list-panel {
  display: flex; flex-direction: column;
  height: 100vh; overflow: hidden;
  border-right: 1px solid var(--border);
  background: var(--bg);
  font-family: 'Inter', 'PingFang SC', system-ui, sans-serif;
}

/* ===== 头部 ===== */
.list-header {
  padding: 26px 22px 0;
  display: flex; align-items: flex-start;
  justify-content: space-between; gap: 12px;
  flex-shrink: 0;
}

.list-title {
  font-size: 17px; font-weight: 600;
  color: var(--text);
  letter-spacing: -0.2px;
}
.list-subtitle {
  font-size: 11px; color: var(--text-muted);
  margin-top: 3px; font-family: var(--font-mono);
}

.header-actions { display: flex; gap: 8px; align-items: center; flex-shrink: 0; }

/* 新建标签按钮 */
.btn-new-tag {
  background: var(--surface2) !important;
  border: 1px solid var(--border) !important;
  color: var(--text-muted) !important;
  font-size: 12px !important; font-weight: 500 !important;
  border-radius: 5px !important;
  transition: all 0.2s !important;
}
.btn-new-tag:hover {
  border-color: var(--border-active) !important;
  color: var(--accent) !important;
  background: var(--surface3) !important;
}

/* 置顶按钮 */
.btn-pin {
  background: var(--surface2) !important;
  border: 1px solid var(--border) !important;
  color: var(--text-muted) !important;
  font-size: 12px !important; font-weight: 500 !important;
  border-radius: 5px !important;
  transition: all 0.2s !important;
}
.btn-pin:not(:disabled):hover {
  border-color: var(--border-active) !important;
  color: var(--accent) !important;
  background: var(--surface3) !important;
}
.btn-pin:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

/* 笔记卡片置顶图标 */
.pin-icon {
  font-size: 13px;
  margin-right: 3px;
  flex-shrink: 0;
  line-height: 1;
}

.btn-delete-category {
  background: var(--surface2) !important;
  border: 1px solid rgba(231,76,60,0.35) !important;
  color: rgba(231,76,60,0.95) !important;
  font-size: 12px !important;
  font-weight: 500 !important;
  border-radius: 5px !important;
  transition: all 0.2s !important;
}
.btn-delete-category:hover {
  border-color: rgba(231,76,60,0.60) !important;
  color: rgba(231,76,60,1) !important;
  background: var(--surface3) !important;
}

/* 新建笔记按钮 */
.btn-new {
  background: var(--surface2) !important;
  border: 1px solid rgba(var(--accent-rgb),0.45) !important;
  color: var(--accent) !important;
  font-size: 12px !important; font-weight: 600 !important;
  border-radius: 5px !important;
  box-shadow: none !important;
  transition: all 0.2s !important;
}
.btn-new:hover {
  background: var(--surface3) !important;
  border-color: rgba(var(--accent-rgb),0.70) !important;
  color: var(--text) !important;
}

/* ===== 颜色点 ===== */
.color-options { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.color-dot {
  width: 20px; height: 20px; border-radius: 50%; cursor: pointer;
  transition: transform 0.15s; outline-offset: 3px;
}
.color-dot:hover { transform: scale(1.2); }

/* ===== 筛选栏 ===== */
.filter-bar { padding: 14px 22px 8px; flex-shrink: 0; }

:deep(.filter-bar .el-radio-button__inner) {
  background: var(--surface2) !important;
  border-color: var(--border) !important;
  color: var(--text-dim) !important;
  font-size: 12px !important;
  transition: all 0.15s !important;
}
:deep(.filter-bar .el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: rgba(var(--accent-rgb),0.20) !important;
  border-color: rgba(var(--accent-rgb),0.50) !important;
  color: rgba(var(--accent-rgb),0.98) !important;
  box-shadow: none !important;
}
:deep(.filter-bar .el-radio-button__inner:hover) {
  color: var(--text) !important;
}

/* ===== 多级标签树 ===== */
.tags-tree {
  padding: 0 14px 10px;
  display: flex; flex-direction: column; gap: 3px;
  flex-shrink: 0; max-height: 200px; overflow-y: auto;
}
.tags-tree::-webkit-scrollbar { width: 3px; }
.tags-tree::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.15); border-radius: 3px; }

.tag-row-wrap {
  display: flex; align-items: center; gap: 4px;
}
.tag-row-wrap:hover .tag-actions { opacity: 1; }

.child-row { padding-left: 18px; }

.tag-pill {
  padding: 3px 10px 3px 7px; border-radius: 20px; font-size: 11px;
  white-space: nowrap; cursor: pointer; border: 1.5px solid;
  display: inline-flex; align-items: center; gap: 5px;
  transition: background 0.15s, box-shadow 0.15s;
  font-weight: 500; flex-shrink: 0;
}
.child-pill { font-size: 10.5px; padding: 2px 9px 2px 6px; }
.tag-pill:hover { box-shadow: 0 1px 6px rgba(0,0,0,0.3); }
.tag-pill.active { box-shadow: 0 2px 8px rgba(0,0,0,0.30); }

.tag-dot {
  width: 8px; height: 8px; border-radius: 50%;
  border: 1.5px solid; flex-shrink: 0;
  transition: background 0.15s;
}

.expand-arrow {
  font-size: 8px; display: inline-block;
  transition: transform 0.2s; cursor: pointer;
  transform: rotate(0deg); line-height: 1;
  color: var(--text-dim);
}
.expand-arrow.expanded { transform: rotate(90deg); }

.tag-actions {
  display: flex; gap: 2px; opacity: 1;
  transition: opacity 0.15s;
}
.tag-action-icon {
  font-size: 13px; cursor: pointer; color: var(--text-dim);
  padding: 3px; border-radius: 4px; transition: color 0.15s, background 0.15s;
}
.tag-action-icon:hover { color: rgba(200,230,150,0.95); background: rgba(180,210,130,0.12); }
.tag-action-icon.danger:hover { color: rgba(255,100,80,0.95); background: rgba(231,76,60,0.12); }

/* ===== 笔记列表体 ===== */
.list-body { flex: 1; overflow-y: auto; padding: 0 14px 14px; position: relative; }
.list-body::-webkit-scrollbar { width: 3px; }
.list-body::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.15); border-radius: 3px; }

.empty-state { display: flex; align-items: center; justify-content: center; height: 200px; }

/* ===== 笔记卡片 ===== */
.note-card {
  background: var(--surface2);
  border: 1px solid var(--border);
  border-radius: 8px; padding: 14px; margin-bottom: 6px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s, transform 0.15s;
  position: relative; overflow: hidden;
}
.note-card::before {
  content: ''; position: absolute; top: 0; left: 0;
  width: 3px; height: 100%; background: transparent; transition: background 0.2s;
}
.note-card:hover {
  border-color: var(--border-active);
  background: var(--surface3);
  transform: translateX(2px);
}
.note-card.active {
  border-color: rgba(var(--accent-rgb),0.40);
  background: var(--surface2);
}
.note-card.active::before { background: rgba(180,210,130,0.85); }

.card-header {
  display: flex; align-items: flex-start;
  justify-content: space-between; gap: 8px; margin-bottom: 6px;
}
.card-title {
  font-size: 13.5px; font-weight: 500;
  color: var(--text);
  flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.card-date {
  font-family: var(--font-mono); font-size: 10px;
  color: var(--text-dim); white-space: nowrap;
}
.card-preview {
  font-size: 12px; color: var(--text-muted); line-height: 1.6;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; margin-bottom: 10px;
}
.card-footer { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }

.note-tag { font-size: 10px; padding: 2px 8px; border-radius: 4px; }
.note-category {
  margin-left: auto; font-size: 10px; color: var(--text-dim);
  display: flex; align-items: center; gap: 4px;
}
.dot { width: 6px; height: 6px; border-radius: 50%; display: inline-block; }

.delete-category-tip {
  margin-bottom: 16px;
  line-height: 1.7;
  color: var(--text-muted);
  font-size: 13px;
}

/* ===== 分页 ===== */
.pagination {
  padding: 10px 16px;
  border-top: 1px solid var(--border);
  display: flex; justify-content: center; flex-shrink: 0;
}

:deep(.pagination .el-pagination button),
:deep(.pagination .el-pager li) {
  background: transparent !important;
  color: var(--text-dim) !important;
  border: none !important;
}
:deep(.pagination .el-pager li.is-active) {
  color: var(--text) !important;
  background: rgba(var(--accent-rgb),0.16) !important;
  border-radius: 4px !important;
}
:deep(.pagination .el-pager li:hover) {
  color: var(--text) !important;
}
</style>

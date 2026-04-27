<template>
  <div class="detail-panel">
    <!-- 空状态 -->
    <div v-if="!note && !isEditing" class="empty-detail">
      <div class="empty-icon">
        <el-icon size="24"><Document /></el-icon>
      </div>
      <div class="empty-title">选择一篇笔记</div>
      <div class="empty-sub">从左侧列表中点击查看详情</div>
    </div>

    <!-- 编辑/新建模式 -->
    <template v-else-if="isEditing">
      <div class="detail-toolbar">
        <span class="note-id" style="color:var(--accent)">● {{ editForm.id ? '编辑笔记' : '新建笔记' }}</span>
        <div class="actions">
          <el-button size="small" @click="cancelEdit">取消</el-button>
          <el-button size="small" type="primary" :loading="saving" @click="save">保存</el-button>
        </div>
      </div>
      <div class="detail-meta">
        <input v-model="editForm.title" class="title-input" placeholder="笔记标题…" autofocus />
        <div class="meta-row">
          <el-select v-model="editForm.categoryId" placeholder="选择分类" size="small" clearable style="width:120px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-select v-model="editForm.tagIds" placeholder="添加标签" size="small" multiple clearable style="width:180px">
            <el-option
              v-for="t in flatTags"
              :key="t.id"
              :label="t._label"
              :value="t.id"
            />
          </el-select>
        </div>
      </div>
      <div class="detail-divider" />
      <div class="detail-body">
        <textarea v-model="editForm.content" class="content-editor" placeholder="开始记录你的想法…" @paste="handlePaste" />

        <!-- 文件上传区域 -->
        <div class="attachment-area">
          <el-upload
            class="upload-demo" drag
            :http-request="noopUpload"
            :before-upload="beforeUpload"
            :on-change="handleChange"
            :file-list="uploadList"
            :auto-upload="false"
            multiple list-type="text"
          >
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <div class="el-upload__tip">支持单个文件最大 10MB，支持图片 / PDF / 文本</div>
          </el-upload>
          <div style="margin-top:8px; display:flex; gap:8px;">
            <el-button size="small" type="primary" @click="submitUploads" :loading="uploading" :disabled="uploadList.length===0">上传</el-button>
            <el-button size="small" @click="clearUploads">清除</el-button>
          </div>
        </div>
      </div>
    </template>

    <!-- 查看模式 -->
    <template v-else>
      <div class="detail-toolbar">
        <div class="actions">
          <el-tooltip content="收藏">
            <el-button :icon="note.isFavorite ? StarFilled : Star" circle size="small" @click="toggleFavorite" />
          </el-tooltip>
          <input ref="viewerFileInput" type="file" style="display:none" multiple @change="handleFileInput" />
          <el-tooltip content="上传附件">
            <el-button :icon="Upload" circle size="small" @click="triggerFileSelect" />
          </el-tooltip>
          <el-tooltip content="编辑">
            <el-button :icon="Edit" circle size="small" @click="startEdit" />
          </el-tooltip>
          <el-tooltip content="删除">
            <el-button :icon="Delete" circle size="small" type="danger" plain @click="confirmDelete" />
          </el-tooltip>
        </div>
      </div>

      <div class="detail-meta">
        <div class="detail-title">{{ note.title }}</div>
        <div class="info-row">
          <span class="info-item"><el-icon><Calendar /></el-icon> {{ note.updatedAt }}</span>
          <span class="info-item"><el-icon><Document /></el-icon> {{ wordCount }} 字</span>
          <span class="info-item" v-if="note.categoryName">
            <span class="dot" :style="{ background: note.categoryColor }" /> {{ note.categoryName }}
          </span>
        </div>
        <div class="tag-row">
          <span v-for="tag in note.tags" :key="tag.id" class="tag-pill"
            :style="{ background: tag.color + '18', color: tag.color }">{{ tag.name }}</span>
        </div>
      </div>

      <div class="detail-divider" />

      <div class="stats-row">
        <div class="stat"><div class="stat-num">{{ wordCount }}</div><div class="stat-label">字数</div></div>
        <div class="stat"><div class="stat-num">{{ readTime }}</div><div class="stat-label">分钟读</div></div>
        <div class="stat"><div class="stat-num">{{ note.tags?.length || 0 }}</div><div class="stat-label">标签</div></div>
      </div>

      <div class="detail-body">
        <div class="detail-content" v-html="renderedContent" />

        <!-- 附件区域（查看模式） -->
        <div v-if="noteAttachments.length" class="attachments-section">
          <div class="attachments-title">附件 ({{ noteAttachments.length }})</div>
          <!-- 图片网格 -->
          <div v-if="imageAttachments.length" class="image-grid">
            <a v-for="att in imageAttachments" :key="att.id"
               :href="att.url" target="_blank" rel="noopener"
               class="image-thumb-wrap" :title="att.fileName">
              <img :src="att.url" :alt="att.fileName" class="image-thumb" />
              <div class="image-thumb-name">{{ att.fileName }}</div>
            </a>
          </div>
          <!-- 非图片列表 -->
          <div v-if="fileAttachments.length" class="file-list" :style="imageAttachments.length ? 'margin-top:10px' : ''">
            <div v-for="att in fileAttachments" :key="att.id" class="attachment-item">
              <el-icon class="att-icon"><Document /></el-icon>
              <span class="att-name" :title="att.fileName">{{ att.fileName }}</span>
              <span class="att-size">{{ formatSize(att.fileSize) }}</span>
              <el-button :icon="Download" circle size="small" @click="downloadAtt(att)" />
              <el-button :icon="Close" circle size="small" type="danger" plain @click="removeAttachment(att)" />
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, reactive, nextTick } from 'vue'
import { Edit, Delete, Star, StarFilled, Calendar, Document, Upload, Download, Close } from '@element-plus/icons-vue'
import { addNote, updateNote, deleteNote, uploadAttachment, deleteAttachment } from '../api/note'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  note: { type: Object, default: null },
  isEditing: { type: Boolean, default: false },
  categories: { type: Array, default: () => [] },
  tags: { type: Array, default: () => [] },  // 树形结构（含 children）
  defaultCategoryId: { type: Number, default: null },
})
const emit = defineEmits(['deleted', 'saved', 'cancel-edit', 'toggle-favorite'])

const saving = ref(false)
const editForm = reactive({ id: null, title: '', content: '', categoryId: null, tagIds: [] })
const uploadList = ref([])
const uploading = ref(false)
const viewerFileInput = ref(null)

// 将树形标签平铺，供下拉选择器使用，显示层级路径
const flatTags = computed(() => {
  const result = []
  for (const tag of props.tags) {
    result.push({ id: tag.id, _label: tag.name })
    if (tag.children) {
      for (const child of tag.children) {
        result.push({ id: child.id, _label: `${tag.name} / ${child.name}` })
      }
    }
  }
  return result
})

watch(() => props.note, (n) => {
  if (n && props.isEditing) {
    editForm.id = n.id
    editForm.title = n.title
    editForm.content = n.content || ''
    editForm.categoryId = n.categoryId || null
    editForm.tagIds = n.tags?.map(t => t.id) || []
    if (!n.attachments) n.attachments = []
  }
})

watch(() => props.isEditing, (val) => {
  if (val && !props.note) {
    editForm.id = null
    editForm.title = ''
    editForm.content = ''
    editForm.categoryId = props.defaultCategoryId
    editForm.tagIds = []
    uploadList.value = []
  }
})

const wordCount = computed(() => (props.note?.content?.replace(/<[^>]+>/g, '') || '').length)
const readTime = computed(() => Math.max(1, Math.ceil(wordCount.value / 300)))
const renderedContent = computed(() => {
  if (!props.note?.content) return ''
  let content = props.note.content

  content = content.replace(
    /!\[(.*?)\]\(([\s\S]*?)\)/g,
    (_, alt, url) => `<img src="${url.replace(/\s+/g, '')}" alt="${alt}" class="note-image" />`
  )

  content = content.replace(
    /\[(.*?)\]\(([\s\S]*?)\)/g,
    (_, text, url) => `<a href="${url.replace(/\s+/g, '')}" target="_blank" rel="noopener">${text}</a>`
  )

  return content.replace(/\n/g, '<br>')
})

function isImage(fileName) {
  return /\.(png|jpe?g|gif|webp|svg)$/i.test(fileName || '')
}

const noteAttachments = computed(() => props.note?.attachments || [])
const imageAttachments = computed(() => noteAttachments.value.filter(a => isImage(a.fileName)))
const fileAttachments = computed(() => noteAttachments.value.filter(a => !isImage(a.fileName)))

function startEdit() {
  editForm.id = props.note.id
  editForm.title = props.note.title
  editForm.content = props.note.content || ''
  editForm.categoryId = props.note.categoryId || null
  editForm.tagIds = props.note.tags?.map(t => t.id) || []
  emit('saved', { ...props.note, _editMode: true })
}

function cancelEdit() { emit('cancel-edit') }

async function save(options = {}) {
  if (!editForm.title.trim()) return ElMessage.warning('标题不能为空')
  saving.value = true
  try {
    if (editForm.id) {
      await updateNote(editForm)
    } else {
      const id = await addNote(editForm)
      editForm.id = id
    }
    if (!options.silent) {
      ElMessage.success('保存成功')
    }
    emit('saved', { ...editForm, _editMode: options.silent || undefined })
  } finally {
    saving.value = false
  }
}

async function confirmDelete() {
  await ElMessageBox.confirm('确定删除这篇笔记吗？', '提示', { type: 'warning' })
  await deleteNote(props.note.id)
  ElMessage.success('已删除')
  emit('deleted')
}

function toggleFavorite() { emit('toggle-favorite', props.note.id) }

function noopUpload() {}

function getFileExtensionByMime(mimeType) {
  const typeMap = {
    'image/png': 'png',
    'image/jpeg': 'jpg',
    'image/gif': 'gif',
    'image/webp': 'webp',
    'image/svg+xml': 'svg',
  }
  return typeMap[mimeType] || 'png'
}

function createPastedImageFile(blob) {
  const extension = getFileExtensionByMime(blob.type)
  const stamp = new Date().toISOString().replace(/[-:TZ.]/g, '').slice(0, 14)
  return new File([blob], `pasted-image-${stamp}.${extension}`, { type: blob.type || 'image/png' })
}

function appendAttachmentToNote(attachment) {
  if (!props.note) return
  if (!props.note.attachments) props.note.attachments = []
  props.note.attachments.push(attachment)
}

function insertTextAtCursor(textarea, content) {
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const before = editForm.content.substring(0, start)
  const after = editForm.content.substring(end)
  editForm.content = before + content + after
  return start + content.length
}

async function ensureEditingNoteId() {
  if (editForm.id) return editForm.id
  await save({ silent: true })
  return editForm.id
}

function triggerFileSelect() {
  if (!viewerFileInput.value) return
  viewerFileInput.value.value = null
  viewerFileInput.value.click()
}

async function handleFileInput(e) {
  const files = Array.from(e.target.files || [])
  if (!files.length) return
  if (!props.note?.id) { ElMessage.warning('请先保存笔记后再上传附件'); return }
  uploading.value = true
  try {
    for (const f of files) {
      if (!beforeUpload(f)) continue
      const resp = await uploadAttachment(props.note.id, f)
      const attachment = resp?.attachment || resp
      if (!props.note.attachments) props.note.attachments = []
      props.note.attachments.push(attachment)
    }
    ElMessage.success('上传完成')
  } catch (err) {
    ElMessage.error(err.message || '上传失败')
  } finally {
    uploading.value = false
    if (viewerFileInput.value) viewerFileInput.value.value = null
  }
}

function beforeUpload(file) {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) { ElMessage.error('文件太大，最大支持 10MB'); return false }
  if (!file.name.match(/\.(md|txt|pdf|png|jpe?g|gif|webp|svg)$/i)) {
    ElMessage.error('不支持的文件类型'); return false
  }
  return true
}

function handleChange(file, fileList) { uploadList.value = fileList }
function clearUploads() { uploadList.value = [] }

async function submitUploads() {
  if (!props.note?.id) { ElMessage.warning('请先保存笔记后再上传附件'); return }
  if (!uploadList.value.length) return
  uploading.value = true
  try {
    for (const f of uploadList.value) {
      if (f.status === 'success' || f.url) continue
      const resp = await uploadAttachment(props.note.id, f.raw)
      const attachment = resp?.attachment || resp
      if (!props.note.attachments) props.note.attachments = []
      props.note.attachments.push(attachment)
      f.status = 'success'
      f.url = attachment.url
    }
    ElMessage.success('上传完成')
    uploadList.value = []
  } catch (err) {
    ElMessage.error(err.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function handlePaste(e) {
  const items = e.clipboardData?.items
  if (!items) return
  const imageItems = Array.from(items).filter(item => item.type.startsWith('image/'))
  if (!imageItems.length) return

  e.preventDefault()

  let noteId = editForm.id
  if (!noteId) {
    noteId = await ensureEditingNoteId()
    if (!noteId) return
  }

  for (const item of items) {
    if (!item.type.startsWith('image/')) continue
    const blob = item.getAsFile()
    if (!blob) continue
    const file = createPastedImageFile(blob)
    if (!beforeUpload(file)) continue

    try {
      uploading.value = true
      const resp = await uploadAttachment(noteId, file)
      const attachment = resp?.attachment || resp
      appendAttachmentToNote(attachment)
      const imgTag = `![${attachment.fileName || file.name}](${attachment.url})`
      const textarea = e.target
      const cursorPos = insertTextAtCursor(textarea, imgTag)
      await nextTick()
      textarea.setSelectionRange(cursorPos, cursorPos)
      textarea.focus()
      ElMessage.success('图片已上传并插入笔记')
    } catch (err) {
      ElMessage.error('图片上传失败')
    } finally {
      uploading.value = false
    }
  }
}

async function removeAttachment(att) {
  if (!props.note?.id) return
  try {
    await deleteAttachment(att.id)
    props.note.attachments = props.note.attachments.filter(a => a.id !== att.id)
    ElMessage.success('附件已删除')
  } catch (err) {
    ElMessage.error(err.message || '删除失败')
  }
}

function downloadAtt(att) {
  const a = document.createElement('a')
  a.href = att.url
  a.download = att.fileName
  a.click()
}

function formatSize(size) {
  if (!size) return ''
  if (size < 1024) return size + 'B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + 'KB'
  return (size / (1024 * 1024)).toFixed(1) + 'MB'
}
</script>

<style scoped>
/* ===== 面板基础 ===== */
.detail-panel {
  background: var(--bg);
  display: flex; flex-direction: column;
  height: 100vh; overflow: hidden;
  font-family: 'Inter', 'PingFang SC', system-ui, sans-serif;
}

/* ===== 空状态 ===== */
.empty-detail {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  color: var(--text-dim); gap: 12px;
}
.empty-icon {
  width: 48px; height: 48px;
  border: 2px dashed var(--border); border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-dim);
}
.empty-title { font-size: 14px; color: var(--text-muted); }
.empty-sub { font-size: 12px; color: var(--text-dim); }

/* ===== 工具栏 ===== */
.detail-toolbar {
  padding: 18px 22px 0;
  display: flex; align-items: center; justify-content: space-between;
  flex-shrink: 0;
  border-bottom: 1px solid var(--border);
  padding-bottom: 14px;
}
.note-id {
  font-family: var(--font-mono); font-size: 11px;
  color: rgba(180,210,130,0.7);
}
.actions { display: flex; gap: 6px; }

/* ===== 元信息区 ===== */
.detail-meta { padding: 16px 22px 0; flex-shrink: 0; }
.detail-title {
  font-size: 20px; font-weight: 600;
  color: var(--text); line-height: 1.35; margin-bottom: 10px;
  letter-spacing: -0.2px;
}
.title-input {
  width: 100%; background: none; border: none; outline: none;
  font-size: 20px; font-weight: 600;
  color: var(--text); letter-spacing: -0.2px; line-height: 1.35;
  margin-bottom: 14px; display: block;
  caret-color: rgba(180,210,130,0.9);
}
.title-input::placeholder { color: var(--text-dim); }

.info-row { display: flex; align-items: center; gap: 14px; margin-bottom: 12px; flex-wrap: wrap; }
.info-item {
  display: flex; align-items: center; gap: 5px;
  font-size: 11px; color: var(--text-muted);
  font-family: var(--font-mono);
}
.dot { width: 7px; height: 7px; border-radius: 50%; display: inline-block; }
.meta-row { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.tag-row { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 14px; }
.tag-pill { font-size: 11px; padding: 3px 10px; border-radius: 6px; }

/* ===== 分割线 ===== */
.detail-divider { height: 1px; background: var(--border); margin: 0 22px; flex-shrink: 0; }

/* ===== 统计条 ===== */
.stats-row { display: flex; gap: 1px; padding: 14px 22px 0; flex-shrink: 0; }
.stat {
  flex: 1; background: var(--surface2);
  padding: 10px 12px; text-align: center;
  border: 1px solid var(--border);
}
.stat:first-child { border-radius: 6px 0 0 6px; border-right: none; }
.stat:last-child  { border-radius: 0 6px 6px 0; border-left: none; }
.stat-num {
  font-family: var(--font-mono); font-size: 18px; font-weight: 700;
  color: rgba(180,210,130,0.9); line-height: 1;
}
.stat-label { font-size: 10px; color: var(--text-dim); margin-top: 3px; }

/* ===== 内容体 ===== */
.detail-body { flex: 1; overflow-y: auto; padding: 18px 22px 24px; }
.detail-body::-webkit-scrollbar { width: 3px; }
.detail-body::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.08); border-radius: 3px; }

.detail-content {
  font-size: 14px; color: var(--text-muted); line-height: 1.9;
}
.detail-content :deep(img) {
  max-width: 100%;
  border-radius: 6px;
  margin: 8px 0;
}
.detail-content :deep(a) {
  color: var(--accent);
  text-decoration: underline;
}
.content-editor {
  width: 100%; height: 100%; min-height: 400px;
  background: none; border: none; outline: none; resize: none;
  font-size: 14px; color: var(--text); line-height: 1.9;
  caret-color: rgba(180,210,130,0.9);
}
.content-editor::placeholder { color: var(--text-dim); }

/* ===== 附件上传区 ===== */
.attachment-area { margin-top: 16px; }

:deep(.el-upload-dragger) {
  background: var(--surface2) !important;
  border: 1px dashed var(--border) !important;
  border-radius: 8px !important;
  transition: border-color 0.2s !important;
}
:deep(.el-upload-dragger:hover) {
  border-color: rgba(var(--accent-rgb),0.55) !important;
}
:deep(.el-upload__text) { color: rgba(255,255,255,0.45) !important; font-size: 13px !important; }
:deep(.el-upload__text em) { color: rgba(180,210,130,0.85) !important; }
:deep(.el-upload__tip) { color: rgba(255,255,255,0.28) !important; font-size: 11px !important; }

/* 覆盖上传区文字颜色：白底模式下保证可读 */
::deep(.el-upload__text) { color: var(--text-muted) !important; font-size: 13px !important; }
::deep(.el-upload__text em) { color: var(--accent) !important; }
::deep(.el-upload__tip) { color: var(--text-dim) !important; font-size: 11px !important; }

/* ===== 附件展示区 ===== */
.attachments-section {
  margin-top: 20px;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 14px 16px;
  background: var(--surface2);
}
.attachments-title {
  font-size: 11px; font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase; letter-spacing: 1.2px; margin-bottom: 10px;
}
.image-grid { display: flex; flex-wrap: wrap; gap: 8px; }
.image-thumb-wrap {
  width: 90px; border-radius: 7px; overflow: hidden;
  border: 1px solid var(--border); text-decoration: none; flex-shrink: 0;
}
.image-thumb { width: 100%; height: 70px; object-fit: cover; display: block; }
.image-thumb-name {
  font-size: 10px; color: rgba(255,255,255,0.35); padding: 3px 5px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  background: var(--surface2);
}
.file-list { display: flex; flex-direction: column; gap: 6px; }
.attachment-item {
  display: flex; align-items: center; gap: 8px;
  padding: 6px 8px; border-radius: 6px;
  background: var(--surface2);
  border: 1px solid var(--border);
}
.att-icon { color: rgba(var(--accent-rgb),0.75); flex-shrink: 0; }
.att-name { flex: 1; font-size: 12px; color: var(--text); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.att-size { font-size: 11px; color: var(--text-dim); font-family: var(--font-mono); flex-shrink: 0; }

/* ===== Element Plus 按钮 / 选择器暗色覆盖 ===== */
:deep(.el-button) {
  --el-button-bg-color: var(--surface2);
  --el-button-border-color: var(--border);
  --el-button-text-color: var(--text-muted);
  --el-button-hover-bg-color: var(--surface3);
  --el-button-hover-border-color: var(--border-active);
  --el-button-hover-text-color: var(--text);
}
:deep(.el-button--primary) {
  --el-button-bg-color: var(--surface2);
  --el-button-border-color: rgba(var(--accent-rgb),0.50);
  --el-button-text-color: var(--accent);
  --el-button-hover-bg-color: var(--surface3);
  --el-button-hover-border-color: rgba(var(--accent-rgb),0.75);
  --el-button-hover-text-color: var(--text);
}
:deep(.el-select__wrapper) {
  background: var(--surface2) !important;
  border: 1px solid var(--border) !important;
  box-shadow: none !important;
  color: var(--text) !important;
}
:deep(.el-select__wrapper.is-focused) {
  border-color: rgba(var(--accent-rgb),0.55) !important;
  box-shadow: none !important;
}
:deep(.el-select__placeholder) { color: rgba(255,255,255,0.40) !important; }
::deep(.el-select__placeholder) { color: var(--text-dim) !important; }
</style>

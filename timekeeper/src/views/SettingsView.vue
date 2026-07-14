<!--
  设置页面

  4 个设置区块：
  - 个人设置：昵称修改、邮箱只读
  - 分类管理：列表 + 添加/编辑/删除
  - 计时偏好：番茄钟时长、倒计时默认值
  - 数据管理：清除记录、导出占位
-->

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useCategoryStore } from '@/stores/categories'
import { usePreferenceStore } from '@/stores/preferences'
import { useRecordStore } from '@/stores/records'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Edit, Delete, User, Calendar, Timer, Folder } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const categoryStore = useCategoryStore()
const prefStore = usePreferenceStore()
const recordStore = useRecordStore()

// ---- 个人设置 ----
const nickname = ref(authStore.user?.nickname || '')

function handleUpdateNickname() {
  if (!nickname.value.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  authStore.updateNickname(nickname.value)
  ElMessage.success('昵称已更新')
}

// ---- 分类管理 ----
// 预设 12 色色板
const colorPalette = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
  '#8e44ad', '#3498db', '#1abc9c', '#e74c3c',
  '#f39c12', '#2ecc71', '#9b59b6', '#34495e'
]

const showCatDialog = ref(false)
const catDialogMode = ref('add') // 'add' | 'edit'
const catForm = reactive({ id: '', name: '', color: '#409eff' })

function openAddCategory() {
  catDialogMode.value = 'add'
  catForm.id = ''
  catForm.name = ''
  catForm.color = '#409eff'
  showCatDialog.value = true
}

function openEditCategory(cat) {
  catDialogMode.value = 'edit'
  catForm.id = cat.id
  catForm.name = cat.name
  catForm.color = cat.color
  showCatDialog.value = true
}

function handleSaveCategory() {
  if (!catForm.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  if (catDialogMode.value === 'add') {
    categoryStore.addCategory(catForm.name.trim(), catForm.color)
    ElMessage.success('分类已添加')
  } else {
    categoryStore.updateCategory(catForm.id, {
      name: catForm.name.trim(),
      color: catForm.color
    })
    ElMessage.success('分类已更新')
  }
  showCatDialog.value = false
}

function handleDeleteCategory(cat) {
  if (categoryStore.categories.length <= 1) {
    ElMessage.warning('至少保留一个分类')
    return
  }
  ElMessageBox.confirm(`确定要删除分类"${cat.name}"吗？`, '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    categoryStore.deleteCategory(cat.id)
    ElMessage.success('分类已删除')
  }).catch(() => {})
}

// ---- 计时偏好 ----
const prefs = reactive({
  pomodoroWorkMinutes: 25,
  pomodoroBreakMinutes: 5,
  defaultCountdownMinutes: 25
})

onMounted(async () => {
  await Promise.all([
    categoryStore.loadCategories(),
    prefStore.loadPreferences()
  ])
  Object.assign(prefs, prefStore.preferences)
})

const countdownOptions = [
  { label: '15 分钟', value: 15 },
  { label: '25 分钟', value: 25 },
  { label: '30 分钟', value: 30 },
  { label: '45 分钟', value: 45 },
  { label: '60 分钟', value: 60 }
]

function handleSavePreferences() {
  prefStore.updatePreferences({
    pomodoroWorkMinutes: prefs.pomodoroWorkMinutes,
    pomodoroBreakMinutes: prefs.pomodoroBreakMinutes,
    defaultCountdownMinutes: prefs.defaultCountdownMinutes
  })
  ElMessage.success('偏好已保存')
}

// ---- 数据管理 ----
function handleClearRecords() {
  ElMessageBox.confirm(
    '此操作将永久删除所有计时记录，不可恢复。确定继续吗？',
    '危险操作',
    {
      confirmButtonText: '确认清除',
      cancelButtonText: '取消',
      type: 'error',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    await recordStore.loadAll()
    const ids = recordStore.records.map(r => r.id)
    for (const id of ids) {
      await recordStore.deleteRecord(id)
    }
    ElMessage.success('所有记录已清除')
  }).catch(() => {})
}
</script>

<template>
  <div class="page settings-page">
    <h1>设置</h1>

    <!-- 区块 1：个人设置 -->
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>个人设置</span>
        </div>
      </template>

      <el-form label-width="60px">
        <el-form-item label="昵称">
          <div style="display:flex;gap:8px;flex:1">
            <el-input v-model="nickname" placeholder="输入昵称" maxlength="20" />
            <el-button type="primary" @click="handleUpdateNickname">保存</el-button>
          </div>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input :model-value="authStore.user?.email || ''" disabled />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 区块 2：分类管理 -->
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
          <el-button type="primary" size="small" :icon="Plus" @click="openAddCategory" style="margin-left:auto">
            添加
          </el-button>
        </div>
      </template>

      <div v-if="categoryStore.categories.length === 0" class="empty-hint">暂无分类</div>

      <div class="cat-list">
        <div v-for="cat in categoryStore.categories" :key="cat.id" class="cat-item">
          <span class="cat-dot" :style="{ background: cat.color }"></span>
          <span class="cat-name">{{ cat.name }}</span>
          <el-button text size="small" :icon="Edit" @click="openEditCategory(cat)">编辑</el-button>
          <el-button text size="small" type="danger" :icon="Delete" @click="handleDeleteCategory(cat)">删除</el-button>
        </div>
      </div>
    </el-card>

    <!-- 区块 3：计时偏好 -->
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <el-icon><Timer /></el-icon>
          <span>计时偏好</span>
        </div>
      </template>

      <el-form label-width="140px">
        <el-form-item label="番茄钟工作时长">
          <el-input-number v-model="prefs.pomodoroWorkMinutes" :min="5" :max="60" :step="5" />
          <span style="margin-left:8px;color:#909399">分钟</span>
        </el-form-item>
        <el-form-item label="番茄钟休息时长">
          <el-input-number v-model="prefs.pomodoroBreakMinutes" :min="1" :max="30" :step="1" />
          <span style="margin-left:8px;color:#909399">分钟</span>
        </el-form-item>
        <el-form-item label="倒计时默认时长">
          <el-select v-model="prefs.defaultCountdownMinutes" style="width:160px">
            <el-option
              v-for="opt in countdownOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSavePreferences">保存偏好</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 区块 4：数据管理 -->
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <el-icon><Calendar /></el-icon>
          <span>数据管理</span>
        </div>
      </template>

      <div class="data-actions">
        <div class="data-row">
          <div>
            <strong>清除所有记录</strong>
            <p class="data-desc">删除所有计时记录，此操作不可恢复</p>
          </div>
          <el-button type="danger" @click="handleClearRecords">清除记录</el-button>
        </div>

        <el-divider />

        <div class="data-row">
          <div>
            <strong>导出数据</strong>
            <p class="data-desc">将记录导出为 CSV 文件（即将推出）</p>
          </div>
          <el-button disabled>即将推出</el-button>
        </div>
      </div>
    </el-card>

    <!-- 分类弹窗 -->
    <el-dialog
      v-model="showCatDialog"
      :title="catDialogMode === 'add' ? '添加分类' : '编辑分类'"
      width="380px"
    >
      <el-form label-width="60px">
        <el-form-item label="名称">
          <el-input v-model="catForm.name" placeholder="分类名称" maxlength="10" />
        </el-form-item>
        <el-form-item label="颜色">
          <div class="color-palette">
            <span
              v-for="c in colorPalette"
              :key="c"
              class="color-chip"
              :class="{ selected: catForm.color === c }"
              :style="{ background: c }"
              @click="catForm.color = c"
            ></span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCatDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCategory">
          {{ catDialogMode === 'add' ? '添加' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.settings-page {
  max-width: 640px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.settings-card {
  border-radius: 10px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

/* ---- 分类列表 ---- */
.cat-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  border-bottom: 1px solid #f0f0f0;
}

.cat-item:last-child {
  border-bottom: none;
}

.cat-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  flex-shrink: 0;
}

.cat-name {
  flex: 1;
  font-size: 14px;
}

/* ---- 色板 ---- */
.color-palette {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.color-chip {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  border: 3px solid transparent;
  transition: border-color 0.15s, transform 0.15s;
}

.color-chip:hover {
  transform: scale(1.15);
}

.color-chip.selected {
  border-color: #303133;
}

/* ---- 数据管理 ---- */
.data-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.data-desc {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.empty-hint {
  text-align: center;
  color: #909399;
  padding: 16px 0;
}
</style>

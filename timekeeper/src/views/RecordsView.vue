<!--
  历史记录页面

  功能：
  - 时间筛选（今天 / 本周 / 本月 / 全部）
  - 筛选条件下汇总（记录数 + 总时长）
  - 手动添加计时记录
  - 记录卡片列表（分类、模式、时长、时间范围、备注）
  - 编辑记录（分类、备注）
  - 删除记录（确认弹窗）
  - 空状态提示
-->
<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRecordStore } from '@/stores/records'
import { useCategoryStore } from '@/stores/categories'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const recordStore = useRecordStore()
const categoryStore = useCategoryStore()

// ---- 时间筛选 ----
const period = ref('today') // 'today' | 'week' | 'month' | 'all'

function todayStr() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function loadByPeriod() {
  const ds = todayStr()
  switch (period.value) {
    case 'today': recordStore.loadByDate(ds); break
    case 'week': recordStore.loadByWeek(ds); break
    case 'month': recordStore.loadByMonth(ds); break
    case 'all': recordStore.loadAll(); break
  }
}

onMounted(() => {
  categoryStore.loadCategories()
  loadByPeriod()
})

watch(period, () => {
  loadByPeriod()
})

/**
 * 根据筛选周期计算记录列表
 */
const filteredRecords = computed(() => {
  const now = new Date()
  switch (period.value) {
    case 'today':
      return recordStore.getRecordsByDate(now)
    case 'week':
      return recordStore.getRecordsByWeek(now)
    case 'month':
      return recordStore.getRecordsByMonth(now)
    case 'all':
      return recordStore.records
    default:
      return []
  }
})

/**
 * 筛选条件下的汇总
 */
const summary = computed(() => {
  const records = filteredRecords.value
  const totalSeconds = records.reduce((sum, r) => sum + r.durationSeconds, 0)
  return {
    count: records.length,
    duration: recordStore.formatDuration(totalSeconds)
  }
})

// ---- 手动添加记录 ----
const showAddDialog = ref(false)
const addForm = ref({
  categoryId: categoryStore.categories[0]?.id || '',
  mode: 'focus',
  startDate: '',
  startTime: '',
  endDate: '',
  endTime: '',
  note: ''
})

const addFormRef = ref(null)

const addRules = {
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'blur' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'blur' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'blur' }]
}

/**
 * 初始化添加表单的时间字段为当前时间
 */
function openAddDialog() {
  const now = new Date()
  addForm.value.startDate = formatDateStr(now)
  addForm.value.startTime = formatTimeStr(now)
  addForm.value.endDate = formatDateStr(now)
  addForm.value.endTime = formatTimeStr(new Date(now.getTime() + 30 * 60000))
  addForm.value.note = ''
  addForm.value.mode = 'focus'
  addForm.value.categoryId = categoryStore.selectedCategoryId || categoryStore.categories[0]?.id || ''
  showAddDialog.value = true
}

function handleAdd() {
  addFormRef.value?.validate((valid) => {
    if (!valid) return

    const start = new Date(`${addForm.value.startDate}T${addForm.value.startTime}`)
    const end = new Date(`${addForm.value.endDate}T${addForm.value.endTime}`)
    const durationSeconds = Math.max(0, Math.floor((end - start) / 1000))

    if (durationSeconds <= 0) {
      ElMessage.warning('结束时间必须晚于开始时间')
      return
    }

    const cat = categoryStore.categories.find(c => c.id === addForm.value.categoryId)
    recordStore.addRecord({
      categoryId: cat?.id || '',
      categoryName: cat?.name || '',
      categoryColor: cat?.color || '#409eff',
      mode: addForm.value.mode,
      startTime: start.toISOString(),
      endTime: end.toISOString(),
      durationSeconds,
      note: addForm.value.note

    })

    ElMessage.success('记录添加成功')
    showAddDialog.value = false
  })
}

// ---- 编辑记录 ----
const showEditDialog = ref(false)
const editForm = ref({ id: '', categoryId: '', note: '' })

function openEditDialog(record) {
  editForm.value = {
    id: record.id,
    categoryId: record.categoryId,
    note: record.note
  }
  showEditDialog.value = true
}

function handleEdit() {
  const cat = categoryStore.categories.find(c => c.id === editForm.value.categoryId)
  recordStore.updateRecord(editForm.value.id, {
    categoryId: editForm.value.categoryId,
    categoryName: cat?.name || '',
    categoryColor: cat?.color || '#409eff',
    note: editForm.value.note
  })
  ElMessage.success('记录已更新')
  showEditDialog.value = false
}

// ---- 删除记录 ----
function handleDelete(record) {
  ElMessageBox.confirm('确定要删除这条记录吗？此操作不可撤销。', '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    recordStore.deleteRecord(record.id)
    ElMessage.success('记录已删除')
  }).catch(() => { })
}

// ---- 工具函数 ----
function formatDateStr(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function formatTimeStr(date) {
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${h}:${min}`
}

/**
 * 格式化 ISO 时间串为可读的日期时间
 */
function formatDateTime(isoStr) {
  const d = new Date(isoStr)
  return `${formatDateStr(d)} ${formatTimeStr(d)}`
}

/**
 * 模式标签类型映射
 */
const modeTagType = {
  focus: 'primary',
  countdown: 'danger',
  pomodoro: 'warning'
}

const modeLabel = {
  focus: '正计时',
  countdown: '倒计时',
  pomodoro: '番茄钟'
}
</script>

<template>
  <div class="page records-page">
    <h1>历史记录</h1>

    <!-- 时间筛选 -->
    <div class="period-filter">
      <el-radio-group v-model="period" size="small">
        <el-radio-button value="today">今天</el-radio-button>
        <el-radio-button value="week">本周</el-radio-button>
        <el-radio-button value="month">本月</el-radio-button>
        <el-radio-button value="all">全部</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 汇总 + 添加按钮 -->
    <div class="records-header">
      <div class="summary-text">
        <template v-if="period === 'today'">今日</template>
        <template v-else-if="period === 'week'">本周</template>
        <template v-else-if="period === 'month'">本月</template>
        <template v-else>全部</template>
        共 <strong>{{ summary.count }}</strong> 条记录 · 合计 <strong>{{ summary.duration }}</strong>
      </div>
      <el-button type="primary" size="small" :icon="Plus" @click="openAddDialog">
        手动添加
      </el-button>
    </div>

    <!-- 记录列表 -->
    <div v-if="filteredRecords.length > 0" class="records-list">
      <div v-for="rec in filteredRecords" :key="rec.id" class="record-card">
        <div class="record-main">
          <!-- 分类颜色圆点 -->
          <span class="category-dot" :style="{ background: rec.categoryColor }"></span>
          <div class="record-info">
            <div class="record-top">
              <span class="record-category">{{ rec.categoryName }}</span>
              <el-tag :type="modeTagType[rec.mode] || 'info'" size="small">
                {{ modeLabel[rec.mode] || rec.mode }}
              </el-tag>
              <span class="record-duration">{{ recordStore.formatDuration(rec.durationSeconds) }}</span>
            </div>
            <div class="record-time">
              {{ formatDateTime(rec.startTime) }} — {{ formatDateTime(rec.endTime) }}
            </div>
            <div v-if="rec.note" class="record-note">{{ rec.note }}</div>
          </div>
        </div>
        <div class="record-actions">
          <el-button text size="small" :icon="Edit" @click="openEditDialog(rec)">编辑</el-button>
          <el-button text size="small" type="danger" :icon="Delete" @click="handleDelete(rec)">删除</el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="暂无记录" />

    <!-- 手动添加弹窗 -->
    <el-dialog v-model="showAddDialog" title="手动添加记录" width="420px">
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="80px">
        <el-form-item label="分类" prop="categoryId">
          <el-select v-if="categoryStore.categories.length" v-model="addForm.categoryId" style="width:100%">
            <el-option v-for="cat in categoryStore.categories" :key="cat.id" :label="cat.name" :value="String(cat.id)" />
          </el-select>
        </el-form-item>

        <el-form-item label="模式" prop="mode">
          <el-select v-model="addForm.mode" style="width:100%">
            <el-option label="正计时" value="focus" />
            <el-option label="倒计时" value="countdown" />
            <el-option label="番茄钟" value="pomodoro" />
          </el-select>
        </el-form-item>

        <el-form-item label="开始时间" required>
          <div style="display:flex;gap:8px;width:100%">
            <el-form-item prop="startDate" style="flex:1;margin-bottom:0">
              <el-input v-model="addForm.startDate" type="date" placeholder="日期" />
            </el-form-item>
            <el-form-item prop="startTime" style="flex:1;margin-bottom:0">
              <el-input v-model="addForm.startTime" type="time" placeholder="时间" />
            </el-form-item>
          </div>
        </el-form-item>

        <el-form-item label="结束时间" required>
          <div style="display:flex;gap:8px;width:100%">
            <el-form-item prop="endDate" style="flex:1;margin-bottom:0">
              <el-input v-model="addForm.endDate" type="date" placeholder="日期" />
            </el-form-item>
            <el-form-item prop="endTime" style="flex:1;margin-bottom:0">
              <el-input v-model="addForm.endTime" type="time" placeholder="时间" />
            </el-form-item>
          </div>
        </el-form-item>

        <el-form-item label="备注" prop="note">
          <el-input v-model="addForm.note" type="textarea" :rows="2" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">添加</el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑记录" width="380px">
      <el-form :model="editForm" label-width="60px">
        <el-form-item label="分类">
          <el-select v-model="editForm.categoryId" style="width:100%">
            <el-option v-for="cat in categoryStore.categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.note" type="textarea" :rows="3" placeholder="备注内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.records-page {
  max-width: 720px;
}

/* ---- 时间筛选 ---- */
.period-filter {
  margin-bottom: 20px;
}

/* ---- 汇总 + 添加按钮 ---- */
.records-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.summary-text {
  font-size: 14px;
  color: #606266;
}

/* ---- 记录列表 ---- */
.records-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  transition: box-shadow 0.2s;
}

.record-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.record-main {
  display: flex;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

/* 分类颜色圆点 */
.category-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.record-info {
  flex: 1;
  min-width: 0;
}

.record-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.record-category {
  font-size: 14px;
  font-weight: 500;
}

.record-duration {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-left: auto;
}

.record-time {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.record-note {
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  margin-top: 4px;
}

.record-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-left: 12px;
  flex-shrink: 0;
}

/* 移动端适配 */
@media (max-width: 640px) {
  .record-card {
    flex-direction: column;
  }

  .record-actions {
    flex-direction: row;
    margin-left: 22px;
    margin-top: 8px;
  }

  .record-duration {
    margin-left: 0;
  }
}
</style>

<!--
  目标设定页面

  功能：
  - 目标卡片列表（分类 + 周期 + 进度条）
  - 添加目标（分类、周期、小时数）
  - 编辑目标（修改小时数和周期）
  - 删除目标（确认弹窗）
  - 实时进度：从 records store 计算今日/本周实际时长 vs 目标
  - 目标达成时显示"已达成"标签
-->
<script setup>
import { computed, ref, onMounted } from 'vue'
import { useGoalStore } from '@/stores/goals'
import { useCategoryStore } from '@/stores/categories'
import { useRecordStore } from '@/stores/records'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Trophy } from '@element-plus/icons-vue'

const goalStore = useGoalStore()
const categoryStore = useCategoryStore()
const recordStore = useRecordStore()

// 进度缓存（getProgress 改为异步后不能在模板中直接调用）
const progressMap = ref({})

async function loadAllProgress() {
  for (const goal of goalStore.goals) {
    try {
      progressMap.value[goal.id] = await goalStore.getProgress(goal)
    } catch {
      progressMap.value[goal.id] = { currentSeconds: 0, targetSeconds: 0, percentage: 0 }
    }
  }
}

onMounted(async () => {
  await Promise.all([
    goalStore.loadGoals(),
    categoryStore.loadCategories()
  ])
  await loadAllProgress()
})

// 周期标签
const periodLabel = { daily: '每日', weekly: '每周' }

// ---- 添加目标 ----
const showAddDialog = ref(false)
const addFormRef = ref(null)

const addForm = ref({
  categoryId: '',
  period: 'daily',
  targetHours: 2
})

const addRules = {
  targetHours: [
    { required: true, message: '请输入目标时长', trigger: 'blur' },
    { type: 'number', min: 0.5, message: '至少 0.5 小时', trigger: 'blur' }
  ]
}

function openAddDialog() {
  addForm.value = { categoryId: '', period: 'daily', targetHours: 2 }
  showAddDialog.value = true
}

function handleAdd() {
  addFormRef.value?.validate(async (valid) => {
    if (!valid) return
    await goalStore.addGoal({
      categoryId: addForm.value.categoryId || null,
      period: addForm.value.period,
      targetHours: addForm.value.targetHours
    })
    ElMessage.success('目标已添加')
    showAddDialog.value = false
    await loadAllProgress()
  })
}

// ---- 编辑目标 ----
const showEditDialog = ref(false)
const editForm = ref({ id: '', period: 'daily', targetHours: 2 })
const editFormRef = ref(null)

const editRules = {
  targetHours: [
    { required: true, message: '请输入目标时长', trigger: 'blur' },
    { type: 'number', min: 0.5, message: '至少 0.5 小时', trigger: 'blur' }
  ]
}

function openEditDialog(goal) {
  editForm.value = {
    id: goal.id,
    period: goal.period,
    targetHours: goal.targetHours
  }
  showEditDialog.value = true
}

function handleEdit() {
  editFormRef.value?.validate(async (valid) => {
    if (!valid) return
    await goalStore.updateGoal(editForm.value.id, {
      period: editForm.value.period,
      targetHours: editForm.value.targetHours
    })
    ElMessage.success('目标已更新')
    showEditDialog.value = false
    await loadAllProgress()
  })
}

// ---- 删除目标 ----
function handleDelete(goal) {
  ElMessageBox.confirm('确定要删除这个目标吗？', '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await goalStore.deleteGoal(goal.id)
    ElMessage.success('目标已删除')
    await loadAllProgress()
  }).catch(() => {})
}
</script>

<template>
  <div class="page goals-page">
    <div class="goals-header">
      <h1>目标设定</h1>
      <el-button type="primary" :icon="Plus" @click="openAddDialog">
        添加目标
      </el-button>
    </div>

    <!-- 目标列表 -->
    <div v-if="goalStore.goals.length > 0" class="goals-list">
      <div v-for="goal in goalStore.goals" :key="goal.id" class="goal-card">
        <!-- 目标头部：分类 + 周期 -->
        <div class="goal-header">
          <div class="goal-title">
            <span
              class="goal-dot"
              :style="{ background: goalStore.getGoalCategoryInfo(goal).color }"
            ></span>
            <span class="goal-category">{{ goalStore.getGoalCategoryInfo(goal).name }}</span>
            <el-tag size="small" :type="goal.period === 'daily' ? '' : 'warning'">
              {{ periodLabel[goal.period] }}
            </el-tag>
            <span class="goal-target">{{ goal.targetHours }}h</span>
          </div>
          <div class="goal-actions">
            <el-button text size="small" :icon="Edit" @click="openEditDialog(goal)">编辑</el-button>
            <el-button text size="small" type="danger" :icon="Delete" @click="handleDelete(goal)">删除</el-button>
          </div>
        </div>

        <!-- 进度 -->
        <div class="goal-progress">
          <el-progress
            :percentage="progressMap[goal.id]?.percentage || 0"
            :color="(progressMap[goal.id]?.percentage || 0) >= 100 ? '#67c23a' : goalStore.getGoalCategoryInfo(goal).color"
            :stroke-width="12"
          >
            <template #default="{ percentage }">
              <div class="progress-text">
                <span>
                  {{ recordStore.formatDuration(progressMap[goal.id]?.currentSeconds || 0) }}
                  /
                  {{ recordStore.formatDuration(progressMap[goal.id]?.targetSeconds || 0) }}
                </span>
                <span>{{ percentage }}%</span>
              </div>
            </template>
          </el-progress>
        </div>

        <!-- 达成标识 -->
        <div v-if="(progressMap[goal.id]?.percentage || 0) >= 100" class="goal-achieved">
          <el-icon><Trophy /></el-icon>
          已达成
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="暂无目标，点击上方按钮添加" />

    <!-- 添加目标弹窗 -->
    <el-dialog v-model="showAddDialog" title="添加目标" width="400px">
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="addForm.categoryId" style="width:100%" clearable placeholder="全部（不限分类）">
            <el-option
              v-for="cat in categoryStore.categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="周期" prop="period">
          <el-radio-group v-model="addForm.period">
            <el-radio value="daily">每日</el-radio>
            <el-radio value="weekly">每周</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="目标时长" prop="targetHours">
          <el-input-number
            v-model="addForm.targetHours"
            :min="0.5"
            :max="24"
            :step="0.5"
            :precision="1"
            style="width:100%"
          />
          <span style="margin-left:8px;color:#909399">小时</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">添加</el-button>
      </template>
    </el-dialog>

    <!-- 编辑目标弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑目标" width="380px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="周期" prop="period">
          <el-radio-group v-model="editForm.period">
            <el-radio value="daily">每日</el-radio>
            <el-radio value="weekly">每周</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="目标时长" prop="targetHours">
          <el-input-number
            v-model="editForm.targetHours"
            :min="0.5"
            :max="24"
            :step="0.5"
            :precision="1"
            style="width:100%"
          />
          <span style="margin-left:8px;color:#909399">小时</span>
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
.goals-page {
  max-width: 640px;
}

.goals-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.goals-header h1 {
  margin-bottom: 0;
}

.goals-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.goal-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  border: 1px solid #e4e7ed;
}

.goal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.goal-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.goal-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.goal-category {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.goal-target {
  font-size: 13px;
  color: #909399;
  margin-left: 4px;
}

.goal-actions {
  display: flex;
  gap: 4px;
}

.goal-progress {
  margin-bottom: 8px;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  width: 100%;
  font-size: 12px;
  color: #909399;
  padding: 0 4px;
}

.goal-achieved {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #67c23a;
  font-weight: 500;
}
</style>

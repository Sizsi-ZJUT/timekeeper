<!--
  计时器页面（首页）

  功能：
  - 三种计时模式切换（正计时 / 倒计时 / 番茄钟）
  - SVG 圆形计时表盘，实时显示进度
  - 开始 / 暂停 / 重置控制
  - 分类选择下拉框
  - 倒计时快捷时长选择
  - 番茄钟阶段信息和轮数
  - 今日计时摘要

  计时完成后自动保存记录到 recordStore
-->
<script setup>
import { computed, onMounted, ref } from 'vue'
import { useTimerStore } from '@/stores/timer'
import { useCategoryStore } from '@/stores/categories'
import { useRecordStore } from '@/stores/records'
import { usePreferenceStore } from '@/stores/preferences'
import { VideoPlay, VideoPause, Refresh, Check } from '@element-plus/icons-vue'
import TimerCircle from '@/components/TimerCircle.vue'

const timerStore = useTimerStore()
const categoryStore = useCategoryStore()
const recordStore = useRecordStore()
const prefStore = usePreferenceStore()

// 倒计时快捷时长选项（秒）
const countdownPresets = [
  // { label: '15 分钟', seconds: 15 * 60 },
  // { label: '25 分钟', seconds: 25 * 60 },
  // { label: '30 分钟', seconds: 30 * 60 },
  // { label: '45 分钟', seconds: 45 * 60 },
  // { label: '60 分钟', seconds: 60 * 60 }
  // ,{ label: '设置时间', seconds: x * 60 }
]

// 当前选中的快捷时长标签（用于高亮）
const selectedPreset = computed(() => {
  if (timerStore.mode !== 'countdown') return null
  const idx = countdownPresets.findIndex(p => p.seconds === timerStore.targetSeconds)
  return idx !== -1 ? idx : null
})

// ---- 自定义时长输入 ----

/** 倒计时自定义分钟数 */
const customCountdownMinutes = ref(25)

/** 应用自定义倒计时时长 */
function applyCustomCountdown() {
  const mins = customCountdownMinutes.value
  if (mins > 0 && mins <= 1440) {
    timerStore.setTarget(mins * 60)
  }
}

/** 番茄钟自定义工作/休息分钟数 */
const pomodoroWorkMin = ref(prefStore.preferences.pomodoroWorkMinutes || 25)
const pomodoroBreakMin = ref(prefStore.preferences.pomodoroBreakMinutes || 5)

/** 应用番茄钟自定义时长（保存偏好 + 重置计时器） */
async function applyPomodoroCustom() {
  await prefStore.updatePreferences({
    pomodoroWorkMinutes: pomodoroWorkMin.value,
    pomodoroBreakMinutes: pomodoroBreakMin.value
  })
  timerStore.reset()
}

// 格式化今日总时长
const todayDisplay = computed(() => {
  const secs = recordStore.todayTotalSeconds
  const h = Math.floor(secs / 3600)
  const m = Math.floor((secs % 3600) / 60)
  if (h > 0) return `${h}h ${m}m`
  return `${m}m`
})

/** 是否可保存：暂停中且有实际计时时长 */
const canSave = computed(() => {
  return !timerStore.isRunning && getActualDuration() > 0
})

/**
 * 开始 / 暂停切换
 */
function toggleTimer() {
  if (timerStore.isRunning) {
    timerStore.pause()
  } else {
    timerStore.start()
  }
}

/**
 * 重置计时器
 */
function handleReset() {
  timerStore.reset()
}

/**
 * 切换模式
 */
function switchMode(m) {
  timerStore.setMode(m)
}

/**
 * 选择倒计时时长
 */
function selectPreset(seconds) {
  timerStore.setTarget(seconds)
}

/**
 * 计算本次计时实际已过时长（秒）
 * 正计时：elapsedSeconds 即为已过时长
 * 倒计时/番茄钟：elapsedSeconds 是剩余时间，target - elapsed = 已过时长
 */
function getActualDuration() {
  if (timerStore.mode === 'focus') {
    return timerStore.elapsedSeconds
  }
  return timerStore.targetSeconds - timerStore.elapsedSeconds
}

/**
 * 保存当前计时记录
 */
function handleSave() {
  const duration = getActualDuration()
  if (duration <= 0) return

  recordStore.addRecord({
    categoryId: categoryStore.selectedCategory?.id || null,
    categoryName: categoryStore.selectedCategory?.name || '',
    categoryColor: categoryStore.selectedCategory?.color || '#409eff',
    mode: timerStore.mode,
    startTime: new Date(Date.now() - duration * 1000).toISOString(),
    endTime: new Date().toISOString(),
    durationSeconds: duration
  })
  timerStore.reset()
}

/**
 * 计时完成回调：自动保存记录（倒计时归零 / 番茄钟阶段结束）
 */
function handleTimerComplete() {
  const duration = getActualDuration()
  if (duration <= 0) return

  recordStore.addRecord({
    categoryId: categoryStore.selectedCategory?.id || null,
    categoryName: categoryStore.selectedCategory?.name || '',
    categoryColor: categoryStore.selectedCategory?.color || '#409eff',
    mode: timerStore.mode,
    startTime: new Date(Date.now() - duration * 1000).toISOString(),
    endTime: new Date().toISOString(),
    durationSeconds: duration
  })
}

function todayStr() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 注册回调 + 加载数据
onMounted(() => {
  timerStore.onComplete(handleTimerComplete)
  categoryStore.loadCategories()
  recordStore.loadByDate(todayStr())
})

// 组件卸载时暂停计时（避免后台继续跑）
// onUnmounted(() => {
//   if (timerStore.isRunning) {
//     timerStore.pause()
//   }
// })
</script>

<template>
  <div class="page timer-page">
    <!-- 模式切换 -->
    <div class="mode-tabs">
      <button v-for="m in [
        { key: 'focus', label: '正计时' },
        { key: 'countdown', label: '倒计时' },
        { key: 'pomodoro', label: '番茄钟' }
      ]" :key="m.key" :class="['mode-tab', { active: timerStore.displayMode === m.key }]" @click="switchMode(m.key)">
        {{ m.label }}
      </button>
    </div>

    <!-- 圆形表盘 -->
    <TimerCircle :display-time="timerStore.displayTime" :progress="timerStore.progress"
      :is-running="timerStore.isRunning" :mode="timerStore.mode" :pomodoro-phase="timerStore.pomodoroPhase" />

    <!-- 控制按钮 -->
    <div class="timer-controls">
      <el-button :type="timerStore.isRunning ? 'warning' : 'primary'"
        :icon="timerStore.isRunning ? VideoPause : VideoPlay" size="large" circle class="control-btn control-btn--main"
        @click="toggleTimer" />
      <el-button v-if="canSave" type="success" :icon="Check" size="large" circle
        class="control-btn control-btn--main" @click="handleSave" />
      <el-button :icon="Refresh" size="large" circle class="control-btn"
        :disabled="!timerStore.isRunning && timerStore.elapsedSeconds === 0" @click="handleReset" />
    </div>

    <!-- 倒计时：快捷时长 + 自定义输入 -->
    <div v-if="timerStore.displayMode === 'countdown'" class="time-config">
      <div class="preset-row">
        <el-button v-for="(preset, idx) in countdownPresets" :key="preset.seconds"
          :type="selectedPreset === idx ? 'primary' : 'default'" size="small" @click="selectPreset(preset.seconds)">
          {{ preset.label }}
        </el-button>
      </div>
      <div class="custom-time-row">
        <el-input-number v-model="customCountdownMinutes" :min="1" :max="1440" :step="5" size="small" style="width: 120px" />
        <span class="custom-unit">分钟</span>
        <el-button type="primary" size="small" @click="applyCustomCountdown">设置</el-button>
      </div>
    </div>

    <!-- 番茄钟：阶段信息 + 自定义时长 -->
    <div v-if="timerStore.displayMode === 'pomodoro'" class="time-config">
      <div class="pomodoro-info">
        <el-tag :type="timerStore.pomodoroPhase === 'work' ? 'warning' : 'success'">
          {{ timerStore.pomodoroPhase === 'work' ? '工作中' : '休息中' }}
        </el-tag>
        <span class="pomodoro-count">已完成 {{ timerStore.pomodoroCount }} 个番茄</span>
      </div>
      <div class="custom-time-row">
        <span class="custom-label">工作</span>
        <el-input-number v-model="pomodoroWorkMin" :min="1" :max="120" :step="5" size="small" style="width: 100px" />
        <span class="custom-unit">分钟</span>
        <span class="custom-label">休息</span>
        <el-input-number v-model="pomodoroBreakMin" :min="1" :max="60" :step="1" size="small" style="width: 100px" />
        <span class="custom-unit">分钟</span>
        <el-button type="primary" size="small" @click="applyPomodoroCustom">应用</el-button>
      </div>
    </div>

    <!-- 分类选择 -->
    <div class="category-row">
      <span class="label">分类：</span>
      <el-select v-if="categoryStore.categories.length" v-model="categoryStore.selectedCategoryId" size="small" style="width: 160px">
        <el-option v-for="cat in categoryStore.categories" :key="cat.id" :label="cat.name" :value="String(cat.id)">
          <span
            :style="{ display: 'inline-block', width: '10px', height: '10px', borderRadius: '50%', background: cat.color, marginRight: '8px' }"></span>
          {{ cat.name  }} 
        </el-option>
      </el-select>
    </div>

    <!-- 今日摘要 -->
    <div class="today-summary">
      <span>今日已记录 <strong>{{ recordStore.todayCount }}</strong> 条，共 <strong>{{ todayDisplay }}</strong></span>
    </div>
  </div>
</template>

<style scoped>
.timer-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding-top: 16px;
}

/* ---- 模式切换 ---- */
.mode-tabs {
  display: flex;
  background: #e4e7ed;
  border-radius: 8px;
  padding: 4px;
}

.mode-tab {
  padding: 8px 20px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.2s;
}

.mode-tab.active {
  background: #fff;
  color: #409eff;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* ---- 控制按钮 ---- */
.timer-controls {
  display: flex;
  align-items: center;
  gap: 24px;
}

.control-btn--main {
  width: 64px;
  height: 64px;
  font-size: 24px;
}

.control-btn {
  width: 44px;
  height: 44px;
}

/* ---- 时长配置区 ---- */
.time-config {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

/* ---- 倒计时快捷时长 ---- */
.preset-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.custom-time-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.custom-unit {
  color: #909399;
}

.custom-label {
  margin-left: 8px;
  font-weight: 500;
}

/* ---- 番茄钟 ---- */
.pomodoro-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #606266;
}

/* ---- 分类选择 ---- */
.category-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

/* ---- 今日摘要 ---- */
.today-summary {
  font-size: 13px;
  color: #909399;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
  width: 100%;
  max-width: 320px;
  text-align: center;
}
</style>

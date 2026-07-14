<!--
  TimerCircle - SVG 圆形计时表盘

  Props:
  - displayTime: 格式化时间字符串 HH:MM:SS
  - progress: 0-1 进度值
  - isRunning: 是否正在计时
  - mode: 计时模式 (focus/countdown/pomodoro)，影响环颜色
  - pomodoroPhase: 番茄钟阶段 (work/break)，仅 pomodoro 模式使用

  使用 SVG circle 绘制进度环，stroke-dasharray/dashoffset 控制进度
  中央大字显示时间，下方显示当前模式标签
-->
<script setup>
import { computed } from 'vue'

const props = defineProps({
  displayTime: { type: String, required: true },
  progress: { type: Number, default: 0 },
  isRunning: { type: Boolean, default: false },
  mode: { type: String, default: 'focus' },
  pomodoroPhase: { type: String, default: 'work' }
})

// SVG 圆形参数
const radius = 140
const circumference = 2 * Math.PI * radius

// 已完成的弧长（倒计时模式下反转，显示剩余量）
const dashOffset = computed(() => circumference * (1 - props.progress))

/**
 * 进度环颜色随模式变化
 */
const strokeColor = computed(() => {
  if (props.mode === 'focus') return '#409eff'    // 蓝色
  if (props.mode === 'pomodoro') {
    return props.pomodoroPhase === 'break' ? '#67c23a' : '#e6a23c' // 休息绿/工作橙
  }
  return '#f56c6c' // 倒计时红色
})

/**
 * 模式标签文字
 */
const modeLabel = computed(() => {
  if (props.mode === 'focus') return '正计时'
  if (props.mode === 'countdown') return '倒计时'
  if (props.mode === 'pomodoro') {
    return props.pomodoroPhase === 'break' ? '休息中' : '工作中'
  }
  return ''
})
</script>

<template>
  <div class="timer-circle" :class="{ 'timer-circle--running': isRunning }">
    <svg viewBox="0 0 320 320" class="timer-svg">
      <!-- 背景环 -->
      <circle
        cx="160" cy="160" :r="radius"
        fill="none"
        stroke="#e4e7ed"
        stroke-width="12"
      />
      <!-- 进度环（从顶部 12 点钟方向开始，顺时针） -->
      <circle
        cx="160" cy="160" :r="radius"
        fill="none"
        :stroke="strokeColor"
        stroke-width="12"
        stroke-linecap="round"
        :stroke-dasharray="circumference"
        :stroke-dashoffset="dashOffset"
        class="progress-ring"
        transform="rotate(-90 160 160)"
      />
    </svg>
    <!-- 中央时间文字 -->
    <div class="timer-display">
      <span class="timer-time">{{ displayTime }}</span>
      <span class="timer-label">{{ modeLabel }}</span>
    </div>
  </div>
</template>

<style scoped>
.timer-circle {
  position: relative;
  width: 320px;
  height: 320px;
  margin: 0 auto;
}

.timer-svg {
  width: 100%;
  height: 100%;
  transform: rotate(0deg);
}

/* 进度环过渡动画（运行时平滑，停止时立即） */
.progress-ring {
  transition: stroke-dashoffset 0.3s linear;
}

.timer-circle--running .progress-ring {
  transition: stroke-dashoffset 0.5s linear;
}

/* 时间文字层叠在 SVG 上方 */
.timer-display {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.timer-time {
  font-size: 42px;
  font-weight: 700;
  font-family: 'Courier New', Courier, monospace;
  color: #303133;
  letter-spacing: 2px;
}

.timer-label {
  font-size: 14px;
  color: #909399;
}

/* 移动端适配 */
@media (max-width: 480px) {
  .timer-circle {
    width: 260px;
    height: 260px;
  }

  .timer-time {
    font-size: 32px;
  }
}
</style>

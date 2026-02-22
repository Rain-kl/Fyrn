<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from "vue";
import dayjs from "dayjs";
import type { WbTask } from "~/api/models/WbTask";

const props = withDefaults(
  defineProps<{
    task: WbTask;
    compact?: boolean;
  }>(),
  {
    compact: false,
  },
);

const status = computed(() => props.task.status || 0);
const progress = computed(() => props.task.progress || 0);
const taskType = computed(() => props.task.type || 0);
const nowTick = ref(Date.now());
let tickTimer: ReturnType<typeof setInterval> | undefined;

const statusLabel = computed(() => {
  const map: Record<number, string> = {
    0: "未开始",
    1: "进行中",
    2: "已完成",
    3: "已作废",
  };
  return map[status.value] || "未知";
});

const statusIcon = computed(() => {
  if (status.value === 1) return "i-lucide-play-circle";
  if (status.value === 2) return "i-lucide-check-circle-2";
  if (status.value === 3) return "i-lucide-x-circle";
  return "i-lucide-circle-dashed";
});

const statusColor = computed(() => {
  if (status.value === 1) return "text-blue-500";
  if (status.value === 2) return "text-green-500";
  return "text-gray-400";
});

const deadlineAlert = computed(() => {
  if (!props.task.deadline) return null;
  if (status.value === 2 || status.value === 3) return null;

  const deadlineTime = dayjs(props.task.deadline);
  if (!deadlineTime.isValid()) return null;

  // Reminder: use minute-level countdown.
  if (taskType.value === 1) {
    const now = dayjs(nowTick.value);
    const minutesLeft = deadlineTime.diff(now, "minute");
    const absMinutes = Math.abs(minutesLeft);
    const durationText =
      absMinutes >= 60
        ? `${Math.floor(absMinutes / 60)}小时${absMinutes % 60}分钟`
        : `${absMinutes} 分钟`;

    if (minutesLeft < 0) {
      return { label: `已超期 ${durationText}`, className: "text-red-500" };
    }
    if (minutesLeft < 3 * 24 * 60) {
      return { label: `剩余 ${durationText}`, className: "text-orange-500" };
    }
    if (minutesLeft > 7 * 24 * 60) {
      return { label: `剩余 ${durationText}`, className: "text-green-500" };
    }
    return { label: `剩余 ${durationText}`, className: "text-blue-500" };
  }

  // Task: keep original day-based logic.
  const daysLeft = deadlineTime.startOf("day").diff(dayjs().startOf("day"), "day");
  if (daysLeft < 0) {
    return { label: "已超期", className: "text-red-500" };
  }
  if (daysLeft < 3) {
    return { label: `${daysLeft}天内截止`, className: "text-orange-500" };
  }
  if (daysLeft > 7) {
    return { label: `${daysLeft}天内截止`, className: "text-green-500" };
  }
  return { label: `${daysLeft}天内截止`, className: "text-blue-500" };
});

const radius = computed(() => (props.compact ? 4 : 5));
const circumference = computed(() => 2 * Math.PI * radius.value);
const dashoffset = computed(
  () => circumference.value - (progress.value / 100) * circumference.value,
);

onMounted(() => {
  tickTimer = setInterval(() => {
    nowTick.value = Date.now();
  }, 60000);
});

onBeforeUnmount(() => {
  if (tickTimer) clearInterval(tickTimer);
});
</script>

<template>
  <div :class="['flex items-center', compact ? 'gap-2' : 'gap-4']">
    <span
      v-if="taskType === 1"
      :class="[
        'text-muted inline-block text-right',
        compact ? 'w-10 text-xs' : 'w-14 pr-2',
      ]"
    >
      -
    </span>

    <div v-else :class="['flex items-center gap-1.5', compact ? 'w-12' : 'w-14']">
      <div
        :class="[
          'relative flex items-center justify-center shrink-0',
          compact ? 'w-3 h-3' : 'w-3.5 h-3.5',
        ]"
      >
        <svg class="w-full h-full transform -rotate-90" viewBox="0 0 16 16">
          <circle
            cx="8"
            cy="8"
            :r="radius"
            fill="none"
            stroke-width="3"
            class="stroke-muted/30"
          />
          <circle
            cx="8"
            cy="8"
            :r="radius"
            fill="none"
            stroke-width="3"
            :class="progress === 100 ? 'stroke-success transition-all duration-300' : 'stroke-green-400 transition-all duration-300'"
            :stroke-dasharray="circumference"
            :stroke-dashoffset="dashoffset"
            stroke-linecap="round"
          />
        </svg>
      </div>
      <span
        :class="[
          'font-semibold',
          compact ? 'text-[11px]' : 'text-xs',
          progress === 100 ? 'text-success' : 'text-green-500',
        ]"
      >
        {{ progress }}%
      </span>
    </div>

    <div
      :class="[
        'flex items-center gap-1 font-medium',
        compact ? 'text-xs' : 'text-sm',
        statusColor,
      ]"
    >
      <div :class="`${statusIcon} w-4 h-4`" />
      <span>{{ statusLabel }}</span>
    </div>

    <span
      v-if="deadlineAlert"
      :class="[
        'font-medium whitespace-nowrap',
        compact ? 'text-[11px]' : 'text-xs',
        deadlineAlert.className,
      ]"
    >
      {{ deadlineAlert.label }}
    </span>
  </div>
</template>

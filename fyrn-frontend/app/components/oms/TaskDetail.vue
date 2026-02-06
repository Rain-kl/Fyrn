<script setup lang="ts">
import { useApi } from "~/api/useApi";
import type { TaskDetailOutput } from "~/api/models";

const props = defineProps<{
  taskId: string;
}>();

const { OmsTaskControllerApi } = useApi();

const task = ref<TaskDetailOutput | null>(null);
const loading = ref(false);
let timer: ReturnType<typeof setInterval> | null = null;
const logContainerRef = ref<HTMLElement | null>(null);

const fetchData = async () => {
  if (!task.value) loading.value = true;
  try {
    const res = await OmsTaskControllerApi.omsTaskDetailGet({
      taskId: props.taskId,
    });
    if (res.code === 200 && res.data) {
      task.value = res.data;
    }
  } catch (e) {
    console.error("Failed to fetch task details:", e);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
  timer = setInterval(fetchData, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

watch(
  () => task.value?.taskLog,
  () => {
    nextTick(() => {
      if (logContainerRef.value) {
        logContainerRef.value.scrollTop = logContainerRef.value.scrollHeight;
      }
    });
  }
);

const hasTotal = computed(() => {
  return (
    task.value?.taskProgressDTO?.total !== undefined &&
    task.value.taskProgressDTO.total > 0
  );
});

const progressPercentage = computed(() => {
  if (!task.value?.taskProgressDTO) return 0;
  const { current, total } = task.value.taskProgressDTO;
  if (!total) return 0;
  const pct = (current / total) * 100;
  return Math.min(100, Math.max(0, pct));
});

const statusLabel = computed(() => {
  if (!task.value) return "";
  const statusMap: Record<number, string> = {
    0: "排队中",
    1: "运行中",
    2: "成功",
    3: "失败",
    4: "已取消",
  };
  return statusMap[task.value.status as number] || "未知";
});

const allowRetryLabel = computed(() => {
  if (task.value?.allowRetry === undefined || task.value?.allowRetry === null) {
    return "-";
  }
  return task.value.allowRetry === 1 ? "允许" : "不允许";
});

const allowRetryVariant = computed(() => {
  if (task.value?.allowRetry === 1) return "badge-soft-success";
  if (task.value?.allowRetry === 0) return "badge-soft-gray";
  return "badge-soft-gray";
});
</script>

<template>
  <div class="flex flex-col gap-4 h-full overflow-hidden">
    <div v-if="task" class="flex flex-col gap-4 h-full">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-hash size-3" />
            Task ID
          </div>
          <div class="font-mono text-sm truncate">{{ task.taskId }}</div>
        </div>

        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-tag size-3" />
            任务标签
          </div>
          <div>
            <NBadge
              v-if="task.taskTag"
              :label="task.taskTag"
              :una="{
                badgeDefaultVariant: 'badge-soft-info',
              }"
            />
            <span v-else class="text-sm">-</span>
          </div>
        </div>

        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-briefcase size-3" />
            业务标签
          </div>
          <div>
            <NBadge
              v-if="task.bizTag"
              :label="task.bizTag"
              :una="{
                badgeDefaultVariant: 'badge-soft-warning',
              }"
            />
            <span v-else class="text-sm">-</span>
          </div>
        </div>

        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-barcode size-3" />
            业务值
          </div>
          <div class="text-sm">{{ task.bizValue || "-" }}</div>
        </div>

        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-activity size-3" />
            当前状态
          </div>
          <div>
            <NBadge
              :label="statusLabel"
              :una="{
                badgeDefaultVariant:
                  task.status === 2
                    ? 'badge-soft-success'
                    : task.status === 1
                    ? 'badge-soft-warning'
                    : task.status === 3
                    ? 'badge-soft-error'
                    : 'badge-soft-gray',
              }"
            />
          </div>
        </div>

        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-rotate-ccw size-3" />
            允许重试
          </div>
          <div>
            <NBadge
              v-if="allowRetryLabel !== '-'"
              :label="allowRetryLabel"
              :una="{
                badgeDefaultVariant: allowRetryVariant,
              }"
            />
            <span v-else class="text-sm">-</span>
          </div>
        </div>

        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-user size-3" />
            触发人
          </div>
          <div class="text-sm">{{ task.createdUser || "-" }}</div>
        </div>

        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-calendar size-3" />
            创建时间
          </div>
          <div class="text-sm">
            {{ task.createTime ? new Date(task.createTime).toLocaleString() : '-' }}
          </div>
        </div>

        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-play size-3" />
            开始时间
          </div>
          <div class="text-sm">
            {{ task.startedTime ? new Date(task.startedTime).toLocaleString() : '-' }}
          </div>
        </div>

        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-flag size-3" />
            结束时间
          </div>
          <div class="text-sm">
            {{ task.finishedTime ? new Date(task.finishedTime).toLocaleString() : '-' }}
          </div>
        </div>
      </div>

      <div v-if="task.message" class="p-3 rounded-lg border border-base bg-base/30">
        <div class="text-xs text-muted font-medium mb-1 flex items-center gap-1">
          <div class="i-lucide-info size-3" />
          状态说明
        </div>
        <CommonTextCollapse :text="task.message" :max-length="200" class="text-sm" />
      </div>

      <div v-if="task.status === 1 || (task.taskProgressDTO && task.taskProgressDTO.total > 0)" class="flex flex-col gap-2">
        <div class="flex justify-between items-end">
          <div class="text-sm font-medium flex items-center gap-1">
            <div class="i-lucide-loader-2 size-4 animate-spin text-primary" v-if="task.status === 1" />
            <div class="i-lucide-check-circle size-4 text-success" v-else-if="task.status === 2" />
            任务进度
          </div>
          <div v-if="hasTotal" class="text-xs text-muted font-mono">
            {{ task.taskProgressDTO?.current }} / {{ task.taskProgressDTO?.total }} ({{ progressPercentage.toFixed(1) }}%)
          </div>
        </div>
        <NProgress
          v-if="!hasTotal && task.status === 1"
          indeterminate
          progress="primary"
          size="sm"
        />
        <NProgress
          v-else
          :model-value="progressPercentage"
          progress="primary"
          size="sm"
        />
      </div>

      <div class="flex flex-col gap-2 flex-1 min-h-0">
        <div class="flex justify-between items-center">
          <div class="text-sm font-medium flex items-center gap-1">
            <div class="i-lucide-terminal size-4" />
            执行日志
          </div>
          <div class="text-[10px] px-1.5 py-0.5 rounded border border-base bg-base text-muted font-mono uppercase">
            Console
          </div>
        </div>
        <div
          ref="logContainerRef"
          class="w-full h-full rounded-lg bg-zinc-950 text-zinc-50 p-4 overflow-auto whitespace-pre-wrap font-mono text-xs leading-relaxed border border-zinc-800 shadow-inner selection:bg-primary/30"
        >
          <template v-if="task.taskLog">
            {{ task.taskLog }}
          </template>
          <div v-else class="text-zinc-300 italic flex items-center gap-2">
            <div class="i-lucide-clock size-4" />
            暂无日志...
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="loading" class="flex flex-col items-center justify-center h-64 gap-4">
      <div class="i-lucide-loader-2 size-8 animate-spin text-primary" />
      <div class="text-sm text-muted animate-pulse">正在获取任务详情...</div>
    </div>

    <div v-else class="flex flex-col items-center justify-center h-64 gap-3 text-muted">
      <div class="i-lucide-file-question size-10 opacity-20" />
      <div class="text-sm">未能获取到任务详情</div>
    </div>
  </div>
</template>

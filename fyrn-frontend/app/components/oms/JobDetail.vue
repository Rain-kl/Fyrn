<script setup lang="ts">
import { useApi } from "~/api/useApi";
import type { OmsJob } from "~/api/models";

const props = defineProps<{
  jobId: string;
}>();

const { JobControllerApi } = useApi();

interface JobProgressDto {
  current: number;
  total: number;
}

interface ExtendedOmsJob extends OmsJob {
  jobProgressDto?: JobProgressDto;
  jobLog?: string;
}

const job = ref<ExtendedOmsJob | null>(null);
const loading = ref(false);
let timer: ReturnType<typeof setInterval> | null = null;
const logContainerRef = ref<HTMLElement | null>(null);

const fetchData = async () => {
  if (!job.value) loading.value = true;
  try {
    const res = await JobControllerApi.omsJobDetailGet({
      operator: "admin",
      jobId: props.jobId,
    });
    if (res.code === 200 && res.data) {
      job.value = res.data as ExtendedOmsJob;
    }
  } catch (e) {
    console.error("Failed to fetch job details:", e);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
  // 自动刷新
  timer = setInterval(fetchData, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

// Auto-scroll to bottom if log updates
watch(
  () => job.value?.jobLog,
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
    job.value?.jobProgressDto?.total !== undefined &&
    job.value.jobProgressDto.total > 0
  );
});

const progressPercentage = computed(() => {
  if (!job.value?.jobProgressDto) return 0;
  const { current, total } = job.value.jobProgressDto;
  if (!total) return 0;
  // Calculate percentage, max 100
  const pct = (current / total) * 100;
  return Math.min(100, Math.max(0, pct));
});

const statusLabel = computed(() => {
  if (!job.value) return "";
  const statusMap: Record<number, string> = {
    0: "排队中",
    1: "运行中",
    2: "成功",
    3: "失败",
    4: "已取消",
  };
  return statusMap[job.value.status as number] || "未知";
});
</script>

<template>
  <div class="flex flex-col gap-4 h-full overflow-hidden">
    <div v-if="job" class="flex flex-col gap-4 h-full">
      <!-- Header Info -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-hash size-3" />
            Job ID
          </div>
          <div class="font-mono text-sm truncate">{{ job.jobId }}</div>
        </div>
        
        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-tag size-3" />
            任务类型
          </div>
          <div class="text-sm">{{ job.taskType }}</div>
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
                  job.status === 2
                    ? 'badge-soft-success'
                    : job.status === 1
                    ? 'badge-soft-warning'
                    : job.status === 3
                    ? 'badge-soft-error'
                    : 'badge-soft-gray',
              }"
            />
          </div>
        </div>

        <div class="p-3 rounded-lg border border-base bg-base/50 flex flex-col gap-1">
          <div class="text-xs text-muted font-medium flex items-center gap-1">
            <div class="i-lucide-calendar size-3" />
            创建时间
          </div>
          <div class="text-sm">
            {{ job.createTime ? new Date(job.createTime).toLocaleString() : '-' }}
          </div>
        </div>
      </div>

      <!-- Message -->
      <div v-if="job.message" class="p-3 rounded-lg border border-base bg-base/30">
        <div class="text-xs text-muted font-medium mb-1 flex items-center gap-1">
          <div class="i-lucide-info size-3" />
          状态说明
        </div>
        <CommonTextCollapse :text="job.message" :max-length="200" class="text-sm" />
      </div>

      <!-- Progress -->
      <div v-if="job.status === 1 || (job.jobProgressDto && job.jobProgressDto.total > 0)" class="flex flex-col gap-2">
        <div class="flex justify-between items-end">
          <div class="text-sm font-medium flex items-center gap-1">
            <div class="i-lucide-loader-2 size-4 animate-spin text-primary" v-if="job.status === 1" />
            <div class="i-lucide-check-circle size-4 text-success" v-else-if="job.status === 2" />
            任务进度
          </div>
          <div v-if="hasTotal" class="text-xs text-muted font-mono">
            {{ job.jobProgressDto?.current }} / {{ job.jobProgressDto?.total }} ({{ progressPercentage.toFixed(1) }}%)
          </div>
        </div>
        <NProgress
          v-if="!hasTotal && job.status === 1"
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

      <!-- Logs -->
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
          <template v-if="job.jobLog">
            {{ job.jobLog }}
          </template>
          <div v-else class="text-zinc-300 italic flex items-center gap-2">
            <div class="i-lucide-clock size-4" />
            暂无日志...
          </div>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-else-if="loading" class="flex flex-col items-center justify-center h-64 gap-4">
      <div class="i-lucide-loader-2 size-8 animate-spin text-primary" />
      <div class="text-sm text-muted animate-pulse">正在获取任务详情...</div>
    </div>

    <!-- Error/Empty State -->
    <div v-else class="flex flex-col items-center justify-center h-64 gap-3 text-muted">
      <div class="i-lucide-file-question size-10 opacity-20" />
      <div class="text-sm">未能获取到任务详情</div>
    </div>
  </div>
</template>

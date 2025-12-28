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
  <div class="flex flex-col gap-6 p-4 h-full">
    <div v-if="job" class="flex flex-col gap-6 h-full">
      <!-- Header Info -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 text-sm">
        <div>
          <span class="text-muted">Job ID:</span>
          <span class="font-mono ml-2">{{ job.jobId }}</span>
        </div>
        <div>
          <span class="text-muted">任务类型:</span>
          <span class="ml-2">{{ job.taskType }}</span>
        </div>
        <div>
          <span class="text-muted">状态:</span>
          <NBadge
            class="ml-2"
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
        <div>
          <span class="text-muted">创建时间:</span>
          <span class="ml-2">{{
            job.createTime && new Date(job.createTime).toLocaleString()
          }}</span>
        </div>

        <div class="col-span-full" v-if="job.message">
          <span class="text-muted text-xs block mb-1">状态说明:</span>
          <CommonTextCollapse :text="job.message" :max-length="200" />
        </div>
      </div>

      <!-- Progress -->
      <div class="flex flex-col gap-2" v-if="job.status === 1">
        <div class="font-medium">任务进度</div>
        <div class="w-full">
          <NProgress v-if="!hasTotal" indeterminate progress="primary" />
          <NProgress
            v-else
            :model-value="progressPercentage"
            progress="lime"
            size="0.5cm"
          />
          <div v-if="hasTotal" class="text-xs text-muted text-right mt-1">
            {{ job.jobProgressDto?.current }} / {{ job.jobProgressDto?.total }}
          </div>
        </div>
      </div>

      <!-- Logs -->
      <div class="flex flex-col gap-2 flex-1 min-h-[300px]">
        <div class="font-medium">执行日志</div>
        <div
          ref="logContainerRef"
          class="w-full h-full border rounded-md bg-gray-900 text-gray-100 p-4 overflow-auto whitespace-pre-wrap font-mono text-xs leading-5"
        >
          {{ job.jobLog || "暂时没有日志..." }}
        </div>
      </div>
    </div>

    <div v-else-if="loading" class="flex items-center justify-center p-10">
      <span class="animate-pulse">Loading job details...</span>
    </div>

    <div v-else class="flex items-center justify-center p-10 text-muted">
      未能获取到任务详情
    </div>
  </div>
</template>

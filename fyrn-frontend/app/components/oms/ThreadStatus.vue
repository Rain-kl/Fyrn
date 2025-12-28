<script setup lang="ts">
import { useApi } from "~/api/useApi";

const { JobControllerApi } = useApi();

// Define reactive state for data
const monitorData = ref<any>(null);
const loading = ref(false);

// Function to fetch data
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await JobControllerApi.omsJobMonitorGet();
    if (res.code === 200) {
      monitorData.value = res.data;
    }
  } catch (error) {
    console.error("Failed to fetch thread status:", error);
  } finally {
    loading.value = false;
  }
};

// Fetch on mount
onMounted(() => {
  fetchData();
});

// Auto refresh every 3 seconds
const timer = ref();
onMounted(() => {
  timer.value = setInterval(fetchData, 3000);
});

onUnmounted(() => {
  if (timer.value) clearInterval(timer.value);
});

// Compute metrics for easier access
const metrics = computed(() => monitorData.value?.threadPoolMetricsDTO || {});
const jobOverview = computed(() => monitorData.value?.jobOverviewDto || {});

// Helper to format timestamp
const formatTime = (ts: string) => {
  if (!ts) return "-";
  return new Date(Number(ts)).toLocaleTimeString();
};
</script>

<template>
  <div class="flex flex-col gap-6">
    <!-- Header with Refresh -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-xl font-bold">System Monitor</h2>
        <p class="text-sm text-muted">
          Real-time job processing and thread pool statistics
        </p>
      </div>
      <div class="flex items-center gap-2">
        <span class="text-xs text-muted" v-if="metrics.timestamp"
          >Last updated: {{ formatTime(metrics.timestamp) }}</span
        >
        <NButton
          leading="i-lucide-refresh-cw"
          size="sm"
          btn="ghost"
          :loading="loading"
          @click="fetchData"
        />
      </div>
    </div>

    <div v-if="monitorData" class="space-y-6">
      <!-- Job Overview Section -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <NCard
          title="Total Jobs"
          class="transition-all hover:scale-[1.02]"
          card="outline-gray"
          :una="{
            cardContent: 'flex items-baseline gap-2',
          }"
        >
          <span class="text-3xl font-bold">{{
            jobOverview.totalJobCount || 0
          }}</span>
          <span class="text-xs text-muted">tasks</span>
          <div
            class="absolute top-4 right-4 p-2 rounded-full bg-gray-100 dark:bg-gray-800"
          >
            <NIcon name="i-lucide-layers" class="w-5 h-5 text-gray-500" />
          </div>
        </NCard>

        <NCard
          title="Running"
          class="transition-all hover:scale-[1.02]"
          card="soft-info"
          :una="{
            cardContent: 'flex items-baseline gap-2',
          }"
        >
          <span class="text-3xl font-bold text-info">{{
            jobOverview.runningJobCount || 0
          }}</span>
          <span class="text-xs text-info/80">active</span>
          <div class="absolute top-4 right-4 p-2 rounded-full bg-info/10">
            <NIcon
              name="i-lucide-loader-2"
              class="w-5 h-5 text-info animate-spin"
            />
          </div>
        </NCard>

        <NCard
          title="Completed"
          class="transition-all hover:scale-[1.02]"
          card="soft-success"
          :una="{
            cardContent: 'flex items-baseline gap-2',
          }"
        >
          <span class="text-3xl font-bold text-success">{{
            jobOverview.successJobCount || 0
          }}</span>
          <span class="text-xs text-success/80">jobs</span>
          <div class="absolute top-4 right-4 p-2 rounded-full bg-success/10">
            <NIcon name="i-lucide-check-circle" class="w-5 h-5 text-success" />
          </div>
        </NCard>

        <NCard
          title="Failed"
          class="transition-all hover:scale-[1.02]"
          card="soft-error"
          :una="{
            cardContent: 'flex items-baseline gap-2',
          }"
        >
          <span class="text-3xl font-bold text-error">{{
            jobOverview.failedJobCount || 0
          }}</span>
          <span class="text-xs text-error/80">errors</span>
          <div class="absolute top-4 right-4 p-2 rounded-full bg-error/10">
            <NIcon name="i-lucide-alert-octagon" class="w-5 h-5 text-error" />
          </div>
        </NCard>
      </div>

      <!-- Thread Pool Metrics -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- Pool Status -->
        <NCard
          title="Thread Pool Status"
          icon="i-lucide-cpu"
          card="outline-gray"
          class="h-full"
        >
          <div class="grid grid-cols-2 gap-4">
            <div class="p-3 rounded-lg border border-border bg-muted/20">
              <div class="text-sm text-muted mb-1">Active Threads</div>
              <div class="text-2xl font-semibold">
                {{ metrics.activeCount || 0 }}
              </div>
            </div>
            <div class="p-3 rounded-lg border border-border bg-muted/20">
              <div class="text-sm text-muted mb-1">Queue Size</div>
              <div class="text-2xl font-semibold">
                {{ metrics.queueSize || 0 }}
              </div>
            </div>
            <div class="p-3 rounded-lg border border-border bg-muted/20">
              <div class="text-sm text-muted mb-1">Current Pool Size</div>
              <div class="text-2xl font-semibold">
                {{ metrics.poolSize || 0 }}
              </div>
            </div>
            <div class="p-3 rounded-lg border border-border bg-muted/20">
              <div class="text-sm text-muted mb-1">Largest Pool Size</div>
              <div class="text-2xl font-semibold">
                {{ metrics.largestPoolSize || 0 }}
              </div>
            </div>
          </div>
        </NCard>

        <!-- Configuration & Stats -->
        <NCard
          title="Configuration & History"
          icon="i-lucide-settings"
          card="outline-gray"
          class="h-full"
        >
          <ul class="space-y-4">
            <li
              class="flex items-center justify-between pb-3 border-b border-border/50"
            >
              <div class="flex items-center gap-2">
                <NIcon name="i-lucide-box" class="text-primary" />
                <span>Core Pool Size</span>
              </div>
              <span class="font-mono font-bold">{{
                metrics.corePoolSize || 0
              }}</span>
            </li>
            <li
              class="flex items-center justify-between pb-3 border-b border-border/50"
            >
              <div class="flex items-center gap-2">
                <NIcon name="i-lucide-maximize" class="text-primary" />
                <span>Max Pool Size</span>
              </div>
              <span class="font-mono font-bold">{{
                metrics.maxPoolSize || 0
              }}</span>
            </li>
            <li class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <NIcon name="i-lucide-history" class="text-primary" />
                <span>Total Completed Tasks</span>
              </div>
              <span class="font-mono font-bold">{{
                metrics.completedTaskCount || 0
              }}</span>
            </li>
          </ul>
        </NCard>
      </div>
    </div>

    <!-- Loading/Empty State -->
    <div
      v-else-if="loading && !monitorData"
      class="flex items-center justify-center p-20"
    >
      <NIcon
        name="i-lucide-loader-2"
        class="w-8 h-8 animate-spin text-primary"
      />
    </div>

    <div v-else class="text-center py-10 text-muted">No data available</div>
  </div>
</template>

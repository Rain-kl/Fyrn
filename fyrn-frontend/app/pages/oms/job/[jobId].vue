<script setup lang="ts">
const route = useRoute();
const jobId = computed(() => route.params.jobId as string);

useHead({
  title: `任务详情 - ${jobId.value}`,
});
</script>

<template>
  <div class="h-full w-full flex flex-col gap-4">
    <div class="flex items-center justify-between">
      <h1 class="text-xl font-bold">任务详情</h1>
      <NButton
        label="返回列表"
        btn="link"
        leading="i-lucide-arrow-left"
        to="/oms/job"
      />
    </div>
    <NCard
      class="flex-1 overflow-hidden"
      :una="{
        cardContent: 'p-4 h-full',
      }"
    >
      <OmsJobDetail :jobId="jobId" />
    </NCard>
  </div>
</template>

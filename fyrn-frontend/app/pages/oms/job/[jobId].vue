<script setup lang="ts">
const route = useRoute();
const jobId = computed(() => route.params.jobId as string);

useHead({
  title: `任务详情 - ${jobId.value}`,
});
</script>

<template>
  <div class="h-full w-full">
    <div class="mb-4">
      <h1 class="text-xl font-bold">任务详情</h1>
    </div>
    <div class="bg-white rounded-lg shadow-sm h-[calc(100vh-180px)]">
      <OmsJobDetail :jobId="jobId" />
    </div>
  </div>
</template>

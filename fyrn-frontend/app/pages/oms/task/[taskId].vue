<script setup lang="ts">
const route = useRoute();
const taskId = computed(() => route.params.taskId as string);

useHead({
  title: `任务详情 - ${taskId.value}`,
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
        to="/oms/task"
      />
    </div>
    <NCard
      class="flex-1 overflow-hidden"
      :una="{
        cardContent: 'p-4 h-full',
      }"
    >
      <ModulesOmsTaskDetail :taskId="taskId" />
    </NCard>
  </div>
</template>

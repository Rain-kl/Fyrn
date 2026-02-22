<template>
  <div
    class="workbench-overview-grid grid grid-cols-1 gap-6 h-full min-h-[700px]"
  >
    <!-- Column 1 -->
    <div class="flex flex-col gap-6 h-full">
      <ModulesWorkbenchBlock title="任务待办">
        <ModulesWorkbenchTodoOverviewPanel mode="task" />
      </ModulesWorkbenchBlock>
      <ModulesWorkbenchBlock title="笔记" class="flex-1 min-h-[250px]">
        <div class="space-y-3">
          <NSkeleton class="h-4 w-3/4" />
          <NSkeleton class="h-4 w-full" />
          <NSkeleton class="h-4 w-5/6" />
          <NSkeleton class="h-4 w-2/3" />
        </div>
      </ModulesWorkbenchBlock>
    </div>

    <!-- Column 2 -->
    <div class="flex flex-col gap-6 h-full">
      <ModulesWorkbenchBlock title="应用中心" class="flex-1 min-h-[180px]">
        <div class="grid grid-cols-4 gap-4">
          <NSkeleton v-for="i in 4" :key="i" class="aspect-square rounded-xl" />
        </div>
      </ModulesWorkbenchBlock>
      <ModulesWorkbenchBlock title="提醒">
        <ModulesWorkbenchTodoOverviewPanel mode="reminder" />
      </ModulesWorkbenchBlock>
      <ModulesWorkbenchBlock title="待分类" class="flex-[1.5] min-h-[240px]">
        <div class="space-y-3">
          <NSkeleton class="h-10 w-full rounded" />
          <NSkeleton class="h-10 w-full rounded" />
          <NSkeleton class="h-10 w-full rounded" />
        </div>
      </ModulesWorkbenchBlock>
    </div>
  </div>
</template>

<style scoped>
@media (min-width: 768px) {
  .workbench-overview-grid {
    grid-template-columns: 3fr 2fr;
  }
}
</style>

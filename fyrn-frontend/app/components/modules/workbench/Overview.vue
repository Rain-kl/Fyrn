<template>
  <div
    class="workbench-overview-grid grid grid-cols-1 gap-6 h-full min-h-[700px]"
  >
    <!-- Column 1 -->
    <div class="flex flex-col gap-6 h-full">
      <ModulesWorkbenchBlock title="今日待办" class="flex-[2] min-h-[350px]">
        <ModulesWorkbenchTodoOverviewPanel class="h-full" />
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
      <ModulesWorkbenchBlock title="即将开始" class="flex-1 min-h-[180px]">
        <div class="flex items-center space-x-4 mb-4" v-for="i in 2" :key="i">
          <NSkeleton rounded="full" class="h-10 w-10" />
          <div class="space-y-2 flex-1">
            <NSkeleton class="h-4 w-1/2" />
            <NSkeleton class="h-3 w-1/3" />
          </div>
        </div>
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

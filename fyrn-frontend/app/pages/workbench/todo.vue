<script setup lang="ts">
import { ref } from "vue";

definePageMeta({
  layout: "workbench",
});

const todoListPanelRef = ref<{
  refresh: () => Promise<void>;
  openAddModal: () => void;
} | null>(null);

const handleRefresh = () => {
  todoListPanelRef.value?.refresh();
};

const handleOpenAdd = () => {
  todoListPanelRef.value?.openAddModal();
};
</script>

<template>
  <div class="h-full flex flex-col gap-4 w-full">
    <div class="flex items-center justify-between pb-4 border-b shrink-0">
      <div>
        <h2 class="text-2xl font-semibold tracking-tight">任务待办</h2>
        <p class="text-sm text-muted-foreground mt-1">管理和跟踪您的任务与提醒</p>
      </div>
      <div class="flex gap-2">
        <NButton
          @click="handleRefresh"
          size="sm"
          btn="outline-gray"
          leading="i-radix-icons-update"
        >
          刷新
        </NButton>
        <NButton
          @click="handleOpenAdd"
          size="sm"
          btn="solid-primary"
          leading="i-lucide-plus"
        >
          添加任务
        </NButton>
      </div>
    </div>

    <ModulesWorkbenchTodoListPanel ref="todoListPanelRef" />
  </div>
</template>

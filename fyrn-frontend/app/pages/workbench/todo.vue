<script setup lang="ts">
definePageMeta({
  layout: "workbench",
});
</script>

<template>
  <ModulesWorkbenchBlock title="待办" class="h-full min-h-[calc(100vh-10rem)]">
    <div
      class="flex flex-col items-center justify-center h-full text-muted-foreground space-y-4 opacity-70"
    >
      <NIcon name="i-lucide-check-square" class="size-12" />
      <p class="text-lg">待办页面开发中</p>
    </div>
  </ModulesWorkbenchBlock>
</template>

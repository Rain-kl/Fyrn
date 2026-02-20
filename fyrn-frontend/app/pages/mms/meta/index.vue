<script setup lang="ts">
definePageMeta({
  layout: "default",
});
</script>

<template>
  <div class="container mx-auto py-8">
    <div class="mb-6">
      <h1 class="text-3xl font-bold">小说元数据管理</h1>
      <p class="mt-2 text-muted">管理小说的元数据信息</p>
    </div>

    <ModulesMmsMetaListManagement />
  </div>
</template>

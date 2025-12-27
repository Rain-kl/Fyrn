<script setup lang="ts">
const items = ref([
  {
    value: 'oss',
    name: 'OSS管理',
    _tabsTrigger: {
      leading: 'i-lucide-cloud',
    },
  },
  {
    value: 'local',
    name: '本地文件管理',
    _tabsTrigger: {
      leading: 'i-lucide-hard-drive',
    },
  },
])
</script>

<template>
  <NTabs
    :items="items"
    default-value="oss"
    :_tabs-list="{
      class: 'grid grid-cols-2 w-full max-w-md',
    }"
    :_tabs-content="{
      class: 'py-6 w-full',
    }"
  >
    <template #content="{ item }">
      <div v-if="item.value === 'oss'">
        <MmsOosFileManagement />
      </div>

      <div v-if="item.value === 'local'">
        <MmsLocalFileManagement />
      </div>
    </template>
  </NTabs>
</template>

<script setup lang="ts">
const items = ref([
  {
    value: "jobList",
    name: "Job List",
    _tabsTrigger: {
      leading: "i-lucide-list-checks",
    },
  },
  {
    value: "threadStatus",
    name: "Thread Status",
    _tabsTrigger: {
      leading: "i-lucide-activity",
    },
  },
]);
</script>

<template>
  <NTabs
    :items="items"
    default-value="jobList"
    :_tabs-list="{
      class: 'grid grid-cols-2 w-full max-w-md',
    }"
    :_tabs-content="{
      class: 'py-6 w-full',
    }"
  >
    <template #content="{ item }">
      <div v-if="item.value === 'jobList'">
        <OmsJobListManagement />
      </div>

      <div v-if="item.value === 'threadStatus'">
        <!-- Thread Status 暂时为空 -->
        <div class="text-center text-muted py-10">Thread Status 功能待实现</div>
      </div>
    </template>
  </NTabs>
</template>

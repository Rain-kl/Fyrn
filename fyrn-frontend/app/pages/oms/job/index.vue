<script setup lang="ts">
const items = ref([

  {
    value: "threadStatus",
    name: "Thread Status",
    _tabsTrigger: {
      leading: "i-lucide-activity",
    },
  },
  {
    value: "jobList",
    name: "任务列表",
    _tabsTrigger: {
      leading: "i-lucide-list-checks",
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

      <div v-if="item.value === 'threadStatus'">
        <OmsThreadStatus/>
      </div>

      <div v-if="item.value === 'jobList'">
        <OmsJobListManagement/>
      </div>

    </template>
  </NTabs>
</template>

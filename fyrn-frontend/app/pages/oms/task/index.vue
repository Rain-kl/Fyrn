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
    value: "taskList",
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
      default-value="taskList"
      :_tabs-list="{
      class: 'grid grid-cols-2 w-full max-w-md',
    }"
      :_tabs-content="{
      class: 'py-6 w-full',
    }"
  >
    <template #content="{ item }">

      <div v-if="item.value === 'threadStatus'">
        <ModulesOmsThreadStatus/>
      </div>

      <div v-if="item.value === 'taskList'">
        <ModulesOmsTaskListManagement/>
      </div>

    </template>
  </NTabs>
</template>

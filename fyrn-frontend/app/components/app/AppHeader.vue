<script setup lang="ts">
import ColorMode from "~/components/button/ColorMode.vue";

const items = [
  {
    label: '物料管理',
    leading: 'i-lucide:book-open',
    items: [
      {
        label: '物料管理系统',
        description: '物料信息的维护与管理',
        to: '/mms/novels',
      },
      {
        label: '文件管理系统',
        description: 'OOS文件存储同步与管理',
        to: '/mms/files',
      }
    ],
  },
  {
    label: '运维管理系统',
    leading: 'i-lucide:box',
    items: [
      {
        label: '系统参数管理',
        description: '系统参数的维护与配置',
        to: '/oms/ppc',
      },
      {
        label: '任务管理系统',
        description: '进程任务的状态与监控',
        to: '/oms/job',
      },
      {
        label: '用户管理系统',
        description: '系统用户的维护与管理',
        to: '/oms/user',
      }
    ],
  },
  {
    label: 'Help',
    leading: 'i-lucide-circle-help',
    disabled: true,
  },
]
</script>

<template>
  <header class="app-header">
    <div>
      <NNavigationMenu :items indicator />
    </div>

    <div class="app-header__actions">
      <ColorMode />
      <NThemeSwitcher />
    </div>
  </header>
</template>

<style scoped>
.app-header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.8rem;

  /* spacing */
  padding: 0.65rem 0.5rem;
  margin-bottom: 1rem;

  /* visual */
  /* border-bottom: 1px solid rgba(0, 0, 0, 0.08); */
}

@media (min-width: 768px) {
  .app-header {
    padding: 0.7rem 0.1rem;
  }
}


.app-header__actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.5rem;
}
</style>

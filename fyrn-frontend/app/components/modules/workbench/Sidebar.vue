<script setup lang="ts">
const route = useRoute();
const sidebar = useSidebar();

const data = {
  navMain: [
    { title: "总览", url: "/", icon: "i-lucide-layout-dashboard" },
    { title: "待办", url: "/workbench/todo", icon: "i-lucide-check-square" },
    { title: "日历", url: "/workbench/calendar", icon: "i-lucide-calendar" },
    { title: "笔记", url: "/workbench/notes", icon: "i-lucide-book" },
    {
      title: "数据看板",
      url: "/workbench/dashboard",
      icon: "i-lucide-bar-chart-2",
    },
    {
      title: "设置",
      url: "/workbench/settings",
      icon: "i-lucide-settings",
    },
  ],
};
</script>

<template>
  <NSidebar collapsible="icon" class="border-r h-full !h-full __fyrn_sidebar">
    <NSidebarContent>
      <NSidebarGroup>
        <NSidebarGroupContent class="px-1.5 md:px-0">
          <NSidebarMenu>
            <NSidebarMenuItem v-for="item in data.navMain" :key="item.title">
              <NSidebarMenuButton
                :tooltip="item.title"
                :is-active="route.path === item.url"
                as-child
              >
                <NLink :to="item.url">
                  <NIcon :name="item.icon" />
                  <span>{{ item.title }}</span>
                </NLink>
              </NSidebarMenuButton>
            </NSidebarMenuItem>
          </NSidebarMenu>
        </NSidebarGroupContent>
      </NSidebarGroup>
    </NSidebarContent>
    <NSidebarFooter class="p-2 border-t mt-auto">
      <NSidebarMenu>
        <NSidebarMenuItem>
          <NSidebarMenuButton
            @click="sidebar.toggleSidebar"
            class="w-full"
            tooltip="折叠/展开"
          >
            <NIcon
              :name="
                sidebar.state.value === 'expanded'
                  ? 'i-lucide-panel-left-close'
                  : 'i-lucide-panel-left-open'
              "
            />
            <span>折叠/展开</span>
          </NSidebarMenuButton>
        </NSidebarMenuItem>
      </NSidebarMenu>
    </NSidebarFooter>
  </NSidebar>
</template>

<script setup lang="ts">
import { useApi } from "~/api/useApi";

const { OmsAuthControllerApi } = useApi();
const router = useRouter();
const userInfo = useState<any>("user", () => null); // Global state for user
const isUserMenuOpen = ref(false);

function toggleUserMenu() {
  isUserMenuOpen.value = !isUserMenuOpen.value;
}

async function handleLogout() {
  try {
    await OmsAuthControllerApi.omsAuthLogoutPost();
  } catch (e) {
    console.error("Logout error:", e);
  } finally {
    isUserMenuOpen.value = false;
    userInfo.value = null;
    router.push("/oms/auth/login");
  }
}

// Fetch user info on mount if not present? Or maybe middleware does it?
// For now, let's assume we fetch it if missing.
onMounted(async () => {
  if (!userInfo.value) {
    try {
      const res = await OmsAuthControllerApi.omsAuthInfoGet();
      if (res.code === 200) {
        userInfo.value = res.data;
      }
    } catch (e) {
      // Not logged in or error
    }
  }
});

const items = [
  {
    label: "工作台",
    leading: "i-lucide:layout-dashboard",
    to: "/",
  },
  {
    label: "物料管理",
    leading: "i-lucide:book-open",
    items: [
      {
        label: "物料管理系统",
        description: "物料信息的维护与管理",
        to: "/mms/novels",
      },

      {
        label: "元数据管理系统",
        description: "元数据信息的维护与管理",
        to: "/mms/meta",
      },
      {
        label: "文件管理系统",
        description: "OOS文件存储同步与管理",
        to: "/mms/files",
      },
    ],
  },
  {
    label: "运维管理系统",
    leading: "i-lucide:box",
    items: [
      {
        label: "系统参数管理",
        description: "系统参数的维护与配置",
        to: "/oms/ppc",
      },
      {
        label: "任务管理系统",
        description: "进程任务的状态与监控",
        to: "/oms/task",
      },
      {
        label: "用户管理系统",
        description: "系统用户的维护与管理",
        to: "/oms/user",
      },
    ],
  },
  {
    label: "Help",
    leading: "i-lucide-circle-help",
    disabled: true,
  },
];
</script>

<template>
  <header class="app-header">
    <div>
      <NNavigationMenu :items="items" indicator />
    </div>

    <div class="app-header__actions">
      <div v-if="userInfo" class="relative">
        <button
          @click="toggleUserMenu"
          class="flex items-center gap-2 px-3 py-1.5 text-sm font-medium rounded-md hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors focus:outline-none"
        >
          <span class="i-lucide-user text-base"></span>
          <span>{{ userInfo.nickname || userInfo.username }}</span>
          <span class="i-lucide-chevron-down text-xs opacity-50"></span>
        </button>

        <!-- Backdrop -->
        <div
          v-if="isUserMenuOpen"
          @click="isUserMenuOpen = false"
          class="fixed inset-0 z-10 cursor-default"
        ></div>

        <!-- Dropdown Menu -->
        <div
          v-if="isUserMenuOpen"
          class="absolute right-0 z-20 mt-2 w-48 origin-top-right rounded-md bg-white dark:bg-gray-800 py-1 shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none border border-gray-200 dark:border-gray-700"
        >
          <NLink
            to="/oms/user/me"
            class="block px-4 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700 decoration-none"
            @click="isUserMenuOpen = false"
          >
            <div class="flex items-center gap-2">
              <span class="i-lucide-user-circle"></span>
              Profile
            </div>
          </NLink>
          <button
            class="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700"
            @click="handleLogout"
          >
            <div class="flex items-center gap-2">
              <span class="i-lucide-log-out"></span>
              Logout
            </div>
          </button>
        </div>
      </div>
      <div v-else>
        <NButton to="/oms/auth/login" label="Login" btn="ghost" size="sm" />
      </div>

      <CommonButtonColorMode />
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

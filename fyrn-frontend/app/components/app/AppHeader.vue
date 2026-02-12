<script setup lang="ts">
import ColorMode from "~/components/button/ColorMode.vue";
import { useApi } from '~/api/useApi'

const { OmsAuthControllerApi } = useApi()
const userInfo = useState<any>('user', () => null) // Global state for user

// Fetch user info on mount if not present? Or maybe middleware does it?
// For now, let's assume we fetch it if missing.
onMounted(async () => {
    if (!userInfo.value) {
        try {
            const res = await OmsAuthControllerApi.omsAuthInfoGet()
            if (res.code === 200) {
                userInfo.value = res.data
            }
        } catch (e) {
            // Not logged in or error
        }
    }
})

const userMenuItems = [
  [{
    label: 'Profile',
    to: '/oms/user',
    icon: 'i-lucide-user-circle'
  }],
  [{
    label: 'Logout',
    icon: 'i-lucide-log-out',
    click: () => {
        // Handle logout
        userInfo.value = null
        useRouter().push('/oms/auth/login')
    }
  }]
]

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
        label: '元数据管理系统',
        description: '元数据信息的维护与管理',
        to: '/mms/meta',
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
        to: '/oms/task',
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
      <NNavigationMenu :items="items" indicator />
    </div>

    <div class="app-header__actions">
      <div v-if="userInfo" class="flex items-center gap-2">
         <NDropdown :items="userMenuItems">
            <NButton 
              :label="userInfo.nickname || userInfo.username" 
              leading="i-lucide-user" 
              btn="ghost"
            />
         </NDropdown>
      </div>
      <div v-else>
         <NButton to="/oms/auth/login" label="Login" btn="ghost" size="sm" />
      </div>

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

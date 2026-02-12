<script setup lang="ts">
import { useApi } from '~/api/useApi'
import { formatToYMD } from "~/utils/date";

const { OmsAuthControllerApi, OmsUserControllerApi } = useApi()
const { toast } = useToast()

const userInfo = ref<any>(null)
const loading = ref(true)
const activeTab = ref('profile')

const fetchUserInfo = async () => {
    loading.value = true
    try {
        const res = await OmsAuthControllerApi.omsAuthInfoGet()
        if (res.code === 200) {
            userInfo.value = res.data
        }
    } catch (error) {
        console.error(error)
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchUserInfo()
})

const tabs = [
    { id: 'profile', label: '个人资料', icon: 'i-lucide-user' },
    { id: 'security', label: '账号安全', icon: 'i-lucide-shield-check' },
    // { id: 'settings', label: '系统设置', icon: 'i-lucide-settings' },
]

// Profile Form
const profileForm = reactive({
    nickname: '',
    email: '',
    phone: '',
})

watch(userInfo, (val) => {
    if (val) {
        profileForm.nickname = val.nickname
        profileForm.email = val.email
        profileForm.phone = val.phone
    }
})

const updateProfileLoading = ref(false)
const handleUpdateProfile = async () => {
    if (!userInfo.value) return
    
    updateProfileLoading.value = true
    try {
        // Assuming OmsUserControllerApi.omsUserUpdatePost is available for update
        const res = await OmsUserControllerApi.omsUserUpdatePost({
            userUpdateInput: {
                userId: userInfo.value.userId,
                nickname: profileForm.nickname,
                email: profileForm.email,
                phone: profileForm.phone
            }
        })
        if (res.code === 200) {
            toast({
                title: '成功',
                description: '个人资料更新成功',
                toast: 'soft-success',
            })
            // Refresh global user state if necessary
            const globalUser = useState('user')
            if (globalUser.value) {
                globalUser.value = { ...globalUser.value, ...profileForm }
            }
            fetchUserInfo() 
        }
    } catch (e) {
        console.error(e)
    } finally {
        updateProfileLoading.value = false
    }
}

</script>

<template>
  <div class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
    <div class="px-4 py-6 sm:px-0">
        <div class="flex flex-col md:flex-row gap-6">
            <!-- Sidebar -->
            <div class="w-full md:w-64 shrink-0">
                <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-4 sticky top-6">
                    <div class="flex items-center gap-3 mb-6 px-2">
                         <div class="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                            <span class="i-lucide-user text-2xl"></span>
                        </div>
                        <div class="overflow-hidden">
                            <h3 class="font-bold text-lg truncate">{{ userInfo?.nickname || 'User' }}</h3>
                            <p class="text-xs text-gray-500 truncate">{{ userInfo?.username }}</p>
                        </div>
                    </div>
                    
                    <nav class="space-y-1">
                        <button
                            v-for="tab in tabs"
                            :key="tab.id"
                            @click="activeTab = tab.id"
                            class="w-full flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-md transition-colors"
                            :class="[
                                activeTab === tab.id 
                                ? 'bg-primary/10 text-primary' 
                                : 'text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700'
                            ]"
                        >
                            <span :class="tab.icon"></span>
                            {{ tab.label }}
                        </button>
                    </nav>
                </div>
            </div>

            <!-- Content Area -->
            <div class="flex-1 bg-white dark:bg-gray-800 shadow rounded-lg p-6 min-h-[500px]">
                <div v-if="loading" class="flex justify-center items-center h-full">
                    <span class="i-lucide-loader-2 animate-spin text-3xl text-gray-400"></span>
                </div>

                <div v-else>
                     <!-- Profile Section -->
                    <div v-if="activeTab === 'profile'" class="space-y-6">
                        <div>
                            <h2 class="text-xl font-bold">个人资料</h2>
                            <p class="text-sm text-gray-500 mt-1">管理您的基本信息</p>
                        </div>
                        <div class="border-t border-gray-100 dark:border-gray-700 my-4"></div>

                        <form @submit.prevent="handleUpdateProfile" class="max-w-2xl space-y-6">
                             <NFormGroup label="用户名">
                                <NInput :model-value="userInfo?.username" disabled class="bg-gray-50 dark:bg-gray-900" />
                            </NFormGroup>

                            <NFormGroup label="昵称">
                                <NInput v-model="profileForm.nickname" placeholder="请输入昵称" />
                            </NFormGroup>
                            
                            <NFormGroup label="邮箱">
                                <NInput v-model="profileForm.email" placeholder="xxx@example.com" />
                            </NFormGroup>

                             <NFormGroup label="手机号">
                                <NInput v-model="profileForm.phone" placeholder="请输入您的手机号" />
                            </NFormGroup>

                             <NFormGroup label="注册时间">
                                <div class="px-3 py-2 text-sm text-gray-700 dark:text-gray-300">
                                    {{ userInfo?.createTime ? formatToYMD(userInfo.createTime) : '-' }}
                                </div>
                            </NFormGroup>

                            <div class="pt-4">
                                <NButton 
                                    type="submit" 
                                    label="保存修改" 
                                    :loading="updateProfileLoading"
                                    class="w-full sm:w-auto"
                                />
                            </div>
                        </form>
                    </div>

                    <!-- Security Section -->
                    <div v-else-if="activeTab === 'security'" class="space-y-6">
                         <div>
                            <h2 class="text-xl font-bold">账号安全</h2>
                            <p class="text-sm text-gray-500 mt-1">修改密码及安全设置</p>
                        </div>
                        <div class="border-t border-gray-100 dark:border-gray-700 my-4"></div>
                        
                        <div class="space-y-4">
                            <!-- Password Change Placeholder -->
                            <div class="flex items-center justify-between p-4 border rounded-lg border-gray-100 dark:border-gray-700">
                                <div>
                                    <h4 class="font-medium">登录密码</h4>
                                    <p class="text-sm text-gray-500">建议定期更换密码以保护账号安全</p>
                                </div>
                                <NButton label="修改密码" btn="outline" size="sm" />
                            </div>
                             <div class="flex items-center justify-between p-4 border rounded-lg border-gray-100 dark:border-gray-700">
                                <div>
                                    <h4 class="font-medium">账号状态</h4>
                                    <p class="text-sm text-gray-500">
                                        <span v-if="userInfo?.status === 1" class="text-green-600 flex items-center gap-1">
                                            <span class="i-lucide-check-circle size-4"></span> 正常
                                        </span>
                                        <span v-else class="text-red-600">异常</span>
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

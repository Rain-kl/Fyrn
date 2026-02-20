<script setup lang="ts">
import { useApi } from '~/api/useApi'

definePageMeta({
  layout: 'auth'
})

const { OmsAuthControllerApi } = useApi()
const router = useRouter()
const { toast } = useToast()

const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
})

async function handleLogin() {
  if (!form.username || !form.password) {
    toast({
      title: 'Validation Error',
      description: 'Please fill in all fields',
      toast: 'soft-warning',
    })
    return
  }

  loading.value = true
  try {
    const res = await OmsAuthControllerApi.omsAuthLoginPost({
        username: form.username,
        password: form.password
    })
    
    if (res.code === 200) {
      toast({
        title: 'Success',
        description: 'Login successful',
        toast: 'soft-success',
      })
      // Store user info if needed, e.g. useState or Pinia
      const user = useState('user')
      user.value = res.data
      await router.push('/')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="bg-white dark:bg-gray-800 py-8 px-4 shadow sm:rounded-lg sm:px-10">
    <div class="sm:mx-auto sm:w-full sm:max-w-md mb-6">
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900 dark:text-white">
        Sign in to your account
      </h2>
    </div>

    <form class="space-y-6" @submit.prevent="handleLogin">
      <NFormGroup label="Username">
        <NInput 
          v-model="form.username" 
          placeholder="Enter your username"
          leading="i-lucide-user"
        />
      </NFormGroup>

      <NFormGroup label="Password">
        <NInput 
            v-model="form.password" 
            type="password" 
            placeholder="Enter your password"
            leading="i-lucide-lock"
        />
      </NFormGroup>

      <div>
        <NButton
          type="submit"
          class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          :loading="loading"
          label="Sign in"
        />
      </div>
      
      <div class="flex items-center justify-between">
          <div class="text-sm">
            <NLink to="/oms/auth/register" class="font-medium text-indigo-600 hover:text-indigo-500">
              Don't have an account? Register
            </NLink>
          </div>
      </div>
    </form>
  </div>
</template>

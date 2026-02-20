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
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: ''
})

async function handleRegister() {
  if (!form.username || !form.password || !form.confirmPassword) {
     toast({
      title: 'Validation Error',
      description: 'Please fill in required fields',
      toast: 'soft-warning',
    })
    return
  }

  if (form.password !== form.confirmPassword) {
    toast({
      title: 'Validation Error',
      description: 'Passwords do not match',
      toast: 'soft-warning',
    })
    return
  }

  loading.value = true
  try {
    const res = await OmsAuthControllerApi.omsAuthRegisterPost({
        username: form.username,
        password: form.password,
        nickname: form.nickname,
        email: form.email,
        phone: form.phone
    })
    
    if (res.code === 200) {
      toast({
        title: 'Success',
        description: 'Registration successful! Please login.',
        toast: 'soft-success',
      })
      await router.push('/oms/auth/login')
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
        Create a new account
      </h2>
    </div>

    <form class="space-y-6" @submit.prevent="handleRegister">
      <NFormGroup label="Username" required>
        <NInput v-model="form.username" placeholder="Choose a username" leading="i-lucide-user"/>
      </NFormGroup>

      <NFormGroup label="Nickname">
        <NInput v-model="form.nickname" placeholder="Your nickname" leading="i-lucide-smile"/>
      </NFormGroup>

      <NFormGroup label="Email">
        <NInput v-model="form.email" type="email" placeholder="you@example.com" leading="i-lucide-mail"/>
      </NFormGroup>
      
       <NFormGroup label="Phone">
        <NInput v-model="form.phone" placeholder="Your phone number" leading="i-lucide-phone"/>
      </NFormGroup>

      <NFormGroup label="Password" required>
        <NInput v-model="form.password" type="password" placeholder="Choose a password" leading="i-lucide-lock"/>
      </NFormGroup>

      <NFormGroup label="Confirm Password" required>
        <NInput v-model="form.confirmPassword" type="password" placeholder="Confirm your password" leading="i-lucide-lock"/>
      </NFormGroup>

      <div>
        <NButton
          type="submit"
          class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          :loading="loading"
          label="Register"
        />
      </div>

      <div class="flex items-center justify-between">
          <div class="text-sm">
            <NLink to="/oms/auth/login" class="font-medium text-indigo-600 hover:text-indigo-500">
              Already have an account? Sign in
            </NLink>
          </div>
      </div>
    </form>
  </div>
</template>

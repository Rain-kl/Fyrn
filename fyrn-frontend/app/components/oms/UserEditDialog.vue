<script setup lang="ts">
import type { UserInfoVo, UserCreateInput, UserUpdateInput } from '~/api/models'

const props = defineProps<{
  user: UserInfoVo | null
  open: boolean
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'save', value: UserCreateInput | UserUpdateInput): void
}>()

const form = ref<UserCreateInput & UserUpdateInput>({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  status: 1,
  password: '',
})

watch(() => props.user, (newVal) => {
  if (newVal) {
    form.value = {
      ...newVal,
      password: '', // 密码通常不回显
    } as any
  } else {
    form.value = {
      username: '',
      nickname: '',
      email: '',
      phone: '',
      status: 1,
      password: '',
    }
  }
}, { immediate: true })

const handleSave = () => {
  const payload = { ...form.value }
  if (!props.user) {
    // 新增模式，如果密码为空则不传（或者根据后端要求处理）
    if (!payload.password) delete payload.password
  } else {
    // 编辑模式
    payload.userId = props.user.userId
    // 如果密码为空，则不修改密码
    if (!payload.password) delete payload.password
  }
  emit('save', payload)
  emit('update:open', false)
}

const statusOptions = [
  { label: '禁用', value: 0 },
  { label: '正常', value: 1 },
  { label: '锁定', value: 2 },
]
</script>

<template>
  <NDialog
    :open="open"
    @update:open="emit('update:open', $event)"
    :title="user ? '编辑用户' : '新增用户'"
    :description="(user ? '修改用户的详细信息。' : '填写新用户的信息。') + '完成后点击保存。'"
    class="sm:max-w-[425px]"
    :una="{
      dialogOverlay: 'bg-black/20 backdrop-blur-sm',
    }"
  >
    <div class="flex flex-col gap-4 py-4">
      <NFormGroup label="用户名">
        <NInput v-model="form.username" placeholder="请输入用户名" :disabled="!!user" />
      </NFormGroup>
      <NFormGroup label="昵称">
        <NInput v-model="form.nickname" placeholder="请输入昵称" />
      </NFormGroup>
      <NFormGroup label="密码">
        <NInput v-model="form.password" type="password" :placeholder="user ? '留空则不修改' : '请输入密码'" />
      </NFormGroup>
      <NFormGroup label="邮箱">
        <NInput v-model="form.email" placeholder="请输入邮箱" />
      </NFormGroup>
      <NFormGroup label="手机号">
        <NInput v-model="form.phone" placeholder="请输入手机号" />
      </NFormGroup>
      <NFormGroup label="状态">
        <NSelect
          :items="statusOptions.map(o => o.label)"
          :model-value="statusOptions.find(o => o.value === form.status)?.label"
          @update:model-value="form.status = statusOptions.find(o => o.label === $event)?.value"
        />
      </NFormGroup>
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <NButton
          label="取消"
          btn="ghost-gray"
          @click="emit('update:open', false)"
        />
        <NButton
          label="保存"
          btn="primary"
          @click="handleSave"
        />
      </div>
    </template>
  </NDialog>
</template>

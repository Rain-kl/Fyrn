<script setup lang="ts">
import type { OmsParameter } from '~/api/models'

const props = defineProps<{
  parameter: OmsParameter | null
  open: boolean
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'save', value: OmsParameter): void
}>()

const form = ref<OmsParameter>({})

watch(() => props.parameter, (newVal) => {
  if (newVal) {
    form.value = { ...newVal }
  }
}, { immediate: true })

const handleSave = () => {
  emit('save', form.value)
  emit('update:open', false)
}
</script>

<template>
  <NAlertDialog :open="open" @update:open="emit('update:open', $event)">
    <NAlertDialogContent class="sm:max-w-[425px]">
      <NAlertDialogHeader>
        <NAlertDialogTitle>编辑系统参数</NAlertDialogTitle>
        <NAlertDialogDescription>
          修改系统参数的详细信息。完成后点击保存。
        </NAlertDialogDescription>
      </NAlertDialogHeader>

      <div class="flex flex-col gap-4 py-4">
        <NFormGroup label="参数名称">
          <NInput v-model="form.paramName" placeholder="请输入参数名称" />
        </NFormGroup>
        <NFormGroup label="参数值">
          <NInput v-model="form.paramValue" placeholder="请输入参数值" />
        </NFormGroup>
        <NFormGroup label="参数描述">
          <NInput v-model="form.paramDesc" placeholder="请输入参数描述" />
        </NFormGroup>
        <NFormGroup label="状态">
          <NSelect
            :items="['启用', '禁用']"
            :model-value="form.enabledFlag === 1 ? '启用' : '禁用'"
            @update:model-value="form.enabledFlag = $event === '启用' ? 1 : 0"
          />
        </NFormGroup>
      </div>

      <NAlertDialogFooter>
        <NAlertDialogCancel>
          取消
        </NAlertDialogCancel>
        <NAlertDialogAction @click="handleSave">
          保存修改
        </NAlertDialogAction>
      </NAlertDialogFooter>
    </NAlertDialogContent>
  </NAlertDialog>
</template>

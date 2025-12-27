<script setup lang="ts">
const props = defineProps<{
  open: boolean;
}>();

const emit = defineEmits<{
  (e: "update:open", value: boolean): void;
  (e: "confirm", size: number): void;
}>();

const syncSize = ref<number>(-1);
const unlimited = ref<boolean>(true);

// 监听 unlimited 变化，自动设置 syncSize
watch(unlimited, (newValue) => {
  if (newValue) {
    syncSize.value = -1;
  } else {
    // 取消勾选时，如果当前是 -1，设置为 0 让用户输入
    if (syncSize.value === -1) {
      syncSize.value = 0;
    }
  }
});

const handleConfirm = () => {
  emit("confirm", syncSize.value);
  emit("update:open", false);
};
</script>

<template>
  <NAlertDialog :open="open" @update:open="emit('update:open', $event)">
    <NAlertDialogContent class="sm:max-w-[425px]">
      <NAlertDialogHeader>
        <NAlertDialogTitle>同步配置</NAlertDialogTitle>
        <NAlertDialogDescription>
          设置同步到 OOS 的文件数量。默认 -1 表示无限制。
        </NAlertDialogDescription>
      </NAlertDialogHeader>

      <div class="flex flex-col gap-4 py-4">
        <div class="flex items-center gap-2">
          <NCheckbox v-model="unlimited" label="不限制同步数量" />
        </div>

        <NFormGroup v-if="!unlimited" label="同步数量">
          <NInput
            v-model.number="syncSize"
            type="number"
            placeholder="请输入同步数量"
          />
          <p class="text-xs text-muted mt-2">提示：输入要同步的文件数量</p>
        </NFormGroup>
      </div>

      <NAlertDialogFooter>
        <NAlertDialogCancel> 取消 </NAlertDialogCancel>
        <NAlertDialogAction @click="handleConfirm">
          确认同步
        </NAlertDialogAction>
      </NAlertDialogFooter>
    </NAlertDialogContent>
  </NAlertDialog>
</template>

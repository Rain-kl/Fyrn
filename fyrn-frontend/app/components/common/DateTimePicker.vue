<script setup lang="ts">
import dayjs from "dayjs";
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    modelValue?: string | Date | null;
    placeholder?: string;
    disabled?: boolean;
    withTime?: boolean;
  }>(),
  {
    withTime: false,
  },
);

const emit = defineEmits<{
  "update:modelValue": [value: string | null];
}>();

const toInputString = (value?: string | Date | null): string => {
  if (!value) return "";
  const parsed = dayjs(value);
  if (!parsed.isValid()) return "";
  return props.withTime
    ? parsed.format("YYYY-MM-DDTHH:mm")
    : parsed.format("YYYY-MM-DD");
};

const dateValue = computed({
  get: () => toInputString(props.modelValue),
  set: (val: string) => {
    emit("update:modelValue", val || null);
  },
});

const clearDate = () => {
  emit("update:modelValue", null);
};
</script>

<template>
    <div class="flex items-center gap-2">
      <div class="relative flex-1">
        <NIcon
          name="i-lucide-calendar-days"
          class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
        />
        <input
          v-model="dateValue"
          :type="withTime ? 'datetime-local' : 'date'"
          :step="withTime ? 60 : undefined"
          :disabled="disabled"
          class="h-10 w-full rounded-lg border border-input bg-background pl-9 pr-3 text-sm text-foreground outline-none transition focus:ring-2 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
        />
      </div>

      <NButton
        v-if="dateValue && !disabled"
        size="xs"
        btn="ghost-gray"
        leading="i-lucide-x"
        @click="clearDate"
      >
      </NButton>
    </div>

</template>

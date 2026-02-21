<script setup lang="ts">
import dayjs from "dayjs";
import { computed } from "vue";

const props = defineProps<{
  startValue?: string | Date | null;
  endValue?: string | Date | null;
  placeholder?: string;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:startValue": [value: string | null];
  "update:endValue": [value: string | null];
}>();

const toDateString = (value?: string | Date | null): string => {
  if (!value) return "";
  const parsed = dayjs(value);
  return parsed.isValid() ? parsed.format("YYYY-MM-DD") : "";
};

const startDate = computed({
  get: () => toDateString(props.startValue),
  set: (val: string) => {
    emit("update:startValue", val || null);
  },
});

const endDate = computed({
  get: () => toDateString(props.endValue),
  set: (val: string) => {
    emit("update:endValue", val || null);
  },
});

const minEndDate = computed(() => startDate.value || undefined);

const clearRange = () => {
  emit("update:startValue", null);
  emit("update:endValue", null);
};
</script>

<template>
    <div class="flex items-center gap-2">
      <div class="flex-1 grid grid-cols-1 md:grid-cols-[1fr_auto_1fr] items-center gap-2">
        <div class="relative">
          <NIcon
            name="i-lucide-calendar-days"
            class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
          />
          <input
            v-model="startDate"
            type="date"
            :disabled="disabled"
            class="h-10 w-full rounded-lg border border-input bg-background pl-9 pr-3 text-sm text-foreground outline-none transition focus:ring-2 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
          />
        </div>

        <div class="hidden md:flex items-center justify-center text-muted-foreground px-1">
          <NIcon name="i-lucide-arrow-right" class="size-4" />
        </div>

        <div class="relative">
          <NIcon
            name="i-lucide-calendar-check-2"
            class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
          />
          <input
            v-model="endDate"
            type="date"
            :min="minEndDate"
            :disabled="disabled"
            class="h-10 w-full rounded-lg border border-input bg-background pl-9 pr-3 text-sm text-foreground outline-none transition focus:ring-2 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
          />
        </div>
      </div>

      <NButton
        v-if="(startDate || endDate) && !disabled"
        size="xs"
        btn="ghost-gray"
        leading="i-lucide-x"
        @click="clearRange"
      >
      </NButton>
    </div>
</template>

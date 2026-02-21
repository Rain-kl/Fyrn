<script setup lang="ts">
import { computed } from "vue";
import type { WbTask } from "~/api/models/WbTask";
import dayjs from "dayjs";

const props = defineProps<{
  task: WbTask;
}>();

const emit = defineEmits<{
  (e: "update:status", task: WbTask, checked: boolean): void;
  (e: "edit", task: WbTask): void;
  (e: "delete", task: WbTask): void;
}>();

const isCompleted = computed({
  get: () => props.task.status === 2,
  set: (val) => {
    emit("update:status", props.task, val);
  },
});

const priorityMap: Record<number, { label: string; color: string }> = {
  1: { label: "低", color: "slate" },
  2: { label: "中", color: "blue" },
  3: { label: "高", color: "orange" },
  4: { label: "紧急", color: "red" },
};

const priorityInfo = computed(() => {
  const p = props.task.priority || 1;
  return priorityMap[p] ?? { label: "低", color: "slate" };
});

const isDeadlineNearOrPast = computed(() => {
  if (!props.task.deadline) return false;
  const deadline = dayjs(props.task.deadline);
  const now = dayjs();
  // Near if deadline is before today's end, or already past
  return deadline.isBefore(now.endOf("day"));
});

const deadlineFormatted = computed(() => {
  if (!props.task.deadline) return "";
  return dayjs(props.task.deadline).format("MM-DD HH:mm");
});
</script>

<template>
  <div
    class="group flex items-start gap-3 p-3 rounded-lg border border-transparent hover:border-border hover:bg-muted/30 transition-colors"
  >
    <div class="mt-0.5">
      <NCheckbox v-model="isCompleted" size="sm" />
    </div>

    <div
      class="flex-1 min-w-0 flex flex-col gap-1 cursor-pointer"
      @click="emit('edit', task)"
    >
      <div class="flex items-center gap-2">
        <span
          class="text-sm font-medium truncate"
          :class="{ 'line-through text-muted-foreground': isCompleted }"
        >
          {{ task.name }}
        </span>
        <NBadge
          :color="priorityInfo.color"
          class="text-[10px] px-1.5 py-0 shadow-none font-normal h-4 rounded-sm"
        >
          {{ priorityInfo.label }}
        </NBadge>
      </div>

      <div
        v-if="task.description || task.deadline"
        class="flex items-center gap-3 text-xs text-muted-foreground mt-0.5"
      >
        <span
          v-if="task.deadline"
          class="flex items-center gap-1"
          :class="{ 'text-error': isDeadlineNearOrPast && !isCompleted }"
        >
          <NIcon name="i-lucide-calendar-clock" class="size-3" />
          {{ deadlineFormatted }}
        </span>
        <span v-if="task.description" class="truncate opacity-75 max-w-[200px]">
          {{ task.description }}
        </span>
      </div>
    </div>

    <div
      class="opacity-0 group-hover:opacity-100 transition-opacity flex items-center gap-1 shrink-0"
    >
      <NButton
        size="xs"
        btn="ghost-primary"
        icon
        @click.stop="emit('edit', task)"
      >
        <NIcon name="i-lucide-edit-3" class="size-4" />
      </NButton>
      <NButton
        size="xs"
        btn="ghost-error"
        icon
        @click.stop="emit('delete', task)"
      >
        <NIcon name="i-lucide-trash-2" class="size-4" />
      </NButton>
    </div>
  </div>
</template>

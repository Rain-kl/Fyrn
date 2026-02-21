<script setup lang="ts">
import { ref, onMounted } from "vue";
import dayjs from "dayjs";
import type { WbTask } from "~/api/models/WbTask";
import { useApi } from "~/api/useApi";

const { WbTaskControllerApi } = useApi();
const { toast } = useToast();

const tasks = ref<WbTask[]>([]);
const loadingTasks = ref(true);
const isModalOpen = ref(false);
const selectedTask = ref<WbTask | undefined>(undefined);
const isViewMode = ref(false);
const confirmStatusOpen = ref(false);
const confirmActionObj = ref<{
  task: WbTask;
  actionTitle: string;
  targetStatus: number;
} | null>(null);

const fetchTasks = async () => {
  loadingTasks.value = true;
  try {
    const res = await WbTaskControllerApi.wbPagePost({
      wbTaskPageInput: {
        pageNo: 1,
        pageSize: 100,
      },
    });

    if (res.data?.rows) {
      const filteredTasks = res.data.rows
        .filter((t) => t.status !== 2 && t.status !== 3)
        .filter((t) => {
          if (!t.deadline) return false;
          const daysLeft = dayjs(t.deadline)
            .startOf("day")
            .diff(dayjs().startOf("day"), "day");
          return daysLeft >= 0 && daysLeft <= 7;
        })
        .sort((a, b) => {
          const dateA = a.deadline ? dayjs(a.deadline).valueOf() : Infinity;
          const dateB = b.deadline ? dayjs(b.deadline).valueOf() : Infinity;
          if (dateA !== dateB) return dateA - dateB;
          return (b.priority || 1) - (a.priority || 1);
        });

      tasks.value = filteredTasks;
    }
  } catch (error) {
    console.error("Failed to fetch overview tasks", error);
  } finally {
    loadingTasks.value = false;
  }
};

const confirmAction = (
  task: WbTask,
  actionTitle: string,
  targetStatus: number,
) => {
  confirmActionObj.value = { task, actionTitle, targetStatus };
  confirmStatusOpen.value = true;
};

const submitStatusUpdate = async () => {
  if (!confirmActionObj.value) return;
  const { task, actionTitle, targetStatus } = confirmActionObj.value;
  try {
    const payload = { ...task, status: targetStatus };
    if (targetStatus === 2 && task.type !== 1) {
      payload.progress = 100;
    }
    await WbTaskControllerApi.wbUpdatePost({ wbTask: payload });
    toast({ title: `${actionTitle}成功`, toast: "soft-success" });
    fetchTasks();
  } catch (error) {
  } finally {
    confirmStatusOpen.value = false;
  }
};

const openPreview = (task: WbTask) => {
  selectedTask.value = task;
  isViewMode.value = true;
  isModalOpen.value = true;
};

onMounted(() => {
  fetchTasks();
});
</script>

<template>
  <div class="overview-todo-panel h-[340px] flex flex-col">
    <div v-if="loadingTasks" class="space-y-3">
      <NSkeleton class="h-14 w-full rounded-md" />
      <NSkeleton class="h-14 w-full rounded-md" />
      <NSkeleton class="h-14 w-full rounded-md" />
    </div>

    <div
      v-else-if="tasks.length === 0"
      class="flex flex-col items-center justify-center h-full text-muted-foreground opacity-70 space-y-2 py-6"
    >
      <NIcon name="i-lucide-calendar-check-2" class="size-10" />
      <p class="text-sm">未来 7 天内无截止任务</p>
    </div>

    <NScrollArea v-else class="h-[344px] min-h-[344px] max-h-[344px] pr-1">
      <div class="space-y-1.5">
        <div
          v-for="task in tasks"
          :key="task.id"
          class="group min-h-[52px] flex items-center justify-between gap-3 px-3 py-2 rounded-lg border border-transparent hover:border-border hover:bg-muted/30 transition-colors"
        >
          <div
            class="min-w-0 flex-1 flex items-center justify-between gap-3 cursor-pointer"
            @click="openPreview(task)"
          >
            <div class="min-w-0 flex items-center gap-2">
              <NBadge
                :una="{
                  badgeDefaultVariant: task.type === 1 ? 'badge-soft-warning' : 'badge-soft-info',
                }"
                class="text-[10px] shrink-0"
                :label="task.type === 1 ? '提醒' : '任务'"
              />
              <div class="min-w-0">
                <div class="text-[15px] font-semibold truncate max-w-[180px] leading-5">
                  {{ task.name }}
                </div>
                <div class="mt-0.5 flex items-center gap-1 text-xs text-muted-foreground">
                  <NIcon
                    name="i-lucide-calendar-clock"
                    class="size-3.5 text-muted-foreground shrink-0"
                  />
                  <span class="whitespace-nowrap shrink-0">
                    截止: {{ task.deadline ? dayjs(task.deadline).format("MM-DD") : "-" }}
                  </span>
                </div>
              </div>
            </div>
            <div class="shrink-0">
              <ModulesWorkbenchTodoStatusProgress :task="task" compact />
            </div>
          </div>

          <div
            class="opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto transition-opacity shrink-0"
          >
            <NButton
              v-if="(task.status || 0) === 0"
              size="xs"
              btn="soft-primary"
              @click.stop="confirmAction(task, '开始任务', 1)"
            >
              开始
            </NButton>
            <NButton
              v-else-if="(task.status || 0) === 1"
              size="xs"
              btn="soft-success"
              @click.stop="confirmAction(task, '完成任务', 2)"
            >
              完成
            </NButton>
          </div>
        </div>
      </div>
    </NScrollArea>

    <ModulesWorkbenchTodoEditDialog
      v-model:open="isModalOpen"
      :task="selectedTask"
      :viewMode="isViewMode"
      @saved="fetchTasks"
    />

    <NDialog
      v-model:open="confirmStatusOpen"
      title="状态更新"
      :description="`您确定要标记为[${confirmActionObj?.actionTitle}]吗？`"
    >
      <template #footer>
        <NButton
          class="w-full"
          btn="solid-primary"
          @click="submitStatusUpdate"
          label="确定"
        />
      </template>
    </NDialog>
  </div>
</template>

<style scoped>
.overview-todo-panel {
  max-width: 100%;
}
</style>

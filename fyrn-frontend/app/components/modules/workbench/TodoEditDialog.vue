<script setup lang="ts">
import { ref, computed, watch, reactive } from "vue";
import dayjs from "dayjs";
import type { WbTask } from "~/api/models/WbTask";
import { useApi } from "~/api/useApi";

const { WbTaskControllerApi } = useApi();
const { toast } = useToast();

const props = defineProps<{
  open: boolean;
  task?: WbTask;
  viewMode?: boolean; // Default to false
  parentTaskId?: string;
  hideSubTask?: boolean;
}>();

const emit = defineEmits<{
  "update:open": [value: boolean];
  saved: [];
  close: [];
}>();

const isOpen = computed({
  get: () => props.open,
  set: (val) => emit("update:open", val),
});

const isView = ref(false);
const viewDetailExpanded = ref(false);
const loading = ref(false);
const loadingSubTasks = ref(false);
const subTasks = ref<WbTask[]>([]);
const subTaskDialogOpen = ref(false);
const subTaskDialogTask = ref<WbTask | undefined>(undefined);
const subTaskDialogViewMode = ref(false);
const openedSubTaskActionMenuId = ref<string | undefined>(undefined);
const todayString = dayjs().format("YYYY-MM-DD");

const formData = reactive<WbTask>({
  id: undefined,
  name: "",
  description: "",
  priority: 1,
  deadline: undefined,
  type: 0,
  tag: "0",
  status: 0,
  progress: 0,
  startTime: todayString,
});

const tagOptions: string[] = ["未分类", "日常", "工作"];

const getTagLabel = (val?: string): string => {
  if (val === "1") return "日常";
  if (val === "2") return "工作";
  return "未分类";
};

const getTagValue = (label: string) => {
  if (label === "日常") return "1";
  if (label === "工作") return "2";
  return "0";
};

const selectedTagLabel = computed({
  get: () => getTagLabel(formData.tag),
  set: (val: string) => {
    formData.tag = getTagValue(val);
  },
});

const progressSliderValue = computed<number[]>({
  get: () => [Number(formData.progress || 0)],
  set: (val: number[]) => {
    formData.progress = Number(val?.[0] ?? 0);
  },
});

const typeText = computed(() => (formData.type === 1 ? "提醒" : "任务"));

const priorityTextMap: Record<number, string> = {
  1: "低",
  2: "中",
  3: "高",
  4: "紧急",
};

const priorityText = computed(
  () => priorityTextMap[Number(formData.priority || 1)] || "低",
);

const periodText = computed(() => {
  if (formData.type === 1) {
    return formData.deadline
      ? dayjs(formData.deadline).format("YYYY-MM-DD HH:mm")
      : "-";
  }
  const start = formData.startTime
    ? dayjs(formData.startTime).format("YYYY-MM-DD")
    : "-";
  const end = formData.deadline ? dayjs(formData.deadline).format("YYYY-MM-DD") : "-";
  return `${start} 至 ${end}`;
});

const canManageSubTasks = computed(
  () =>
    !props.hideSubTask &&
    isView.value &&
    !!formData.id &&
    (formData.type || 0) !== 1,
);

const getStatusText = (status?: number) => {
  const map: Record<number, string> = {
    0: "未开始",
    1: "进行中",
    2: "已完成",
    3: "已作废",
  };
  return map[status || 0] || "未知";
};

const getStatusBadgeVariant = (status?: number) => {
  if (status === 2) return "badge-soft-success";
  if (status === 1) return "badge-soft-primary";
  if (status === 3) return "badge-soft-error";
  return "badge-soft-warning";
};

const fetchSubTasks = async () => {
  if (!formData.id) {
    subTasks.value = [];
    return;
  }
  loadingSubTasks.value = true;
  try {
    const res = await WbTaskControllerApi.wbSubTasksGet({ parentId: formData.id });
    subTasks.value = (res.data || []).slice().sort((a, b) => {
      const dateA = a.deadline ? dayjs(a.deadline).valueOf() : Infinity;
      const dateB = b.deadline ? dayjs(b.deadline).valueOf() : Infinity;
      if (dateA !== dateB) return dateA - dateB;
      return (a.createTime ? dayjs(a.createTime).valueOf() : 0) -
        (b.createTime ? dayjs(b.createTime).valueOf() : 0);
    });
  } catch (e) {
    subTasks.value = [];
  } finally {
    loadingSubTasks.value = false;
  }
};

const openCreateSubTaskDialog = () => {
  if (!formData.id) return;
  subTaskDialogTask.value = {
    parentId: formData.id,
    type: 0,
    priority: formData.priority || 1,
    tag: formData.tag || "0",
    status: 0,
    progress: 0,
    startTime: todayString,
    deadline: formData.deadline || undefined,
  };
  subTaskDialogViewMode.value = false;
  subTaskDialogOpen.value = true;
};

const openSubTaskDetail = (task: WbTask) => {
  closeSubTaskActionMenu();
  subTaskDialogTask.value = task;
  subTaskDialogViewMode.value = true;
  subTaskDialogOpen.value = true;
};

const updateSubTaskStatus = async (
  task: WbTask,
  targetStatus: number,
  actionTitle: string,
) => {
  if (!task.id) return;
  closeSubTaskActionMenu();
  try {
    const payload: WbTask = { ...task, status: targetStatus };
    if (targetStatus === 2 && task.type !== 1) {
      payload.progress = 100;
    }
    await WbTaskControllerApi.wbUpdatePost({ wbTask: payload });
    toast({ title: `${actionTitle}成功`, toast: "soft-success" });
    await fetchSubTasks();
    emit("saved");
  } catch (e) {
  }
};

const deleteSubTask = async (task: WbTask) => {
  if (!task.id) return;
  closeSubTaskActionMenu();
  try {
    await WbTaskControllerApi.wbDeletePost({ requestBody: [task.id] });
    toast({ title: "子任务已删除", toast: "soft-success" });
    await fetchSubTasks();
    emit("saved");
  } catch (e) {
  }
};

const handleSubTaskSaved = async () => {
  await fetchSubTasks();
  emit("saved");
};

const toggleSubTaskActionMenu = (taskId?: string) => {
  if (!taskId) return;
  openedSubTaskActionMenuId.value =
    openedSubTaskActionMenuId.value === taskId ? undefined : taskId;
};

const closeSubTaskActionMenu = () => {
  openedSubTaskActionMenuId.value = undefined;
};

watch(
  () => props.open,
  (opened) => {
    if (opened) {
      isView.value = !!props.viewMode;
      viewDetailExpanded.value = false;
      if (props.task) {
        Object.assign(formData, {
          id: props.task.id,
          name: props.task.name || "",
          description: props.task.description || "",
          priority: props.task.priority || 1,
          deadline: props.task.deadline
            ? dayjs(props.task.deadline).format(
                (props.task.type || 0) === 1 ? "YYYY-MM-DDTHH:mm" : "YYYY-MM-DD",
              )
            : undefined,
          type: props.task.type || 0,
          tag: props.task.tag || "0",
          status: props.task.status || 0,
          progress: props.task.progress || 0,
          startTime: props.task.startTime
            ? dayjs(props.task.startTime).format("YYYY-MM-DD")
            : props.task.type === 0
              ? todayString
              : undefined,
        });
        fetchSubTasks();
      } else {
        Object.assign(formData, {
          id: undefined,
          name: "",
          description: "",
          priority: 1,
          deadline: undefined,
          type: 0,
          tag: "0",
          status: 0,
          progress: 0,
          startTime: todayString,
        });
        subTasks.value = [];
      }
    }
  },
  { immediate: true },
);

watch(
  () => formData.type,
  (type) => {
    if (type === 0 && !formData.startTime) {
      formData.startTime = todayString;
    }
  },
);

const saveTask = async () => {
  if (!formData.name)
    return toast({ title: "请输入任务名称", toast: "soft-warning" });
  if (formData.type === 0 && !formData.deadline) {
    return toast({ title: "任务类型必须填写截止时间", toast: "soft-warning" });
  }

  loading.value = true;
  try {
    const payload = { ...formData };

    if (payload.type === 0 && !payload.startTime) {
      payload.startTime = todayString;
    }

    // Formatting dates to ISO string for backend
    if (payload.deadline) {
      payload.deadline = dayjs(payload.deadline).toISOString();
    } else {
      payload.deadline = undefined;
    }

    if (payload.startTime) {
      payload.startTime = dayjs(payload.startTime).toISOString();
    } else {
      payload.startTime = undefined;
    }

    if (formData.id) {
      await WbTaskControllerApi.wbUpdatePost({
        wbTask: Object.assign({}, props.task, payload),
      });
      toast({ title: "任务已更新", toast: "soft-success" });
      emit("saved");
      isView.value = true;
      return;
    } else {
      // Default Status based on logic
      if (payload.type === 0) {
        payload.status = 0; // Task defaults to unstarted
      } else {
        payload.status = 1; // Reminder defaults to doing
      }
      payload.progress = 0;
      if (props.parentTaskId) {
        payload.parentId = props.parentTaskId;
        await WbTaskControllerApi.wbAddSubTaskPost({ wbTask: payload });
        toast({ title: "子任务已创建", toast: "soft-success" });
      } else {
        await WbTaskControllerApi.wbAddPost({ wbTask: payload });
        toast({ title: "任务已创建", toast: "soft-success" });
      }
    }
    emit("saved");
    isOpen.value = false;
  } catch (e) {
  } finally {
    loading.value = false;
  }
};

const handleEditClick = () => {
  isView.value = false;
};
</script>

<template>
  <NDialog
    v-model:open="isOpen"
    :_dialog-content="{ class: 'sm:max-w-6xl w-full h-[80vh] flex flex-col' }"
  >
    <template #title>
      <div
        class="flex items-center gap-4 text-lg font-semibold leading-none tracking-tight"
      >
        <span>{{
          isView ? "查看任务" : formData.id ? "编辑任务" : "添加任务"
        }}</span>
        <NButton
          v-if="isView"
          size="xs"
          btn="outline-gray"
          leading="i-lucide-edit"
          @click="handleEditClick"
        >
          编辑
        </NButton>
      </div>
    </template>

    <div class="space-y-4 py-4 flex-1 overflow-y-auto pr-2 min-h-0">
      <div class="grid grid-cols-2 gap-4">
        <NFormGroup label="任务名称" required>
          <NInput
            v-model="formData.name"
            placeholder="输入任务名称"
            :disabled="isView"
          />
        </NFormGroup>

        <NFormGroup label="分类">
          <NSelect
            v-model="selectedTagLabel"
            :disabled="isView"
            :items="tagOptions"
          />
        </NFormGroup>
      </div>

      <div
        v-if="isView"
        class="rounded-md border border-border/70 bg-muted/10"
      >
        <button
          type="button"
          class="w-full px-3 py-2 text-left text-sm font-medium flex items-center justify-between"
          @click="viewDetailExpanded = !viewDetailExpanded"
        >
          <span>任务详情</span>
          <NIcon
            :name="
              viewDetailExpanded
                ? 'i-lucide-chevron-up'
                : 'i-lucide-chevron-down'
            "
            class="size-4 text-muted-foreground"
          />
        </button>
        <div
          v-if="viewDetailExpanded"
          class="grid grid-cols-2 gap-x-6 gap-y-3 px-3 pb-3 pt-1 text-sm"
        >
          <div>
            <span class="text-muted-foreground">类型：</span>{{ typeText }}
          </div>
          <div>
            <span class="text-muted-foreground">优先级：</span>{{ priorityText }}
          </div>
          <div>
            <span class="text-muted-foreground">进度：</span>{{ formData.progress || 0 }}%
          </div>
          <div>
            <span class="text-muted-foreground">{{
              formData.type === 1 ? "提醒时间：" : "任务周期："
            }}</span>{{ periodText }}
          </div>
        </div>
      </div>

      <div v-if="!isView" class="grid grid-cols-2 gap-4">
        <NFormGroup label="类型">
          <div class="flex gap-4 text-sm font-medium pt-2">
            <label class="flex items-center gap-1.5 cursor-pointer">
              <input
                type="radio"
                v-model="formData.type"
                :value="0"
                :disabled="isView"
                class="accent-primary"
              />
              任务
            </label>
            <label class="flex items-center gap-1.5 cursor-pointer">
              <input
                type="radio"
                v-model="formData.type"
                :value="1"
                :disabled="isView"
                class="accent-primary"
              />
              提醒
            </label>
          </div>
        </NFormGroup>

        <NFormGroup label="优先级">
          <div class="flex gap-4 text-sm font-medium pt-2">
            <label class="flex items-center gap-1.5 cursor-pointer"
              ><input
                type="radio"
                v-model="formData.priority"
                :value="1"
                :disabled="isView"
                class="accent-slate-500"
              />
              低</label
            >
            <label class="flex items-center gap-1.5 cursor-pointer"
              ><input
                type="radio"
                v-model="formData.priority"
                :value="2"
                :disabled="isView"
                class="accent-blue-500"
              />
              中</label
            >
            <label class="flex items-center gap-1.5 cursor-pointer"
              ><input
                type="radio"
                v-model="formData.priority"
                :value="3"
                :disabled="isView"
                class="accent-orange-500"
              />
              高</label
            >
            <label class="flex items-center gap-1.5 cursor-pointer"
              ><input
                type="radio"
                v-model="formData.priority"
                :value="4"
                :disabled="isView"
                class="accent-red-500"
              />
              紧急</label
            >
          </div>
        </NFormGroup>
      </div>

      <div v-if="!isView && formData.type === 0" class="grid grid-cols-2 gap-4">

        <NFormGroup label="进度 (%)" class="col-span-1">
          <div class="flex items-center gap-3 pt-1">
            <NSlider
              v-model="progressSliderValue"
              :min="0"
              :max="100"
              :step="1"
              :disabled="isView || !formData.id"
              class="flex-1"
            />
            <span class="text-sm font-medium w-12 text-right">
              {{ formData.progress || 0 }}%
            </span>
          </div>
        </NFormGroup>
        <NFormGroup label="任务周期" class="col-span-1">
          <CommonDateTimeRangePicker
              v-model:startValue="formData.startTime"
              v-model:endValue="formData.deadline"
              :disabled="isView"
          />
        </NFormGroup>
      </div>
      <div v-else-if="!isView" class="grid grid-cols-2 gap-4">
        <NFormGroup label="提醒时间" class="col-span-1">
          <CommonDateTimePicker
            v-model="formData.deadline"
            :withTime="true"
            :disabled="isView"
            placeholder="选择提醒时间（精确到分钟）"
          />
        </NFormGroup>
      </div>

      <div
        v-if="canManageSubTasks"
        class="rounded-md border border-border/70 bg-muted/5 p-3 space-y-2"
      >
        <div class="flex items-center justify-between">
          <div class="text-sm font-medium">子任务</div>
          <div class="flex items-center gap-2">
            <div class="text-xs text-muted-foreground">共 {{ subTasks.length }} 项</div>
            <NButton
              label="添加子任务"
              btn="solid-primary"
              size="xs"
              :_button="{ class: 'h-7 px-2.5 text-xs' }"
              @click="openCreateSubTaskDialog"
            />
          </div>
        </div>

        <div v-if="loadingSubTasks" class="space-y-2">
          <NSkeleton class="h-10 w-full rounded" />
          <NSkeleton class="h-10 w-full rounded" />
        </div>
        <div
          v-else-if="subTasks.length === 0"
          class="text-sm text-muted-foreground py-2"
        >
          暂无子任务
        </div>
        <NScrollArea v-else class="max-h-[260px] pr-1">
          <table class="w-full text-sm border-collapse">
            <thead>
              <tr class="border-b border-border/70 text-muted-foreground">
                <th class="text-left font-medium py-2 pr-3">标题</th>
                <th class="text-left font-medium py-2 pr-3 w-52"></th>
                <th class="text-left font-medium py-2 pr-3 w-24">状态</th>
                <th class="text-left font-medium py-2 pr-3 w-20">优先级</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in subTasks"
                :key="item.id"
                class="group border-b border-border/50 hover:bg-muted/20 transition-colors"
                @mouseleave="closeSubTaskActionMenu"
              >
                <td class="py-2 pr-3">
                  <div class="flex items-center gap-2 min-w-0">
                    <span
                      :class="[
                        'w-4 h-4 shrink-0',
                        (item.status || 0) === 2
                          ? 'i-lucide-check-square text-green-500'
                          : 'i-lucide-square text-gray-400',
                      ]"
                    />
                    <button
                      type="button"
                      class="truncate max-w-[280px] text-left hover:text-primary cursor-pointer"
                      @click="openSubTaskDetail(item)"
                    >
                      {{ item.name || "-" }}
                    </button>
                  </div>
                </td>
                <td class="py-2 pr-3">
                  <div
                    class="relative opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto transition-opacity flex items-center gap-1"
                  >
                    <button
                      v-if="(item.status || 0) === 0"
                      class="cursor-pointer text-sm font-medium text-blue-500 hover:text-blue-600 transition-colors px-1"
                      @click.stop="updateSubTaskStatus(item, 1, '开始任务')"
                    >
                      开始
                    </button>
                    <button
                      v-else-if="(item.status || 0) === 1"
                      class="cursor-pointer text-sm font-medium text-green-500 hover:text-green-600 transition-colors px-1"
                      @click.stop="updateSubTaskStatus(item, 2, '完成任务')"
                    >
                      完成
                    </button>
                    <button
                      class="cursor-pointer text-sm font-medium text-gray-500 hover:text-gray-700 transition-colors inline-flex items-center gap-1 px-1"
                      @click.stop="toggleSubTaskActionMenu(item.id)"
                    >
                      更多
                      <span class="i-lucide-chevron-down w-3 h-3" />
                    </button>
                    <div
                      v-if="openedSubTaskActionMenuId === item.id"
                      class="absolute left-0 bottom-8 z-40 w-24 rounded-md border bg-white p-1 shadow-md"
                    >
                      <button
                        v-if="(item.status || 0) !== 2 && (item.status || 0) !== 3"
                        class="w-full text-left px-2 py-1.5 rounded text-sm text-gray-600 hover:bg-gray-50"
                        @click.stop="updateSubTaskStatus(item, 3, '作废任务')"
                      >
                        作废
                      </button>
                      <button
                        class="w-full text-left px-2 py-1.5 rounded text-sm text-red-500 hover:bg-red-50"
                        @click.stop="deleteSubTask(item)"
                      >
                        删除
                      </button>
                    </div>
                  </div>
                </td>
                <td class="py-2 pr-3">
                  <NBadge
                    class="text-xs"
                    :una="{ badgeDefaultVariant: getStatusBadgeVariant(item.status) }"
                    :label="getStatusText(item.status)"
                  />
                </td>
                <td class="py-2 pr-3">{{ priorityTextMap[Number(item.priority || 1)] || "低" }}</td>
              </tr>
            </tbody>
          </table>
        </NScrollArea>
      </div>

      <NFormGroup label="描述">
        <CommonMarkdownEditor
          :modelValue="formData.description || ''"
          @update:modelValue="formData.description = $event"
          :disabled="isView"
        />
      </NFormGroup>
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <NButton
          v-if="isView"
          label="关闭"
          btn="ghost-gray"
          @click="isOpen = false"
        />
        <template v-else>
          <NButton label="取消" btn="ghost-gray" @click="isOpen = false" />
          <NButton
            label="保存"
            btn="solid-primary"
            :loading="loading"
            @click="saveTask"
          />
        </template>
      </div>
    </template>
  </NDialog>

  <ModulesWorkbenchTodoEditDialog
    v-if="subTaskDialogOpen"
    v-model:open="subTaskDialogOpen"
    :task="subTaskDialogTask"
    :view-mode="subTaskDialogViewMode"
    :parent-task-id="formData.id"
    :hide-sub-task="true"
    @saved="handleSubTaskSaved"
  />
</template>

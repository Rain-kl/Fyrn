<script setup lang="ts">
import {
  ref,
  onMounted,
  onBeforeUnmount,
  computed,
  watch,
  h,
  resolveComponent,
} from "vue";
import type { ColumnDef, VisibilityState } from "@tanstack/vue-table";
import dayjs from "dayjs";
import type { WbTask } from "~/api/models/WbTask";
import { useApi } from "~/api/useApi";

const { WbTaskControllerApi } = useApi();
const { toast } = useToast();

const tasks = ref<WbTask[]>([]);
const loadingTasks = ref(false);
const total = ref(0);

const pageNo = ref(1);
const pageSize = ref(10);

const isModalOpen = ref(false);
const selectedTask = ref<WbTask | undefined>(undefined);
const isViewMode = ref(false);

const searchName = ref("");
const filterType = ref<number | undefined>(undefined);
const filterStatus = ref<number | undefined>(undefined);
const filterPriority = ref<number | undefined>(undefined);
const sorting = ref<any[]>([]);

const filterTypeLabel = computed({
  get: () => {
    if (filterType.value === 0) return "任务";
    if (filterType.value === 1) return "提醒";
    return "全部类型";
  },
  set: (val: string) => {
    if (val === "任务") filterType.value = 0;
    else if (val === "提醒") filterType.value = 1;
    else filterType.value = undefined;
  },
});

const filterStatusLabel = computed({
  get: () => {
    if (filterStatus.value === 0) return "未开始";
    if (filterStatus.value === 1) return "进行中";
    if (filterStatus.value === 2) return "已完成";
    if (filterStatus.value === 3) return "已作废";
    return "全部状态";
  },
  set: (val: string) => {
    if (val === "未开始") filterStatus.value = 0;
    else if (val === "进行中") filterStatus.value = 1;
    else if (val === "已完成") filterStatus.value = 2;
    else if (val === "已作废") filterStatus.value = 3;
    else filterStatus.value = undefined;
  },
});

const filterPriorityLabel = computed({
  get: () => {
    if (filterPriority.value === 1) return "低";
    if (filterPriority.value === 2) return "中";
    if (filterPriority.value === 3) return "高";
    if (filterPriority.value === 4) return "紧急";
    return "全部优先级";
  },
  set: (val: string) => {
    if (val === "低") filterPriority.value = 1;
    else if (val === "中") filterPriority.value = 2;
    else if (val === "高") filterPriority.value = 3;
    else if (val === "紧急") filterPriority.value = 4;
    else filterPriority.value = undefined;
  },
});

const columnVisibility = ref<VisibilityState>({
  id: false,
  startTime: false,
});
const openedActionMenuId = ref<string | undefined>(undefined);

const getPriorityLabel = (p: number) => {
  const map: Record<number, string> = { 1: "低", 2: "中", 3: "高", 4: "紧急" };
  return map[p] || "低";
};

const getTypeLabel = (t: number) => {
  return t === 1 ? "提醒" : "任务";
};

const getTypeBadgeVariant = (t: number) => {
  return t === 1 ? "badge-soft-warning" : "badge-soft-info";
};

const getTagLabel = (t: string) => {
  const map: Record<string, string> = {
    "0": "未分类",
    "1": "日常",
    "2": "工作",
  };
  return map[t] || "未知";
};

const confirmStatusOpen = ref(false);
const confirmActionObj = ref<{
  task: WbTask;
  actionTitle: string;
  targetStatus: number;
} | null>(null);

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
  } catch (e) {
  } finally {
    confirmStatusOpen.value = false;
  }
};

const deleteDialogOpen = ref(false);
const deleteId = ref<string | undefined>(undefined);

const handleDelete = (id: string) => {
  deleteId.value = id;
  deleteDialogOpen.value = true;
};

const confirmDelete = async () => {
  if (!deleteId.value) return;
  try {
    await WbTaskControllerApi.wbDeletePost({ requestBody: [deleteId.value] });
    toast({ title: "任务已删除", toast: "soft-success" });
    fetchTasks();
  } catch (error) {
  } finally {
    deleteDialogOpen.value = false;
  }
};

const toggleActionMenu = (taskId?: string) => {
  if (!taskId) return;
  openedActionMenuId.value =
    openedActionMenuId.value === taskId ? undefined : taskId;
};

const closeActionMenu = () => {
  openedActionMenuId.value = undefined;
};

const handleGlobalClick = (event: MouseEvent) => {
  const target = event.target as HTMLElement | null;
  if (!target?.closest("[data-action-menu-root='true']")) {
    closeActionMenu();
  }
};

const openAddModal = () => {
  selectedTask.value = undefined;
  isViewMode.value = false;
  isModalOpen.value = true;
};

const openViewModal = (task: WbTask) => {
  selectedTask.value = task;
  isViewMode.value = true;
  isModalOpen.value = true;
};

const columns: ColumnDef<WbTask>[] = [
  {
    header: "类型",
    id: "type",
    accessorKey: "type",
    meta: { class: "w-16 text-center whitespace-nowrap" },
    cell: (info) => {
      const t = info.row.original.type || 0;
      return h(resolveComponent("NBadge"), {
        una: {
          badgeDefaultVariant: getTypeBadgeVariant(t),
        },
        class: "text-xs",
        label: getTypeLabel(t),
      });
    },
    enableSorting: false,
    enableHiding: false,
  },
  {
    header: "ID",
    accessorKey: "id",
    meta: { class: "w-20 text-muted/70 text-xs text-center" },
  },
  {
    header: "标题",
    accessorKey: "name",
    meta: { class: "w-[40%]" },
    cell: (info) => {
      const task = info.row.original;
      return h(
        "div",
        {
          class:
            "flex items-center gap-2 cursor-pointer hover:bg-gray-50/50 rounded py-1 -ml-1 w-fit",
          onClick: () => openViewModal(task),
        },
        [
          h("div", { class: "i-lucide-box text-blue-500 w-4 h-4 shrink-0" }),
          h("span", { class: "font-medium text-gray-900" }, task.name),
        ],
      );
    },
  },
  {
    id: "actions",
    meta: { class: "w-28 text-right pr-4" },
    cell: (info) => {
      const task = info.row.original;
      const status = task.status || 0;
      const actions = [];

      if (status === 0) {
        actions.push(
          h(
            "button",
            {
              class: "cursor-pointer text-sm font-medium text-blue-500 hover:text-blue-600 transition-colors",
              onClick: (e: MouseEvent) => {
                e.stopPropagation();
                confirmAction(task, "开始任务", 1);
              },
            },
            "开始",
          ),
        );
      }

      if (status === 1) {
        actions.push(
          h(
            "button",
            {
              class: "cursor-pointer text-sm font-medium text-green-500 hover:text-green-600 transition-colors",
              onClick: (e: MouseEvent) => {
                e.stopPropagation();
                confirmAction(task, "完成任务", 2);
              },
            },
            "完成",
          ),
        );
      }

      const menuItems = [];
      if (status !== 2 && status !== 3) {
        menuItems.push(
          h(
            "button",
            {
              class: "w-full text-left px-2 py-1.5 rounded text-sm text-gray-600 hover:bg-gray-50",
              onClick: (e: MouseEvent) => {
                e.stopPropagation();
                closeActionMenu();
                confirmAction(task, "作废任务", 3);
              },
            },
            "作废",
          ),
        );
      }
      menuItems.push(
        h(
          "button",
          {
            class: "w-full text-left px-2 py-1.5 rounded text-sm text-red-500 hover:bg-red-50",
            onClick: (e: MouseEvent) => {
              e.stopPropagation();
              closeActionMenu();
              handleDelete(task.id!);
            },
          },
          "删除",
        ),
      );

      actions.push(
        h(
          "div",
          { class: "relative", "data-action-menu-root": "true" },
          [
            h(
              "button",
              {
                class: "inline-flex items-center justify-center w-7 h-7 rounded hover:bg-gray-100 text-gray-500",
                "aria-label": "更多操作",
                onClick: (e: MouseEvent) => {
                  e.stopPropagation();
                  toggleActionMenu(task.id);
                },
              },
              [h("span", { class: "i-lucide-ellipsis w-4 h-4" })],
            ),
            openedActionMenuId.value === task.id
              ? h(
                  "div",
                  {
                    class: "absolute right-0 top-8 z-30 w-24 rounded-md border bg-white p-1 shadow-md",
                  },
                  menuItems,
                )
              : null,
          ],
        ),
      );

      return h(
        "div",
        { class: "flex items-center justify-end gap-2 task-actions" },
        actions,
      );
    },
  },
  {
    id: "status",
    meta: { class: "w-40 whitespace-nowrap" },
    cell: (info) => {
      const task = info.row.original;
      return h(resolveComponent("ModulesWorkbenchTodoStatusProgress"), { task });
    },
  },
  {
    header: "优先级",
    accessorKey: "priority",
    meta: { class: "w-24 whitespace-nowrap" },
    enableSorting: true,
    cell: (info) => {
      const p = info.row.original.priority || 1;
      let color = "text-gray-500";
      if (p === 2) color = "text-blue-500";
      else if (p === 3) color = "text-orange-500";
      else if (p === 4) color = "text-red-500";
      return h("span", { class: `text-sm font-medium ${color}` }, getPriorityLabel(p));
    },
  },
  {
    header: "任务分类",
    id: "tag",
    accessorKey: "tag",
    meta: { class: "w-24 whitespace-nowrap" },
    cell: (info) => {
      const tag = info.row.original.tag || "0";
      return h("span", { class: "text-sm text-muted-foreground" }, getTagLabel(tag));
    },
  },
  {
    header: "需要完成日期",
    accessorKey: "deadline",
    enableSorting: true,
    meta: { class: "w-32 whitespace-nowrap text-center" },
    cell: (info) => {
      const rowOrigin: any = info.row.original;
      return h(
        "div",
        { class: "w-full text-center" },
        rowOrigin.deadline ? dayjs(rowOrigin.deadline).format("YYYY-MM-DD") : "-",
      );
    },
  },
];

const fetchTasks = async () => {
  loadingTasks.value = true;
  try {
    const orderBy =
      sorting.value.length > 0
        ? sorting.value[0].id.replace(
            /[A-Z]/g,
            (match: string) => "_" + match.toLowerCase(),
          )
        : undefined;
    const orderDirection =
      sorting.value.length > 0
        ? sorting.value[0].desc
          ? "desc"
          : "asc"
        : undefined;

    const res = await WbTaskControllerApi.wbPagePost({
      wbTaskPageInput: {
        pageNo: pageNo.value,
        pageSize: pageSize.value,
        orderBy,
        orderDirection,
        wbTask: {
          name: searchName.value || undefined,
          status:
            filterStatus.value !== undefined ? filterStatus.value : undefined,
          type: filterType.value !== undefined ? filterType.value : undefined,
          priority:
            filterPriority.value !== undefined
              ? filterPriority.value
              : undefined,
        },
      },
    });

    if (res.data) {
      tasks.value = res.data.rows || [];
      total.value = res.data.total || 0;
    }
  } catch (error) {
    console.error("Failed to fetch tasks", error);
  } finally {
    loadingTasks.value = false;
  }
};

defineExpose({
  refresh: fetchTasks,
  openAddModal,
});

onMounted(() => {
  fetchTasks();
  window.addEventListener("click", handleGlobalClick);
});

onBeforeUnmount(() => {
  window.removeEventListener("click", handleGlobalClick);
});

watch([pageNo, pageSize], () => {
  fetchTasks();
});

watch([searchName, filterType, filterStatus, filterPriority], () => {
  pageNo.value = 1;
  fetchTasks();
});

watch(
  sorting,
  () => {
    fetchTasks();
  },
  { deep: true },
);
</script>

<template>
  <div class="h-full flex flex-col gap-4 w-full">
    <div class="flex items-center gap-3 pb-2 shrink-0 flex-wrap">
      <NInput
        v-model="searchName"
        placeholder="搜索任务名称..."
        leading="i-lucide-search"
        class="max-w-[250px]"
        clearable
      />
      <NSelect
        v-model="filterTypeLabel"
        :items="['全部类型', '任务', '提醒']"
        placeholder="类型"
        :_select-trigger="{ class: 'w-[120px]' }"
      />
      <NSelect
        v-model="filterStatusLabel"
        :items="['全部状态', '未开始', '进行中', '已完成', '已作废']"
        placeholder="状态"
        :_select-trigger="{ class: 'w-[120px]' }"
      />
      <NSelect
        v-model="filterPriorityLabel"
        :items="['全部优先级', '低', '中', '高', '紧急']"
        placeholder="优先级"
        :_select-trigger="{ class: 'w-[130px]' }"
      />
    </div>

    <div class="flex-1 flex flex-col min-h-0 min-w-0">
      <NTable
        v-model:column-visibility="columnVisibility"
        v-model:sorting="sorting"
        :loading="loadingTasks"
        :columns="columns"
        :data="tasks"
        :row-count="total"
        manual-pagination
        manual-sorting
        row-id="id"
        class="h-full border rounded-md"
      />

      <div class="flex items-center justify-between pt-4 shrink-0">
        <div class="hidden text-sm text-muted sm:block">共 {{ total }} 条记录</div>
        <div class="flex items-center space-x-6 lg:space-x-8">
          <div
            class="hidden items-center justify-center text-sm font-medium sm:flex space-x-2"
          >
            <span class="text-nowrap"> 每页行数 </span>
            <NSelect
              :items="[10, 20, 30, 40, 50]"
              :_select-trigger="{ class: 'w-15' }"
              :model-value="pageSize"
              @update:model-value="
                (val) => {
                  pageSize = Number(val);
                  pageNo = 1;
                }
              "
            />
          </div>
          <div class="flex items-center justify-center text-sm font-medium">
            第 {{ pageNo }} 页,共 {{ Math.ceil(total / pageSize) || 1 }} 页
          </div>
          <NPagination
            :page="pageNo"
            :total="total"
            :show-list-item="false"
            :items-per-page="pageSize"
            @update:page="pageNo = $event"
          />
        </div>
      </div>
    </div>

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

    <NDialog
      v-model:open="deleteDialogOpen"
      title="确认删除"
      description="该操作不可撤销，是否继续？"
    >
      <template #footer>
        <div class="w-full flex gap-2">
          <NButton
            class="flex-1"
            btn="outline-gray"
            @click="deleteDialogOpen = false"
            label="取消"
          />
          <NButton
            class="flex-1"
            btn="solid-error"
            @click="confirmDelete"
            label="删除"
          />
        </div>
      </template>
    </NDialog>
  </div>
</template>

<style scoped>
:deep(tbody tr .task-actions) {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
}

:deep(tbody tr:hover .task-actions) {
  opacity: 1;
  pointer-events: auto;
}
</style>

<script setup lang="ts">
import type {
  ColumnDef,
  RowSelectionState,
  Table,
  VisibilityState,
} from "@tanstack/vue-table";
import type { OmsTask } from "~/api/models";
import { useApi } from "~/api/useApi";
import { formatToYMDHMS } from "~/utils/date";

const { OmsTaskControllerApi } = useApi();

const data = ref<OmsTask[]>([]);
const loading = ref(false);
const total = ref(0);
const pageNo = ref(1);
const pageSize = ref(10);

const columnVisibility = ref<VisibilityState>({
  taskId: false,
  createdUser: false,
  createTime: false,
  allowRetry: false,
  bizValue: false,
});

const filters = reactive({
  taskId: "" as string,
  taskTag: "" as string,
  status: "全部状态" as string,
});

const statusOptions = [
  "全部状态",
  "排队中",
  "运行中",
  "成功",
  "失败",
  "已取消",
];

const statusLabelToValue = (label: string): number | undefined => {
  const map: Record<string, number | undefined> = {
    全部状态: undefined,
    排队中: 0,
    运行中: 1,
    成功: 2,
    失败: 3,
    已取消: 4,
  };
  return map[label];
};

const getStatusLabel = (status?: number) => {
  const statusMap: Record<number, string> = {
    0: "排队中",
    1: "运行中",
    2: "成功",
    3: "失败",
    4: "已取消",
  };
  return status !== undefined ? statusMap[status] : "未知";
};

const getStatusVariant = (status?: number) => {
  const variantMap: Record<number, string> = {
    0: "badge-soft-info",
    1: "badge-soft-warning",
    2: "badge-soft-success",
    3: "badge-soft-error",
    4: "badge-soft-gray",
  };
  return status !== undefined ? variantMap[status] : "badge-soft-gray";
};

const columns: ColumnDef<OmsTask>[] = [
  {
    header: "Task ID",
    accessorKey: "taskId",
  },
  {
    header: "任务标签",
    accessorKey: "taskTag",
    cell: (info) => {
      const tag = info.row.original.taskTag;
      if (!tag) return "-";
      return h(resolveComponent("NBadge"), {
        una: {
          badgeDefaultVariant: "badge-soft-info",
        },
        class: "capitalize",
        label: tag,
      });
    },
  },
  {
    header: "业务标签",
    accessorKey: "bizTag",
    cell: (info) => {
      const tag = info.row.original.bizTag;
      if (!tag) return "-";
      return h(resolveComponent("NBadge"), {
        una: {
          badgeDefaultVariant: "badge-soft-warning",
        },
        class: "capitalize",
        label: tag,
      });
    },
  },
  {
    header: "业务值",
    accessorKey: "bizValue",
  },
  {
    header: "允许重试",
    accessorKey: "allowRetry",
    cell: (info) => {
      const allowRetry = info.row.original.allowRetry;
      if (allowRetry === undefined || allowRetry === null) return "-";
      const label = allowRetry === 1 ? "允许" : "不允许";
      const variant = allowRetry === 1 ? "badge-soft-success" : "badge-soft-gray";
      return h(resolveComponent("NBadge"), {
        una: {
          badgeDefaultVariant: variant,
        },
        class: "capitalize",
        label,
      });
    },
  },
  {
    header: "状态",
    accessorKey: "status",
    cell: (info) => {
      const status = info.row.original.status;
      return h(resolveComponent("NBadge"), {
        una: {
          badgeDefaultVariant: getStatusVariant(status),
        },
        class: "capitalize",
        label: getStatusLabel(status),
      });
    },
  },
  {
    header: "状态说明",
    accessorKey: "message",
    cell: (info) => {
      const message = info.row.original.message;
      return h(resolveComponent("CommonTextCollapse"), {
        text: message,
        maxLength: 50,
      });
    },
  },
  {
    header: "触发人",
    accessorKey: "createdUser",
  },
  {
    header: "开始时间",
    accessorKey: "startedTime",
    cell: (info) => {
      const time = info.row.original.startedTime;
      return time ? formatToYMDHMS(time) : "-";
    },
  },
  {
    header: "创建时间",
    accessorKey: "createTime",
    cell: (info) => formatToYMDHMS(info.row.original.createTime),
  },
  {
    header: "结束时间",
    accessorKey: "finishedTime",
    cell: (info) => {
      const time = info.row.original.finishedTime;
      return time ? formatToYMDHMS(time) : "-";
    },
  },
  {
    header: "操作",
    id: "actions",
    cell: (info) => {
      const taskId = info.row.original.taskId;
      return h(resolveComponent("NButton"), {
        label: "详情",
        btn: "ghost-primary",
        size: "sm",
        leading: "i-heroicons-eye",
        onClick: () => {
          if (taskId) navigateTo(`/oms/task/${taskId}`);
        },
      });
    },
  },
];

const select = ref<RowSelectionState>();

const table = useTemplateRef<Table<OmsTask>>("table");

const fetchData = async () => {
  loading.value = true;
  try {
    const result = await OmsTaskControllerApi.omsTaskListGet({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      taskId: filters.taskId || undefined,
      taskTag: filters.taskTag || undefined,
      status: statusLabelToValue(filters.status),
    });

    if (result.code === 200) {
      data.value = result.data?.rows || [];
      total.value = result.data?.total || 0;
    }
  } catch (error) {
    console.error("Failed to fetch tasks:", error);
  } finally {
    loading.value = false;
  }
};

watch([pageNo, pageSize], () => {
  fetchData();
});

watch(
  filters,
  () => {
    // 过滤条件变化时回到第一页
    pageNo.value = 1;
    fetchData();
  },
  { deep: true }
);

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="flex flex-col space-y-4">
    <!-- header -->
    <div
      class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center"
    >
      <div class="grid w-full gap-2 md:grid-cols-4">
        <NInput v-model="filters.taskId" placeholder="Task ID" />

        <NInput v-model="filters.taskTag" placeholder="任务标签" />

        <NSelect
          v-model="filters.status"
          :items="statusOptions"
          placeholder="选择状态"
          :_select-trigger="{
            class: 'w-full',
          }"
        />
      </div>

      <div class="flex items-center gap-x-2 sm:ml-auto">
        <NButton
          label="刷新"
          btn="solid-gray"
          leading="i-radix-icons-update"
          class="w-full sm:w-auto sm:shrink-0 active:translate-y-0.5"
          :loading="loading"
          @click="fetchData"
        />
      </div>
    </div>

    <!-- column visibility -->
    <div class="flex flex-wrap items-center gap-4 px-1">
      <span class="text-sm font-medium text-muted">显示列:</span>
      <NCheckbox
        v-for="tableColumn in table
          ?.getAllLeafColumns()
          .filter((c) => typeof c.columnDef.header === 'string')"
        :key="tableColumn.id"
        :model-value="tableColumn.getIsVisible()"
        :label="String(tableColumn.columnDef.header)"
        @update:model-value="tableColumn.toggleVisibility()"
      />
    </div>

    <!-- table -->
    <NTable
      ref="table"
      v-model:row-selection="select"
      v-model:column-visibility="columnVisibility"
      :loading
      :columns
      :data
      enable-row-selection
      row-id="taskId"
    >
    </NTable>

    <!-- footer -->
    <div class="flex items-center justify-between px-2">
      <div class="hidden text-sm text-muted sm:block">
        已选择
        {{ table?.getFilteredSelectedRowModel().rows.length.toLocaleString() }}
        / {{ total.toLocaleString() }}
        条记录
      </div>
      <div class="flex items-center space-x-6 lg:space-x-8">
        <div
          class="hidden items-center justify-center text-sm font-medium sm:flex space-x-2"
        >
          <span class="text-nowrap"> 每页行数 </span>

          <NSelect
            :items="[10, 20, 30, 40, 50]"
            :_select-trigger="{
              class: 'w-15',
            }"
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
          第 {{ pageNo }} 页,共 {{ Math.ceil(total / pageSize) }} 页
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
</template>

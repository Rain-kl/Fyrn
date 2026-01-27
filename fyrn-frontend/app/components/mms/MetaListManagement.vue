<script setup lang="ts">
import type {
  ColumnDef,
  RowSelectionState,
  Table,
  VisibilityState,
} from "@tanstack/vue-table";
import type { MmsMeta } from "~/api/models";
import { useApi } from "~/api/useApi";
import { formatToYMDHMS } from "~/utils/date";
import { formatWordCount } from "~/utils/number";

const { MmsMetaControllerApi } = useApi();
const { toast } = useToast();

const data = ref<MmsMeta[]>([]);
const loading = ref(false);
const total = ref(0);
const pageNo = ref(1);
const pageSize = ref(10);

const columnVisibility = ref<VisibilityState>({
  id: false,
  pbn: false,
  tag: false,
  popularity: false,
  source: false,
  postTime: false,
  editTime: false,
  createdUser: false,
  updatedUser: false,
  createTime: false,
  updateTime: false,
});

const filters = reactive({
  novelId: "" as string,
  novelTitle: "" as string,
  novelAuthor: "" as string,
});

const editDialogOpen = ref(false);
const selectedMeta = ref<MmsMeta | undefined>(undefined);

const getStatusLabel = (status?: number) => {
  const statusMap: Record<number, string> = {
    1: "连载中",
    2: "完结",
  };
  return status !== undefined ? statusMap[status] : "未知";
};

const getStatusVariant = (status?: number) => {
  const variantMap: Record<number, string> = {
    1: "badge-soft-info",
    2: "badge-soft-success",
  };
  return status !== undefined ? variantMap[status] : "badge-soft-gray";
};

const columns: ColumnDef<MmsMeta>[] = [
  {
    header: "ID",
    accessorKey: "id",
  },
  {
    header: "PBN",
    accessorKey: "pbn",
  },
  {
    header: "标题",
    accessorKey: "title",
    cell: (info) => {
      const title = info.row.original.title;
      return h(
        "div",
        {
          class: "font-medium text-primary cursor-pointer hover:underline",
        },
        title
      );
    },
  },
  {
    header: "作者",
    accessorKey: "author",
  },
  {
    header: "标签",
    accessorKey: "tag",
  },
  {
    header: "简介",
    accessorKey: "summary",
    cell: (info) => {
      const summary = info.row.original.summary;
      return h(resolveComponent("CommonTextCollapse"), {
        text: summary,
        maxLength: 30,
      });
    },
  },
  {
    header: "人气值",
    accessorKey: "popularity",
    cell: (info) => {
      const value = info.row.original.popularity;
      return formatWordCount(value);
    },
  },
  {
    header: "字数",
    accessorKey: "wordCount",
    cell: (info) => {
      const value = info.row.original.wordCount;
      return formatWordCount(value);
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
    header: "来源",
    accessorKey: "source",
  },
  {
    header: "来源链接",
    accessorKey: "sourceUrl",
    cell: (info) => {
      const url = info.row.original.sourceUrl;
      if (!url) return "-";
      return h(
        "a",
        {
          href: url,
          target: "_blank",
          class: "text-primary hover:underline",
        },
        "查看"
      );
    },
  },
  {
    header: "发布时间",
    accessorKey: "postTime",
    cell: (info) => {
      const time = info.row.original.postTime;
      return time ? formatToYMDHMS(time) : "-";
    },
  },
  {
    header: "编辑时间",
    accessorKey: "editTime",
    cell: (info) => {
      const time = info.row.original.editTime;
      return time ? formatToYMDHMS(time) : "-";
    },
  },
  {
    header: "创建人",
    accessorKey: "createdUser",
  },
  {
    header: "创建时间",
    accessorKey: "createTime",
    cell: (info) => {
      const time = info.row.original.createTime;
      return time ? formatToYMDHMS(time) : "-";
    },
  },
  {
    header: "修改人",
    accessorKey: "updatedUser",
  },
  {
    header: "修改时间",
    accessorKey: "updateTime",
    cell: (info) => {
      const time = info.row.original.updateTime;
      return time ? formatToYMDHMS(time) : "-";
    },
  },
  {
    header: "操作",
    id: "actions",
    cell: (info) => {
      const meta = info.row.original;
      return h("div", { class: "flex items-center gap-3" }, [
        h(
          "span",
          {
            class:
              "cursor-pointer text-sm font-medium text-primary hover:underline",
            onClick: () => handleEdit(meta),
          },
          "编辑"
        ),
        h(
          "span",
          {
            class:
              "cursor-pointer text-sm font-medium text-red-500 hover:underline",
            onClick: () => openDeleteDialog(meta.id!),
          },
          "删除"
        ),
      ]);
    },
  },
];

const select = ref<RowSelectionState>();

const table = useTemplateRef<Table<MmsMeta>>("table");

const fetchData = async () => {
  loading.value = true;
  try {
    const result = await MmsMetaControllerApi.mmsMetaPageGet({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      novelId: filters.novelId ? Number(filters.novelId) : undefined,
      novelTitle: filters.novelTitle || undefined,
      novelAuthor: filters.novelAuthor || undefined,
    });

    if (result.code === 200) {
      data.value = result.data?.rows || [];
      total.value = result.data?.total || 0;
    }
  } catch (error) {
    console.error("Failed to fetch meta data:", error);
  } finally {
    loading.value = false;
  }
};

const handleCreate = () => {
  selectedMeta.value = undefined;
  editDialogOpen.value = true;
};

const handleEdit = (meta: MmsMeta) => {
  selectedMeta.value = { ...meta };
  editDialogOpen.value = true;
};

const deleteDialogOpen = ref(false);
const deleteId = ref<number | undefined>(undefined);
const deleteLoading = ref(false);

const openDeleteDialog = (id: number) => {
  deleteId.value = id;
  deleteDialogOpen.value = true;
};

const handleConfirmDelete = async () => {
  if (!deleteId.value) return;

  deleteLoading.value = true;
  try {
    const result = await MmsMetaControllerApi.mmsMetaDeletePost({
      id: deleteId.value,
    });
    if (result.code === 200) {
      deleteDialogOpen.value = false;
      toast({
        title: "删除成功",
        description: "元数据已成功删除",
        toast: "soft-success",
      });
      await fetchData();
    } else {
      toast({
        title: "删除失败",
        description: result.msg || "未知错误",
        toast: "soft-error",
      });
    }
  } catch (error) {
    console.error("Failed to delete meta:", error);
    toast({
      title: "删除失败",
      description: "请求发生错误",
      toast: "soft-error",
    });
  } finally {
    deleteLoading.value = false;
  }
};

const handleSaved = () => {
  fetchData();
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
      <div class="grid w-full gap-2 md:grid-cols-3">
        <NInput v-model="filters.novelId" placeholder="小说ID" type="number" />

        <NInput v-model="filters.novelTitle" placeholder="小说标题" />

        <NInput v-model="filters.novelAuthor" placeholder="作者" />
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
        <NButton
          label="新增"
          btn="solid-primary"
          leading="i-heroicons-plus"
          class="w-full sm:w-auto sm:shrink-0"
          @click="handleCreate"
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
      row-id="id"
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

    <!-- Edit Dialog -->
    <MmsMetaEditDialog
      v-model:open="editDialogOpen"
      :meta="selectedMeta"
      @saved="handleSaved"
    />

    <!-- Delete Confirmation Dialog -->
    <NDialog
      v-model:open="deleteDialogOpen"
      title="确认删除"
      description="确定要删除这条元数据吗？此操作无法撤销。"
    >
      <template #footer>
        <div class="flex justify-end gap-3">
          <NButton
            label="取消"
            btn="ghost-gray"
            @click="deleteDialogOpen = false"
          />
          <NButton
            label="确认删除"
            btn="solid-error"
            :loading="deleteLoading"
            @click="handleConfirmDelete"
          />
        </div>
      </template>
    </NDialog>
  </div>
</template>

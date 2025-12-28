<script setup lang="ts">
import type { ColumnDef, RowSelectionState, Table } from "@tanstack/vue-table";
import type { MmsNovelFile } from "~/api/models";
import { useApi } from "~/api/useApi";
import { formatToYMD } from "~/utils/date";
import { formatBytes } from "~/utils/file";
import { formatWordCount } from "~/utils/number";

const { uMmsNovelApi, mmsNovelApi } = useApi();
const { toast } = useToast();

const data = ref<MmsNovelFile[]>([]);
const loading = ref(false);
const syncing = ref(false);
const total = ref(0);
const pageNo = ref(1);
const pageSize = ref(10);

const filters = reactive({
  novelId: "" as string,
  fileName: "" as string,
});

const columns: ColumnDef<MmsNovelFile>[] = [
  {
    header: "ID",
    accessorKey: "id",
  },
  {
    header: "关联状态",
    accessorKey: "novelId",
    cell: (info) => {
      const novelId = info.row.original.novelId;
      if (novelId) {
        return h(resolveComponent("NButton"), {
          label: "已关联",
          btn: "link-primary",
          size: "sm",
          class: "p-0 h-auto font-normal",
          onClick: () => {
            navigateTo({
              path: "/mms/novels",
              query: { id: novelId.toString() },
            });
          },
        });
      }
      return h("span", { class: "text-muted" }, "未关联");
    },
  },
  {
    header: "文件名称",
    accessorKey: "fileName",
  },
  {
    header: "文件大小",
    accessorKey: "fileSize",
    cell: (info) => formatBytes(info.row.original.fileSize),
  },
  {
    header: "字数",
    accessorKey: "wordCount",
    cell: (info) => formatWordCount(info.row.original.wordCount),
  },
  {
    header: "状态",
    accessorKey: "status",
  },
  {
    header: "更新时间",
    accessorKey: "updateTime",
    cell: (info) => formatToYMD(info.row.original.updateTime),
  },
  {
    header: "操作",
    id: "actions",
    cell: (info) => {
      return h("div", { class: "flex gap-2" }, [
        h(resolveComponent("NButton"), {
          label: "下载",
          btn: "ghost-primary",
          size: "sm",
          loading: info.row.original.id
            ? downloadingIds.value.has(info.row.original.id)
            : false,
          onClick: () => {
            handleDownload(info.row.original);
          },
        }),
      ]);
    },
  },
];

const select = ref<RowSelectionState>();

const table = useTemplateRef<Table<MmsNovelFile>>("table");

const downloadingIds = ref(new Set<string>());

const fetchData = async () => {
  loading.value = true;
  try {
    const result = await uMmsNovelApi.ummsNoevlPageGet({
      operator: "admin",
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      novelId: filters.novelId || undefined,
      fileName: filters.fileName || undefined,
    });

    if (result.code === 200) {
      data.value = result.data?.rows || [];
      total.value = result.data?.total || 0;
    }
  } catch (error) {
    console.error("Failed to fetch novel files:", error);
  } finally {
    loading.value = false;
  }
};

const handleDownload = async (file: MmsNovelFile) => {
  if (!file.id) return;

  downloadingIds.value.add(file.id);
  try {
    const response = await uMmsNovelApi.ummsDownloadMaterialGetRaw({
      mmsNovelFileId: String(file.id),
    });
    const blob = await response.raw.blob();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", file.fileName || "download");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error("Failed to download file:", error);
  } finally {
    downloadingIds.value.delete(file.id);
  }
};

const handleSync = async () => {
  syncing.value = true;
  try {
    const result = await mmsNovelApi.mmsSyncPost();
    if (result.code === 200) {
      toast({
        title: "开始同步任务",
        description: `任务 ID: ${result.data?.jobId}`,
        toast: "soft-success",
        progress: "success",
        showProgress: true,
        closable: true,
      });
      fetchData();
    }
  } catch (error) {
    console.error("Failed to sync novels:", error);
    toast({
      title: "同步失败",
      description: "同步文件时发生错误",
      toast: "soft-error",
      progress: "error",
      showProgress: true,
      closable: true,
    });
  } finally {
    syncing.value = false;
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
        <NInput v-model="filters.novelId" placeholder="小说ID" />

        <NInput v-model="filters.fileName" placeholder="文件名称" />
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
          label="同步"
          btn="solid-primary"
          leading="i-radix-icons-download"
          class="w-full sm:w-auto sm:shrink-0 active:translate-y-0.5"
          :loading="syncing"
          @click="handleSync"
        />
      </div>
    </div>

    <!-- table -->
    <NTable
      ref="table"
      v-model:row-selection="select"
      :loading
      :columns
      :data
      enable-row-selection
      row-id="id"
    >
      <!-- cells -->
      <template #status-cell="{ cell }">
        <NBadge
          :una="{
            badgeDefaultVariant:
              cell.row.original.status === 2
                ? 'badge-soft-success'
                : 'badge-soft-error',
          }"
          class="capitalize"
          :label="cell.row.original.status === 2 ? '正常' : '删除'"
        />
      </template>
      <!-- end cell -->
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
          第 {{ pageNo }} 页，共 {{ Math.ceil(total / pageSize) }} 页
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

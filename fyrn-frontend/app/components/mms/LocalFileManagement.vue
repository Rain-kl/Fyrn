<script setup lang="ts">
import type { ColumnDef, RowSelectionState, Table } from "@tanstack/vue-table";
import type { LocalFileSimpleDto } from "~/api/models";
import { useApi } from "~/api/useApi";
import { formatBytes } from "~/utils/file";
import { formatWordCount } from "~/utils/number";

const { uMmsNovelApi } = useApi();

const data = ref<LocalFileSimpleDto[]>([]);
const loading = ref(false);
const syncing = ref(false);
const total = ref(0);
const pageNo = ref(1);
const pageSize = ref(10);

const columns: ColumnDef<LocalFileSimpleDto>[] = [
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
    header: "文件路径",
    accessorKey: "filePath",
    cell: (info) =>
      h(
        "span",
        { class: "text-xs text-muted break-all" },
        info.row.original.filePath
      ),
  },
];

const select = ref<RowSelectionState>();

const table = useTemplateRef<Table<LocalFileSimpleDto>>("table");

const fetchData = async () => {
  loading.value = true;
  try {
    const result = await uMmsNovelApi.ummsLocalPageGet({
      operator: "admin",
      pageNo: pageNo.value,
      pageSize: pageSize.value,
    });

    if (result.code === 200) {
      data.value = result.data?.rows || [];
      total.value = result.data?.total || 0;
    }
  } catch (error) {
    console.error("Failed to fetch local files:", error);
  } finally {
    loading.value = false;
  }
};

const handleSync = async () => {
  syncing.value = true;
  try {
    const result = await uMmsNovelApi.ummsSyncMaterialGet({
      operator: "admin",
      size: 10,
    });
    if (result.code === 200) {
      fetchData();
    }
  } catch (error) {
    console.error("Failed to sync materials:", error);
  } finally {
    syncing.value = false;
  }
};

watch([pageNo, pageSize], () => {
  fetchData();
});

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
      <div class="flex items-center gap-x-2">
        <h2 class="text-lg font-semibold">未处理本地文件</h2>
        <span class="text-sm text-muted">共 {{ total }} 个文件</span>
      </div>

      <div class="flex items-center gap-x-2 sm:ml-auto">
        <NButton
          label="同步到 OOS"
          btn="solid-primary"
          leading="i-tabler-refresh"
          class="w-full sm:w-auto sm:shrink-0 active:translate-y-0.5"
          :loading="syncing"
          @click="handleSync"
        />
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

    <!-- table -->
    <NTable
      ref="table"
      v-model:row-selection="select"
      :loading
      :columns
      :data
      enable-row-selection
      row-id="fileName"
    />

    <!-- footer -->
    <div class="flex items-center justify-between px-2">
      <div class="hidden text-sm text-muted sm:block">
        已选择
        {{
          table?.getFilteredSelectedRowModel().rows.length.toLocaleString()
        }}
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
            @update:model-value="pageSize = $event as unknown as number"
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

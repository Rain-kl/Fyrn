<script setup lang="ts">
import type { ColumnDef } from "@tanstack/vue-table";
import type { MmsNovel, MmsNovelFile } from "~/api/models";
import { useApi } from "~/api/useApi";
import { formatToYMD } from "~/utils/date";
import { formatBytes } from "~/utils/file";
import { formatWordCount } from "~/utils/number";

const props = defineProps<{
  modelValue: boolean;
  novel: MmsNovel | null;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
}>();

const { mmsNovelApi, uMmsNovelApi } = useApi();

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

const filesLoading = ref(false);
const files = ref<MmsNovelFile[]>([]);
const downloadingIds = ref(new Set<string>());

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

const filesColumns: ColumnDef<MmsNovelFile>[] = [
  {
    header: "文件名",
    accessorKey: "fileName",
  },
  {
    header: "大小",
    accessorKey: "fileSize",
    cell: (info) => formatBytes(info.row.original.fileSize),
  },
  {
    header: "字数",
    accessorKey: "wordCount",
    cell: (info) => formatWordCount(info.row.original.wordCount),
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

const fetchFiles = async () => {
  if (!props.novel?.id) {
    files.value = [];
    return;
  }

  filesLoading.value = true;
  try {
    const result = await mmsNovelApi.mmsFileGet({
      mmsNovelId: String(props.novel.id),
    });
    if (result.code === 200) {
      files.value = result.data || [];
    }
  } catch (error) {
    console.error("Failed to fetch novel files:", error);
  } finally {
    filesLoading.value = false;
  }
};

watch(
  () => props.novel,
  (newNovel) => {
    if (newNovel && props.modelValue) {
      fetchFiles();
    }
  },
  { immediate: true }
);

watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal && props.novel) {
      fetchFiles();
    }
  }
);
</script>

<template>
  <NDialog
    v-model:open="isOpen"
    :title="`物料文件列表：${novel?.novelTitle || ''}`"
  >
    <div class="space-y-3">
      <div class="text-sm text-muted">小说ID：{{ novel?.id }}</div>

      <div v-if="filesLoading" class="text-sm">加载中...</div>

      <div v-else>
        <div v-if="files.length === 0" class="text-sm text-muted">暂无文件</div>

        <div v-else class="overflow-auto">
          <NTable
            :loading="filesLoading"
            :columns="filesColumns"
            :data="files"
            row-id="id"
          />
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex justify-end gap-2">
        <NButton label="关闭" btn="solid-gray" @click="isOpen = false" />
      </div>
    </template>
  </NDialog>
</template>

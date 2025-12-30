<script setup lang="ts">
import type { MmsNovel, MmsNovelFile } from "~/api/models";
import { useApi } from "~/api/useApi";

const props = defineProps<{
  open: boolean;
  file: MmsNovelFile | null;
}>();

const emit = defineEmits<{
  "update:open": [value: boolean];
  "success": [];
}>();

const { mmsNovelApi, uMmsNovelApi } = useApi();
const { toast } = useToast();

const isOpen = computed({
  get: () => props.open,
  set: (value) => emit("update:open", value),
});

const form = reactive({
  title: "",
  author: "",
  novelId: "" as string | undefined,
});

const searchResults = ref<MmsNovel[]>([]);
const isSearching = ref(false);
const isBinding = ref(false);
const isLoadingInfo = ref(false);
const showResults = ref(false);
const isProgrammaticChange = ref(false);

// Watch for file changes to reset form
watch(() => props.open, async (isOpen) => {
  if (isOpen) {
    isProgrammaticChange.value = true;
    form.title = "";
    form.author = "";
    form.novelId = undefined;
    searchResults.value = [];
    showResults.value = false;

    if (props.file?.novelId) {
      isLoadingInfo.value = true;
      try {
        const result = await mmsNovelApi.mmsPageGet({
          operator: "admin",
          novelId: props.file.novelId,
          pageNo: 1,
          pageSize: 1,
        });
        const novel = result.data?.rows?.[0];
        if (result.code === 200 && novel) {
          form.title = novel.novelTitle || "";
          form.author = novel.novelAuthor || "";
          form.novelId = novel.id?.toString();
        }
      } catch (error) {
        console.error("Failed to fetch existing novel info:", error);
      } finally {
        isLoadingInfo.value = false;
      }
    }
    
    nextTick(() => {
      isProgrammaticChange.value = false;
    });
  }
});

const handleSearch = async (query: string) => {
  if (!query || query.length < 1) {
    searchResults.value = [];
    showResults.value = false;
    return;
  }

  isSearching.value = true;
  try {
    const result = await mmsNovelApi.mmsPageGet({
      operator: "admin",
      novelTitle: query,
      pageNo: 1,
      pageSize: 10,
    });

    if (result.code === 200) {
      searchResults.value = result.data?.rows || [];
      showResults.value = searchResults.value.length > 0;
    }
  } catch (error) {
    console.error("Failed to search novels:", error);
  } finally {
    isSearching.value = false;
  }
};

// Debounced search
let searchTimeout: any = null;
watch(() => form.title, (newTitle) => {
  if (isProgrammaticChange.value) return;
  
  if (searchTimeout) clearTimeout(searchTimeout);
  
  // Reset novelId if user starts typing again
  form.novelId = undefined;

  searchTimeout = setTimeout(() => {
    handleSearch(newTitle);
  }, 300);
});

const selectNovel = (novel: MmsNovel) => {
  isProgrammaticChange.value = true;
  form.title = novel.novelTitle || "";
  form.author = novel.novelAuthor || "";
  form.novelId = novel.id?.toString();
  showResults.value = false;
  
  nextTick(() => {
    isProgrammaticChange.value = false;
  });
};

const handleBind = async () => {
  if (!props.file?.id) return;
  if (!form.title) {
    toast({
      title: "错误",
      description: "请输入标题",
      toast: "soft-error",
    });
    return;
  }

  isBinding.value = true;
  try {
    const result = await uMmsNovelApi.ummsNovelBindPost({
      fileId: props.file.id.toString(),
      novelId: form.novelId,
      novelTitle: form.novelId ? undefined : form.title,
      novelAuthor: form.novelId ? undefined : form.author,
    });

    if (result.code === 200) {
      toast({
        title: "绑定成功",
        description: "物料已成功绑定",
        toast: "soft-success",
      });
      isOpen.value = false;
      emit("success");
    } else {
      toast({
        title: "绑定失败",
        description: result.msg || "未知错误",
        toast: "soft-error",
      });
    }
  } catch (error) {
    console.error("Failed to bind novel:", error);
    toast({
      title: "绑定失败",
      description: "请求发生错误",
      toast: "soft-error",
    });
  } finally {
    isBinding.value = false;
  }
};
</script>

<template>
  <NDialog
    v-model:open="isOpen"
    title="绑定物料"
    description="将文件关联到现有的物料或创建新物料"
    class="sm:max-w-md"
  >
    <div v-if="isLoadingInfo" class="flex items-center justify-center py-10">
      <div class="i-tabler-loader-2 animate-spin text-2xl text-primary" />
      <span class="ml-2 text-sm text-muted">加载关联信息...</span>
    </div>
    <div v-else class="space-y-4 py-4">
      <div class="space-y-2">
        <label class="text-sm font-medium">文件名称</label>
        <NInput :model-value="file?.fileName" disabled />
      </div>

      <div class="space-y-2 relative">
        <label class="text-sm font-medium">标题</label>
        <NInput
          v-model="form.title"
          placeholder="输入标题搜索或新增"
          @focus="showResults = searchResults.length > 0"
        />
        <!-- Search Results Dropdown -->
        <div
          v-if="showResults"
          class="absolute z-50 w-full mt-1 bg-popover border border-base rounded-md shadow-lg max-h-60 overflow-auto"
        >
          <div
            v-for="novel in searchResults"
            :key="novel.id"
            class="px-4 py-2 hover:bg-accent cursor-pointer flex flex-col"
            @click="selectNovel(novel)"
          >
            <span class="font-medium">{{ novel.novelTitle }}</span>
            <span class="text-xs text-muted">{{ novel.novelAuthor }} (ID: {{ novel.id }})</span>
          </div>
        </div>
      </div>

      <div class="space-y-2">
        <label class="text-sm font-medium">作者</label>
        <NInput
          v-model="form.author"
          placeholder="输入作者"
          :disabled="!!form.novelId"
        />
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div class="space-y-2">
          <label class="text-sm font-medium">物料 ID</label>
          <NInput :model-value="form.novelId || '自动生成'" disabled />
        </div>
        <div class="space-y-2">
          <label class="text-sm font-medium">创建时间</label>
          <NInput
            :model-value="file?.createTime ? new Date(file.createTime).toLocaleString() : '-'"
            disabled
          />
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <NButton
          label="取消"
          btn="ghost-gray"
          @click="isOpen = false"
        />
        <NButton
          :label="form.novelId ? '确认绑定' : '添加并绑定'"
          btn="solid-primary"
          :loading="isBinding"
          @click="handleBind"
        />
      </div>
    </template>
  </NDialog>
</template>

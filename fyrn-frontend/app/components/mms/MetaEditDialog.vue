<script setup lang="ts">
import type { MmsMeta } from "~/api/models";
import { useApi } from "~/api/useApi";

const { MmsMetaControllerApi } = useApi();

const props = defineProps<{
  open: boolean;
  meta?: MmsMeta;
}>();

const emit = defineEmits<{
  "update:open": [value: boolean];
  saved: [];
}>();

const isOpen = computed({
  get: () => props.open,
  set: (value) => emit("update:open", value),
});

const loading = ref(false);

const form = reactive<MmsMeta>({
  id: props.meta?.id,
  pbn: props.meta?.pbn || "",
  title: props.meta?.title || "",
  author: props.meta?.author || "",
  tag: props.meta?.tag || "",
  summary: props.meta?.summary || "",
  popularity: props.meta?.popularity || 0,
  wordCount: props.meta?.wordCount || 0,
  status: props.meta?.status || 1,
  source: props.meta?.source || "",
  sourceUrl: props.meta?.sourceUrl || "",
  postTime: props.meta?.postTime || "",
  editTime: props.meta?.editTime || "",
});

const statusOptions = [
  { label: "连载中", value: 1 },
  { label: "完结", value: 2 },
];

const handleSave = async () => {
  loading.value = true;
  try {
    if (form.id) {
      // 更新
      const result = await MmsMetaControllerApi.mmsMetaUpdatePost({
        mmsMeta: form,
      });
      if (result.code === 200) {
        emit("saved");
        isOpen.value = false;
      }
    } else {
      // 创建
      const result = await MmsMetaControllerApi.mmsMetaPost({
        mmsMeta: form,
      });
      if (result.code === 200) {
        emit("saved");
        isOpen.value = false;
      }
    }
  } catch (error) {
    console.error("Failed to save meta:", error);
  } finally {
    loading.value = false;
  }
};

const handleClose = () => {
  isOpen.value = false;
  emit("close");
};

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      if (props.meta) {
        Object.assign(form, {
          id: props.meta.id,
          pbn: props.meta.pbn || "",
          title: props.meta.title || "",
          author: props.meta.author || "",
          tag: props.meta.tag || "",
          summary: props.meta.summary || "",
          popularity: props.meta.popularity || 0,
          wordCount: props.meta.wordCount || 0,
          status: props.meta.status || 1,
          source: props.meta.source || "",
          sourceUrl: props.meta.sourceUrl || "",
          postTime: props.meta.postTime || "",
          editTime: props.meta.editTime || "",
        });
      } else {
        // 重置表单为空（新增模式）
        Object.assign(form, {
          id: undefined,
          pbn: "",
          title: "",
          author: "",
          tag: "",
          summary: "",
          popularity: 0,
          wordCount: 0,
          status: 1,
          source: "",
          sourceUrl: "",
          postTime: "",
          editTime: "",
        });
      }
    }
  }
);
</script>

<template>
  <NDialog
    v-model:open="isOpen"
    :title="form.id ? '编辑小说元数据' : '创建小说元数据'"
    class="sm:max-w-2xl"
  >
    <div class="space-y-4 py-4">
      <div class="grid grid-cols-2 gap-4">
        <NFormGroup label="PBN" required>
          <NInput v-model="form.pbn" placeholder="Platform Book Number" />
        </NFormGroup>

        <NFormGroup label="标题" required>
          <NInput v-model="form.title" placeholder="小说标题" />
        </NFormGroup>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <NFormGroup label="作者" required>
          <NInput v-model="form.author" placeholder="作者名称" />
        </NFormGroup>

        <NFormGroup label="标签">
          <NInput v-model="form.tag" placeholder="小说标签" />
        </NFormGroup>
      </div>

      <NFormGroup label="简介">
        <NTextarea v-model="form.summary" placeholder="小说简介" :rows="3" />
      </NFormGroup>

      <div class="grid grid-cols-3 gap-4">
        <NFormGroup label="人气值">
          <NInput
            v-model.number="form.popularity"
            type="number"
            placeholder="0"
          />
        </NFormGroup>

        <NFormGroup label="字数">
          <NInput
            v-model.number="form.wordCount"
            type="number"
            placeholder="0"
          />
        </NFormGroup>

        <NFormGroup label="状态">
          <NSelect
            v-model="form.status"
            :items="statusOptions"
            item-value="value"
            item-label="label"
            placeholder="选择状态"
          />
        </NFormGroup>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <NFormGroup label="来源">
          <NInput v-model="form.source" placeholder="小说来源" />
        </NFormGroup>

        <NFormGroup label="来源链接">
          <NInput v-model="form.sourceUrl" placeholder="来源URL" />
        </NFormGroup>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <NFormGroup label="发布时间">
          <NInput
            v-model="form.postTime"
            type="datetime-local"
            placeholder="发布时间"
          />
        </NFormGroup>

        <NFormGroup label="编辑时间">
          <NInput
            v-model="form.editTime"
            type="datetime-local"
            placeholder="编辑时间"
          />
        </NFormGroup>
      </div>
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <NButton label="取消" btn="ghost-gray" @click="handleClose" />
        <NButton
          label="保存"
          btn="solid-primary"
          :loading="loading"
          @click="handleSave"
        />
      </div>
    </template>
  </NDialog>
</template>

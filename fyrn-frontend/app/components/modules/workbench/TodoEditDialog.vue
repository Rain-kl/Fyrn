<script setup lang="ts">
import { ref, computed, watch, reactive, resolveComponent, h } from "vue";
import dayjs from "dayjs";
import type { WbTask } from "~/api/models/WbTask";
import { useApi } from "~/api/useApi";

const { WbTaskControllerApi } = useApi();
const { toast } = useToast();

const props = defineProps<{
  open: boolean;
  task?: WbTask;
  viewMode?: boolean; // Default to false
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
const loading = ref(false);
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

watch(
  () => props.open,
  (opened) => {
    if (opened) {
      isView.value = !!props.viewMode;
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
      }
    }
  },
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
    } else {
      // Default Status based on logic
      if (payload.type === 0) {
        payload.status = 0; // Task defaults to unstarted
      } else {
        payload.status = 1; // Reminder defaults to doing
      }
      payload.progress = 0;
      await WbTaskControllerApi.wbAddPost({ wbTask: payload });
      toast({ title: "任务已创建", toast: "soft-success" });
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

      <div class="grid grid-cols-2 gap-4">
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

      <div v-if="formData.type === 0" class="grid grid-cols-2 gap-4">

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
      <div v-else class="grid grid-cols-2 gap-4">
        <NFormGroup label="提醒时间" class="col-span-1">
          <CommonDateTimePicker
            v-model="formData.deadline"
            :withTime="true"
            :disabled="isView"
            placeholder="选择提醒时间（精确到分钟）"
          />
        </NFormGroup>
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
</template>

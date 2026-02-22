<script setup lang="ts">
import {
  WORKBENCH_MARKDOWN_THEMES,
  useWorkbenchMarkdownTheme,
} from "~/composables/useWorkbenchSettings";

definePageMeta({
  layout: "workbench",
});

const markdownTheme = useWorkbenchMarkdownTheme();
</script>

<template>
  <ModulesWorkbenchBlock title="工作台设置" class="h-full min-h-[calc(100vh-10rem)]">
    <div class="max-w-2xl space-y-6">
      <div>
        <h3 class="text-base font-semibold">Markdown</h3>
        <p class="text-sm text-muted-foreground mt-1">
          配置工作台内 Markdown 预览主题，查看任务描述时会使用此主题。
        </p>
      </div>

      <NFormGroup label="预览主题">
        <NSelect
          v-model="markdownTheme"
          :items="[...WORKBENCH_MARKDOWN_THEMES]"
          class="w-64"
        />
      </NFormGroup>
    </div>
  </ModulesWorkbenchBlock>
</template>

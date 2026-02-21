<script setup lang="ts">
import { MdEditor, MdPreview, config } from "md-editor-v3";
import "md-editor-v3/lib/style.css";
import "md-editor-v3/lib/preview.css";

const props = defineProps<{
  modelValue: string;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

// Ensure basic Chinese locale config
config({
  editorConfig: {
    languageUserDefined: {
      "zh-CN": {
        toolbarTips: {
          bold: "加粗",
          underline: "下划线",
          italic: "斜体",
          strikeThrough: "删除线",
          title: "标题",
          sub: "下标",
          sup: "上标",
          quote: "引用",
          unorderedList: "无序列表",
          orderedList: "有序列表",
          task: "任务列表",
          codeRow: "行内代码",
          code: "块级代码",
          link: "链接",
          image: "图片",
          table: "表格",
          mermaid: "mermaid图",
          katex: "katex公式",
          revoke: "后退",
          next: "前进",
          save: "保存",
          prettier: "美化",
          pageFullscreen: "浏览器全屏",
          fullscreen: "屏幕全屏",
          preview: "预览",
          htmlPreview: "html代码预览",
          catalog: "目录",
          github: "源码地址",
        },
        titleItem: {
          h1: "一级标题",
          h2: "二级标题",
          h3: "三级标题",
          h4: "四级标题",
          h5: "五级标题",
          h6: "六级标题",
        },
        imgTitleItem: {
          link: "添加链接",
          upload: "上传图片",
          clip2upload: "裁剪上传",
        },
        linkModalTips: {
          linkTitle: "添加链接",
          imageTitle: "添加图片",
          descLabel: "链接描述：",
          descLabelPlaceHolder: "请输入描述...",
          urlLabel: "链接地址：",
          urlLabelPlaceHolder: "请输入链接...",
          buttonOK: "确定",
        },
        clipModalTips: {
          title: "裁剪图片上传",
          buttonUpload: "上传",
        },
        copyCode: {
          text: "复制代码",
          successTips: "已复制！",
          failTips: "复制失败！",
        },
        mermaid: {
          flow: "流程图",
          sequence: "时序图",
          gantt: "甘特图",
          class: "类图",
          state: "状态图",
          pie: "饼图",
          relationship: "关系图",
          journey: "用户旅程",
        },
      },
    },
  },
});
</script>

<template>
  <div
    class="border border-border rounded-md overflow-hidden relative z-0"
    :class="disabled ? 'bg-muted/20' : ''"
  >
    <ClientOnly>
      <MdPreview
        v-if="disabled"
        :model-value="modelValue"
        language="zh-CN"
        class="w-full p-4 min-h-[100px] max-h-[60vh] overflow-y-auto"
      />
      <MdEditor
        v-else
        :model-value="modelValue"
        @update:model-value="(v) => emit('update:modelValue', v)"
        language="zh-CN"
        :preview="false"
        class="h-[300px]"
        :toolbars="[
          'bold',
          'underline',
          'italic',
          '-',
          'title',
          'strikeThrough',
          'quote',
          'unorderedList',
          'orderedList',
          'task',
          '-',
          'codeRow',
          'code',
          'link',
          'image',
          'table',
          '-',
          'revoke',
          'next',
          'save',
          '=',
          'pageFullscreen',
          'fullscreen',
          'preview',
          'catalog',
        ]"
      />
      <template #fallback>
        <div class="h-[300px] flex items-center justify-center text-muted">
          正在加载编辑器...
        </div>
      </template>
    </ClientOnly>
  </div>
</template>

<style scoped>
:deep(.md-editor) {
  --md-bk-color: transparent;
  --md-color: inherit;
  --md-border-color: transparent;
}
</style>

<script setup lang="ts">
interface Props {
  text?: string | null;
  maxLength?: number;
  placeholder?: string;
}

const props = withDefaults(defineProps<Props>(), {
  text: "",
  maxLength: 30,
  placeholder: "-",
});

const isExpanded = ref(false);

const shouldCollapse = computed(() => {
  return props.text && props.text.length > props.maxLength;
});

const displayText = computed(() => {
  if (!props.text) return props.placeholder;
  if (!shouldCollapse.value || isExpanded.value) {
    return props.text;
  }
  return props.text.substring(0, props.maxLength) + "...";
});

const toggleExpand = () => {
  if (shouldCollapse.value) {
    isExpanded.value = !isExpanded.value;
  }
};
</script>

<template>
  <div class="text-collapse">
    <span
      :class="[
        'text-sm',
        isExpanded ? 'whitespace-pre-wrap' : 'truncate',
        shouldCollapse ? 'cursor-pointer hover:text-primary' : '',
      ]"
      @click="toggleExpand"
    >
      {{ displayText }}
    </span>
    <NButton
      v-if="shouldCollapse"
      :label="isExpanded ? '收起' : '展开'"
      btn="link-primary"
      size="xs"
      class="ml-2 p-0 h-auto font-normal"
      @click="toggleExpand"
    />
  </div>
</template>

<style scoped>
.text-collapse {
  display: flex;
  align-items: flex-start;
  gap: 0.25rem;
  max-width: 100%;
}
</style>

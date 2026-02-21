<script setup lang="ts">
defineProps<{
  title: string;
}>();
</script>

<template>
  <div
    class="flex flex-col bg-background rounded-xl border border-border shadow-sm hover:shadow-md transition-shadow duration-300"
  >
    <div class="px-5 py-4 border-b flex items-center justify-between">
      <h3 class="font-semibold text-lg text-foreground tracking-tight">
        {{ title }}
      </h3>
      <slot name="action" />
    </div>
    <div class="p-5 flex-1 flex flex-col">
      <slot />
    </div>
  </div>
</template>

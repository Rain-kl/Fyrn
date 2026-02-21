<template>
  <div class="h-screen w-screen flex flex-col overflow-hidden bg-background">
    <!-- Restoring the global AppHeader with correct z-index to stay above dropdowns internally, but below dialogs -->
    <div class="shrink-0 relative z-40 border-b border-border shadow-sm">
      <div class="container mx-auto px-4 md:px-6 lg:px-8">
        <AppHeader />
      </div>
    </div>

    <!-- Apply transform: translateZ(0) to create a new containing block for position: fixed elements. This prevents the Sidebar from covering the AppHeader. -->
    <div
      class="flex-1 flex flex-col min-h-0 overflow-hidden relative z-10"
      style="transform: translateZ(0)"
    >
      <NSidebarProvider
        :default-open="false"
        class="flex-1 h-full min-h-0 overflow-hidden bg-muted/10"
      >
        <!-- Using modularized Sidebar Component -->
        <ModulesWorkbenchSidebar />

        <!-- Ensure NSidebarInset doesn't overflow to cause body scroll -->
        <NSidebarInset
          class="flex-1 flex flex-col min-h-0 h-full !min-h-0 overflow-hidden bg-transparent"
        >
          <!-- Scrollable main content area -->
          <main class="flex-1 overflow-auto p-4 md:p-6 lg:p-8">
            <div class="max-w-[1600px] mx-auto h-full">
              <slot />
            </div>
          </main>
        </NSidebarInset>
      </NSidebarProvider>
    </div>
  </div>
</template>

<style scoped>
/* Remove margin-bottom from AppHeader locally to make it flush with the border layout */
:deep(.app-header) {
  margin-bottom: 0 !important;
}

/* Ensure una-ui sidebar handles height correctly within the new containing block */
:deep([data-sidebar="sidebar"]) {
  height: 100% !important;
}
</style>

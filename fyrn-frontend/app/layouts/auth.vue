<template>
  <main class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4 py-12 sm:px-6 lg:px-8">
    <div class="w-full max-w-md space-y-8">
        <div class="flex justify-center mb-6">
             <!-- Assuming AppLogo is global or auto-imported -->
            <AppLogo class="h-16" />
        </div>
        <slot />
    </div>
  </main>
</template>

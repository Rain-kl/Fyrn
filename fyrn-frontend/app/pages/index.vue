<template>
  <main class="mx-auto max-w-lg px-4 py-8 lg:px-8 sm:px-6">
    <div class="mx-auto max-w-xl rounded-md space-y-10">
      <div class="flex justify-center">
        <AppLogo class="h-30"/>
      </div>

      <div class="text-center space-y-1">
        <h1 class="text-3xl font-extrabold text-accent">
          Sign in to your account
        </h1>
        <p class="text-sm font-medium text-primary">
          Powered by <NLink to="https://unaui.com" target="_blank">https://unaui.com/</NLink>
        </p>
      </div>

      <div class="space-y-4">
        <!-- <FormLogin /> -->
        <div class="border border-base rounded-md p-8 text-center space-y-4">
             <p class="text-lg font-medium">Welcome to Fyrn System</p>
             <div class="flex justify-center gap-4">
                <NButton to="/oms/auth/login" label="Sign In" leading="i-lucide-log-in" />
                <NButton to="/oms/auth/register" label="Register" btn="outline" leading="i-lucide-user-plus" />
             </div>
        </div>
      </div>
    </div>
  </main>
</template>

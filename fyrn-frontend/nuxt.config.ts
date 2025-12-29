// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  modules: [
    '@una-ui/nuxt',
  ],
  compatibilityDate: '2024-11-01',
  future: {
    compatibilityVersion: 4
  },
  devtools: {
    enabled: true,
    componentInspector: false
  },
  // default settings; you can remove this
  una: {
    prefix: 'N',
    themeable: true,
    global: true,
  },
  colorMode: {
    preference: 'light',
  },
  vite: {
    server: {
      proxy: {
        '/api': {
          target: process.env.PROXY_TARGET || 'http://localhost:45600',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    }
  },
  runtimeConfig: {
    public: {
      apiBase: '/api', // 替换为你的后端地址
    },
  },
})

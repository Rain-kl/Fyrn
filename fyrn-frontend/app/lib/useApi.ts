import { Configuration, MmsNovelControllerApi, UMmsNovelControllerApi } from '~/api'

export const useApi = () => {
  const config = useRuntimeConfig()
  
  const apiConfig = new Configuration({
    basePath: config.public.apiBase as string,
    // 如果需要添加 Token，可以在这里配置
    // accessToken: () => localStorage.getItem('token') || '',
  })

  return {
    mmsNovel: new MmsNovelControllerApi(apiConfig),
    uMmsNovel: new UMmsNovelControllerApi(apiConfig),
  }
}

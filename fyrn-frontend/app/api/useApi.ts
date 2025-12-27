import {
    Configuration,
    MmsNovelControllerApi,
    UMmsNovelControllerApi,
    OmsParameterControllerApi
} from '~/api/index'

export const useApi = () => {
    const config = useRuntimeConfig()

    const apiConfig = new Configuration({
        basePath: config.public.apiBase as string,
        // 如果需要添加 Token，可以在这里配置
        // accessToken: () => localStorage.getItem('token') || '',
    })

    return {
        mmsNovelApi: new MmsNovelControllerApi(apiConfig),
        uMmsNovelApi: new UMmsNovelControllerApi(apiConfig),
        OmsParameterApi: new OmsParameterControllerApi(apiConfig),
    }
}

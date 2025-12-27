import {
    Configuration,
    MmsNovelControllerApi,
    UMmsNovelControllerApi,
    OmsParameterControllerApi,
    JobControllerApi,
    type Middleware
} from '~/api/index'

export const useApi = () => {
    const config = useRuntimeConfig()
    const { toast } = useToast()

    // Error handling middleware
    const errorMiddleware: Middleware = {
        post: async (context) => {
            const response = context.response;
            
            // Check HTTP status first
            if (response && (response.status < 200 || response.status >= 300)) {
                try {
                    const data = await response.clone().json();
                    toast({
                        title: '请求失败',
                        description: data.msg || `错误代码: ${response.status}`,
                        toast: 'soft-warning',
                        progress: 'warning',
                        showProgress: true,
                        closable: true,
                    });
                } catch {
                    toast({
                        title: '请求失败',
                        description: `HTTP ${response.status}: ${response.statusText}`,
                        toast: 'soft-warning',
                        progress: 'warning',
                        showProgress: true,
                        closable: true,
                    });
                }
            } else if (response && response.status === 200) {
                // Check application-level error code
                try {
                    const data = await response.clone().json();
                    if (data.code && data.code !== 200) {
                        toast({
                            title: '操作失败',
                            description: data.msg || `错误代码: ${data.code}`,
                            toast: 'soft-warning',
                            progress: 'warning',
                            showProgress: true,
                            closable: true,
                        });
                    }
                } catch {
                    // Ignore JSON parse errors for non-JSON responses
                }
            }
            return response;
        },
        onError: async (context) => {
            // Handle network errors or other exceptions
            toast({
                title: '网络错误',
                description: '请求失败，请检查网络连接',
                toast: 'soft-warning',
                progress: 'warning',
                showProgress: true,
                closable: true,
            });
            return undefined;
        }
    };

    const apiConfig = new Configuration({
        basePath: config.public.apiBase as string,
        middleware: [errorMiddleware],
        // 如果需要添加 Token，可以在这里配置
        // accessToken: () => localStorage.getItem('token') || '',
    })

    return {
        mmsNovelApi: new MmsNovelControllerApi(apiConfig),
        uMmsNovelApi: new UMmsNovelControllerApi(apiConfig),
        OmsParameterApi: new OmsParameterControllerApi(apiConfig),
        JobControllerApi: new JobControllerApi(apiConfig),
    }
}

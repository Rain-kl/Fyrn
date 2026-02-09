package com.arctel.oms.common.utils;

import com.alibaba.fastjson2.JSON;
import com.arctel.oms.common.constants.ErrorConstant;
import com.arctel.oms.common.exception.BizException;
import okhttp3.*;
import okhttp3.Request.Builder;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HttpSupport {

    private static final String MEDIA_TYPE = "application/json";

    private static final String SEPARATOR = "/";

    /**
     * post请求
     *
     * @param httpUrl
     * @param bodyParams
     * @return
     */
    public static String doPost(String httpUrl, Object bodyParams) {
        return doPost(httpUrl, null, bodyParams);
    }

    /**
     * post请求
     *
     * @param httpUrl
     * @param headers
     * @param bodyParams
     * @return
     */
    public static String doPost(String httpUrl, Map<String, String> headers, Object bodyParams) {
        return doPost(httpUrl, null, headers, bodyParams);
    }

    /**
     * post请求
     *
     * @param httpUrl
     * @param urlParams
     * @param headers
     * @param bodyParams
     * @return
     */
    public static String doPost(String httpUrl, Map<String, Object> urlParams, Map<String, String> headers,
                                Object bodyParams) {
        return doRequest(httpUrl, null, urlParams, RequestMethod.POST, headers, bodyParams);
    }

    /**
     * get请求
     *
     * @param httpUrl
     * @param urlParams
     * @return
     */
    public static String doGet(String httpUrl, Map<String, Object> urlParams) {
        return doGet(httpUrl, null, urlParams);
    }

    /**
     * get请求
     *
     * @param httpUrl
     * @param headers
     * @param urlParams
     * @return
     */
    public static String doGet(String httpUrl, Map<String, String> headers, Map<String, Object> urlParams) {
        return doRequest(httpUrl, null, urlParams, RequestMethod.GET, headers, null);
    }

    /**
     * http请求<br>
     * 1、当get请求时，{@code params}格式只支持对象<br>
     * 2、当post请求时，{@code params}格式支持数组或者对象<br>
     *
     * @param httpUrl
     * @param method
     * @param headers
     * @param params
     * @return
     */
    public static String doRequest(String httpUrl, String method, Map<String, String> headers, Object params) {
        if (RequestMethod.GET.equals(method)) {
            return doGet(httpUrl, headers, (Map<String, Object>) params);
        } else {
            return doPost(httpUrl, headers, params);
        }
    }

    private static String doRequest(String httpUrl, String relativeUri, Map<String, Object> urlParams, String method,
        Map<String, String> headers, Object bodyParams) {
        String url = getUrl(httpUrl, relativeUri, urlParams);
        Request request = buildOkHttpRequest(url, method, headers, bodyParams);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .callTimeout(4, TimeUnit.SECONDS)
                .build();
        Call call = okHttpClient.newCall(request);

        // 执行请求
        try (Response response = call.execute()) {
            if (!response.isSuccessful()) {
                throw new BizException(ErrorConstant.EXTERNAL_SERVICE_ERROR,
                    String.format("接口请求失败! status=%s, message=%s", response.code(), response.message()));
            }

            ResponseBody body = response.body();
            return body == null ? null : body.string();
        } catch (IOException ex) {
            throw new BizException(ErrorConstant.COMMON_ERROR, "接口请求失败!", ex);
        }
    }

    /**
     * 创建请求对象
     *
     * @param url
     * @param method
     * @param headers
     * @param bodyParams
     * @return
     */
    private static Request buildOkHttpRequest(String url, String method, Map<String, String> headers,
                                              Object bodyParams) {
        // 构建请求对象
        Builder builder = new Builder();
        // 设置请求URL
        builder.url(url);
        // 设置请求头
        if (MapUtils.isNotEmpty(headers)) {
            headers.forEach(builder::addHeader);
        }

        // 请求内容
        String content = bodyParams == null ? "{}" : JSON.toJSONString(bodyParams);

        // 设置请求方式和请求内容
        switch (method) {
            case RequestMethod.PUT:
                builder.put(RequestBody.create(MediaType.parse(MEDIA_TYPE), content));
                break;
            case RequestMethod.GET:
                builder.get();
                break;
            case RequestMethod.DELETE:
                builder.delete(RequestBody.create(MediaType.parse(MEDIA_TYPE), content));
                break;
            default:
                builder.post(RequestBody.create(MediaType.parse(MEDIA_TYPE), content));
        }

        return builder.build();
    }

    /**
     * 生成http的url地址
     *
     * @param httpUrl
     * @param relativeUri
     * @param urlParams
     * @return
     */
    private static String getUrl(String httpUrl, String relativeUri, Map<String, Object> urlParams) {
        StringBuilder url = getUrlBuilder(httpUrl, relativeUri);

        if (MapUtils.isNotEmpty(urlParams)) {
            for (Map.Entry<String, Object> urlParam : urlParams.entrySet()) {
                url.append(url.toString().contains("?") ? "&" : "?");
                url.append(urlParam.getKey()).append("=").append(Objects.toString(urlParam.getValue(), ""));
            }
        }
        return url.toString();
    }

    private static StringBuilder getUrlBuilder(String httpUrl, String relativeUri) {
        if (StringUtils.isNotBlank(httpUrl) && StringUtils.isBlank(relativeUri)) {
            return new StringBuilder(httpUrl);
        }

        if (StringUtils.isBlank(httpUrl)) {
            throw new BizException(ErrorConstant.PARAMETER_ERROR, "请求地址不能为空!");
        }
        if (httpUrl.endsWith(SEPARATOR)) {
            httpUrl = httpUrl.substring(0, httpUrl.length() - 1);
        }
        if (StringUtils.isBlank(relativeUri)) {
            throw new BizException(ErrorConstant.PARAMETER_ERROR, "相对URI不能为空!");
        }
        if (!relativeUri.startsWith(SEPARATOR)) {
            relativeUri = SEPARATOR + relativeUri;
        }

        return new StringBuilder(httpUrl + relativeUri);
    }

    /**
     * 请求方法
     */
    interface RequestMethod {
        String POST = "post";
        String PUT = "put";
        String GET = "get";
        String DELETE = "delete";
    }
}
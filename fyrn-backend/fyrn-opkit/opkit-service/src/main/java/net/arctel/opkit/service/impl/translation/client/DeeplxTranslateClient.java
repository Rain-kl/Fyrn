/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.arctel.opkit.service.impl.translation.client;

import jakarta.annotation.Resource;
import net.arctel.platform.framework.exception.BizException;
import net.arctel.platform.oms.common.constants.ErrorConstant;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class DeeplxTranslateClient {

    @Resource
    private HttpClient httpClient;


    /**
     * 方式B：如果不能传 Authorization，就用 URL Param：/translate?token=xxx
     */
    public String translateWithUrlToken(String baseUrl,
                                        String accessToken,
                                        String text,
                                        String sourceLang,
                                        String targetLang) throws Exception {

        String encodedToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        String url;
        if (baseUrl.equals("https://api.deeplx.org")) {
            url = baseUrl + "/" + encodedToken + "/translate";
        } else {
            url = baseUrl + "/translate?token=" + encodedToken;
        }
        String jsonBody = buildJsonBody(text, sourceLang, targetLang);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return send(request);
    }

    private String send(HttpRequest request) throws Exception {
        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 429：频繁请求限流，按你描述会出现
        if (resp.statusCode() == 429) {
            throw new RuntimeException("HTTP 429 Too Many Requests: " + resp.body());
        }
        if (resp.statusCode() >= 500) {
            throw new BizException(ErrorConstant.EXTERNAL_ERROR_PREFIX + resp.statusCode(), resp.body());
        }

        // 其他非 2xx
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());
        }

        // 返回原始 JSON 字符串（你可以再用 Jackson/Gson 解析）
        return resp.body();
    }

    private String buildJsonBody(String text, String sourceLang, String targetLang) {
        // 简单 JSON 转义（足够覆盖常见字符）；生产建议用 Jackson/Gson 构造 JSON
        return "{"
                + "\"text\":\"" + jsonEscape(text) + "\","
                + "\"source_lang\":\"" + jsonEscape(sourceLang) + "\","
                + "\"target_lang\":\"" + jsonEscape(targetLang) + "\""
                + "}";
    }

    private String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }


}

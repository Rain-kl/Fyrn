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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.arctel.platform.framework.exception.BizException;
import net.arctel.platform.oms.common.constants.ErrorConstant;
import net.arctel.platform.oms.common.utils.HttpSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LLMTranslateClient {

    public String chatWithGPT(LLMRequest llmRequest) throws Exception {
        String endpoint = buildEndpoint(llmRequest.getBaseUrl());
        JSONObject body = buildRequestBody(llmRequest);
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        if (llmRequest.getApiKey() != null && !llmRequest.getApiKey().isBlank()) {
            headers.put("Authorization", "Bearer " + llmRequest.getApiKey());
        }

        String responseBody;
        try {
            responseBody = HttpSupport.doPost(endpoint, headers, body);
        } catch (BizException e) {
            throw new BizException(ErrorConstant.EXTERNAL_ERROR_PREFIX + "000", e.getMessage());
        }

        return parseContent(responseBody);
    }

    private String buildEndpoint(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("LLM baseUrl不能为空");
        }
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        // 兼容三种传参：
        // 1) http://host:port
        // 2) http://host:port/v1
        // 3) http://host:port/v1/chat/completions 或 /chat/completions
        if (normalized.endsWith("/v1/chat/completions") || normalized.endsWith("/chat/completions")) {
            return normalized;
        }
        if (normalized.endsWith("/v1")) {
            return normalized + "/chat/completions";
        }
        return normalized + "/v1/chat/completions";
    }

    private JSONObject buildRequestBody(LLMRequest llmRequest) {
        if (llmRequest.getModel() == null || llmRequest.getModel().isBlank()) {
            throw new IllegalArgumentException("LLM model不能为空");
        }
        if (llmRequest.getMessages() == null || llmRequest.getMessages().isEmpty()) {
            throw new IllegalArgumentException("LLM messages不能为空");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("model", llmRequest.getModel());
        jsonObject.put("messages", llmRequest.getMessages());
        return jsonObject;
    }

    private String parseContent(String responseBody) {
        JSONObject jsonObject = JSON.parseObject(responseBody);
        if (jsonObject == null) {
            throw new RuntimeException("LLM返回为空");
        }
        JSONArray choices = jsonObject.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("LLM返回缺少choices");
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        if (firstChoice == null) {
            throw new RuntimeException("LLM返回choices[0]为空");
        }
        JSONObject message = firstChoice.getJSONObject("message");
        if (message == null) {
            throw new RuntimeException("LLM返回缺少message");
        }
        String content = message.getString("content");
        if (content == null) {
            throw new RuntimeException("LLM返回缺少message.content");
        }
        return content.trim();
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LLMRequest {
        public String apiKey;
        public String baseUrl;
        public String model;
        @Builder.Default
        public List<LLMMessage> messages = new ArrayList<>();
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LLMMessage {
        public String role;
        public String content;
    }
}

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

package net.arctel.opkit.service.impl.translation;

import jakarta.annotation.Resource;
import net.arctel.opkit.common.constants.OpkitParameterConstant;
import net.arctel.opkit.common.constants.TransEngineConstant;
import net.arctel.opkit.input.TranslationBaseInput;
import net.arctel.opkit.service.TranslationService;
import net.arctel.opkit.service.impl.translation.client.LLMTranslateClient;
import net.arctel.platform.framework.exception.BizException;
import net.arctel.platform.oms.service.OmsParameterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(TransEngineConstant.LLM)
public class LLMTranslationImpl implements TranslationService {

    @Resource
    OmsParameterService omsParameterService;

    @Resource
    private LLMTranslateClient client;

    @Override
    public String translate(TranslationBaseInput input) {
        @SuppressWarnings("unchecked")
        List<String> llmConfig = omsParameterService.getParamValueByCode(OpkitParameterConstant.LLM_CONFIG, List.class);
        if (llmConfig == null || llmConfig.size() < 3) {
            throw new BizException("缺少参数配置：LLM_CONFIG，格式应为 [\"baseUrl\",\"apiKey\",\"model\"]");
        }

        String baseUrl = llmConfig.get(0);
        String apiKey = llmConfig.get(1);
        String model = llmConfig.get(2);
        if (StringUtils.isBlank(baseUrl) || StringUtils.isBlank(model)) {
            throw new BizException("LLM_CONFIG 参数无效：baseUrl/model 不能为空");
        }

        String sourceLanguage = StringUtils.defaultIfBlank(input.getSourceLanguage(), "auto");
        String targetLanguage = StringUtils.defaultIfBlank(input.getTargetLanguage(), "zh");
        String text = StringUtils.defaultString(input.getText());

        LLMTranslateClient.LLMMessage developerMsg = LLMTranslateClient.LLMMessage.builder()
                .role("developer")
                .content("你是专业翻译助手。只返回翻译后的文本，不要解释。")
                .build();
        LLMTranslateClient.LLMMessage userMsg = LLMTranslateClient.LLMMessage.builder()
                .role("user")
                .content("请把以下文本从 " + sourceLanguage + " 翻译到 " + targetLanguage + "：\n" + text)
                .build();

        try {
            LLMTranslateClient.LLMRequest llmRequest = new LLMTranslateClient.LLMRequest();
            llmRequest.setApiKey(apiKey);
            llmRequest.setBaseUrl(baseUrl);
            llmRequest.setModel(model);
            llmRequest.setMessages(List.of(developerMsg, userMsg));
            return client.chatWithGPT(llmRequest);
        } catch (Exception e) {
            throw new BizException("翻译失败", e);
        }
    }
}

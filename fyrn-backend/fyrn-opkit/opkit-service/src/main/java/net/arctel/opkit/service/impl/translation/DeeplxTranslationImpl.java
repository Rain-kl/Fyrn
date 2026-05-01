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
import net.arctel.platform.framework.exception.BizException;
import net.arctel.platform.oms.service.OmsParameterService;
import net.arctel.opkit.common.constants.OpkitParameterConstant;
import net.arctel.opkit.common.constants.TransEngineConstant;
import net.arctel.opkit.input.TranslationBaseInput;
import net.arctel.opkit.model.TranslationResult;
import net.arctel.opkit.service.TranslationService;
import net.arctel.opkit.service.impl.translation.client.DeeplxTranslateClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(TransEngineConstant.DEEPLX)
public class DeeplxTranslationImpl implements TranslationService {

    @Resource
    OmsParameterService omsParameterService;

    @Resource
    private DeeplxTranslateClient client;

    @Override
    public String translate(TranslationBaseInput input) {
        @SuppressWarnings("unchecked")
        List<String> paramValueByCode = omsParameterService.getParamValueByCode(OpkitParameterConstant.DEEPLX_CONFIG, List.class);
        if (paramValueByCode == null || paramValueByCode.size() != 2) {
            throw new BizException("缺少参数配置：DEEPLX_BASE_URL");
        }
        String token = paramValueByCode.get(1);
        try {
            String resultJson = client.translateWithUrlToken(
                    paramValueByCode.get(0),
                    token,
                    input.getText(),
                    input.getSourceLanguage(),
                    input.getTargetLanguage()
            );
            TranslationResult translationResult = TranslationResult.parseFromJson(resultJson);
            return translationResult.getData();
        } catch (Exception e) {
            throw new BizException("翻译失败", e);
        }
    }
}

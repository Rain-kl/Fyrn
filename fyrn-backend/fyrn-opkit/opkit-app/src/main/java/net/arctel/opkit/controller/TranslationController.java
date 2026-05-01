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

package net.arctel.opkit.controller;


import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import net.arctel.opkit.common.constants.OpkitParameterConstant;
import net.arctel.opkit.input.TranslationBaseInput;
import net.arctel.opkit.service.BaseToolService;
import net.arctel.opkit.service.TranslationService;
import net.arctel.platform.framework.utils.Result;
import net.arctel.platform.oms.service.OmsParameterService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/opkit/translation")
public class TranslationController {

    @Resource
    private TranslationService translationService;

    @Resource
    private Map<String, TranslationService> translationServiceMap;

    @Resource
    private BaseToolService baseToolService;

    @Resource
    OmsParameterService omsParameterService;

    /**
     * 基础翻译接口
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public Result<String> translate(TranslationBaseInput input) {
        String taskId = translationService.translate(input);
        return Result.success(baseToolService.queryResponse(taskId, String.class));
    }

    @PostMapping("/deeplx/{action}")
    public Result<String> translateWithDeeplx(@PathVariable String action, String token, @RequestBody JSONObject input) {
        TranslationBaseInput translationBaseInput = new TranslationBaseInput();
        String transEngine = omsParameterService.getParamValueByCode(OpkitParameterConstant.TRANSLATION_ENGINE);
        translationBaseInput.setTransEngine(transEngine);
        translationBaseInput.setText(input.get("text").toString());
        translationBaseInput.setSourceLanguage(input.get("source_lang").toString());
        translationBaseInput.setTargetLanguage(input.get("target_lang").toString());

        TranslationService deeplxTransService = translationServiceMap.get(transEngine);
        return Result.success(deeplxTransService.translate(translationBaseInput));
    }
}

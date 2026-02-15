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

package net.arctel.agent.opkit.handler;

import jakarta.annotation.Resource;
import net.arctel.framework.core.task.fast.FastTaskHandler;
import net.arctel.oms.infra.task.DefaultTaskMessage;
import net.arctel.opkit.input.TranslationBaseInput;
import net.arctel.opkit.service.TranslationService;
import org.springframework.stereotype.Component;

import java.util.Map;

import static net.arctel.opkit.common.constants.TaskTagConstant.OPKIT_TRANS_TAG;

@Component
public class TranslationHandler extends FastTaskHandler<DefaultTaskMessage> {

    @Resource
    private Map<String, TranslationService> services;


    @Override
    public String getHandlerId() {
        return OPKIT_TRANS_TAG;
    }

    @Override
    public void handleTask(DefaultTaskMessage taskMsg) throws Exception {
        TranslationBaseInput translationBaseInput = DefaultTaskMessage.parseBizValue(taskMsg.getBizValue(), TranslationBaseInput.class);
        if (translationBaseInput != null) {
            TranslationService translationService = services.get(translationBaseInput.getTransEngine());
            if(translationService==null){
                collectResponse("不支持的翻译引擎: " + translationBaseInput.getTransEngine());
            }else {
                String translate = translationService.translate(translationBaseInput);
                collectResponse(translate);
            }
        } else {
            collectResponse("无法解析翻译输入参数");
        }
    }
}

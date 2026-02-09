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

package net.arctel.opkit.service.impl;

import com.arctel.oms.infrastructure.task.DefaultTaskMessage;
import jakarta.annotation.Resource;
import net.arctel.opkit.input.TranslationBaseInput;
import net.arctel.opkit.queue.OpKitProducerQueue;
import net.arctel.opkit.service.TranslationService;
import org.springframework.stereotype.Service;

import static net.arctel.opkit.common.constants.TaskTagConstant.OPKIT_TRANS_TAG;

@Service
public class TranslationServiceImpl extends BaseServiceImpl implements TranslationService {

    @Resource
    OpKitProducerQueue opKitProducerQueue;

    @Override
    public String translate(TranslationBaseInput input) {
        DefaultTaskMessage taskMsg = new DefaultTaskMessage();
        taskMsg = (DefaultTaskMessage) taskMsg.build().buildTaskMessage(OPKIT_TRANS_TAG, "OPKIT", input);
        opKitProducerQueue.push(taskMsg);
        return taskMsg.getTaskId();
    }
}

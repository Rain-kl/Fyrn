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

package net.arctel.fyrn.task;

import net.arctel.framework.core.task.model.RegistrationInfo;
import net.arctel.oms.infra.task.DefaultTaskMessage;
import net.arctel.oms.infra.task.OmsTaskQueue;
import net.arctel.framework.core.task.BaseThreadPoolListener;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import static net.arctel.common.constants.QueueConstant.MMS_QUEUE_NAME;

@Component
public class MmsSvrQueue extends OmsTaskQueue<DefaultTaskMessage> {

    @Resource
    MmsSvrPoolListener dataSyncPoolListener;

    @Override
    public BaseThreadPoolListener<DefaultTaskMessage> getPoolListener() {
        return dataSyncPoolListener;
    }

    @Override
    public RegistrationInfo<DefaultTaskMessage> getRegistrationInfo() {
        return new RegistrationInfo<>(MMS_QUEUE_NAME, "Data Synchronization Task Queue",
                DefaultTaskMessage.class);
    }
}

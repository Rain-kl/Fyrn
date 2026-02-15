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

package net.arctel.framework.core.task.fast;

import lombok.extern.slf4j.Slf4j;
import net.arctel.framework.core.task.BaseThreadPoolHandler;
import net.arctel.framework.core.task.model.BaseTaskMessage;


@Slf4j
public abstract class FastTaskHandler<T extends BaseTaskMessage> extends FastTaskLogger<T> implements BaseThreadPoolHandler<T> {

    protected T taskMessage;

    @Override
    protected T getTaskMessage() {
        return taskMessage;
    }

    @Override
    public void doProcess(T taskMsg) {
        this.taskMessage = taskMsg;
        try {
            handleTask(taskMsg);
            log.info("Handler {} process task {} success", getHandlerId(), taskMsg.getTaskId());
        } catch (Exception e) {
            log.error("Handler {} process task {} failed: {}", getHandlerId(), taskMsg.getTaskId(), e.getMessage(), e);
            collectResponse(e.getMessage() + "Handler " + getHandlerId() + " process task failed: ");
        }
    }

    /**
     * 每个Handler需要实现自己的唯一标识，用于在Listener中注册和分发任务
     */
    public abstract String getHandlerId();

    /**
     * 处理任务的核心方法，具体实现由子类完成
     */
    public abstract void handleTask(T taskMsg) throws Exception;

}

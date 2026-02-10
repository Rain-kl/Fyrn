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

package net.arctel.oms.infra.task;

import net.arctel.oms.entity.OmsTask;
import net.arctel.oms.common.enums.TaskStatusEnum;
import net.arctel.framework.core.task.BaseTaskMessage;
import net.arctel.framework.core.task.BaseThreadPoolHandler;
import net.arctel.oms.input.TaskUpdateInput;
import net.arctel.oms.service.OmsTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract class OmsTaskHandler<T extends BaseTaskMessage> extends OmsTaskLogger<T> implements BaseThreadPoolHandler<T> {

    protected OmsTask omsTask;

    protected T taskMsg;

    @Resource
    public OmsTaskService omsTaskService;

    @Override
    public OmsTask getOmsTask() {
        return omsTask;
    }

    @Override
    public OmsTaskService getOmsTaskService() {
        return omsTaskService;
    }

    @Override
    protected T getTaskMessage() {
        return taskMsg;
    }

    @Override
    public void doProcess(T taskMsg) {
        this.omsTask = omsTaskService.getTaskById(taskMsg.getTaskId());
        omsTaskService.updateTask(
                new TaskUpdateInput(taskMsg.getTaskId(), TaskStatusEnum.RUNNING.getValue()));
        try {
            handleTask(taskMsg);
            omsTaskService.updateTask(
                    new TaskUpdateInput(taskMsg.getTaskId(), TaskStatusEnum.SUCCESS.getValue()));
            log.info("Handler {} process task {} success", getHandlerId(), taskMsg.getTaskId());
        } catch (Exception e) {
            log.error("Handler {} process task {} failed: {}", getHandlerId(), taskMsg.getTaskId(), e.getMessage(), e);
            omsTaskService.updateTask(
                    new TaskUpdateInput(taskMsg.getTaskId(), TaskStatusEnum.FAILED.getValue()));
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

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

package com.arctel.oms.infrastructure.task;

import com.alibaba.fastjson2.JSON;
import com.arctel.oms.domain.entity.OmsTask;
import com.arctel.oms.domain.enums.TaskStatusEnum;
import com.arctel.oms.infrastructure.task.base.BaseTaskMessage;
import com.arctel.oms.infrastructure.task.base.BaseTaskQueue;
import com.arctel.oms.service.OmsTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 任务队列基类
 * 设计: 一个模块一个任务队列，在队列注册监听器
 *
 * @param <T>
 */
@Slf4j
public abstract class OmsTaskQueue<T extends BaseTaskMessage> extends BaseTaskQueue<T> {

    @Resource
    OmsTaskService omsTaskService;

    /**
     * 保存任务信息到数据库，并构建OmsTask对象
     */
    public OmsTask buildOmsTask(T input, String message) {
        OmsTask task = new OmsTask();
        if (StringUtils.isNotBlank(input.getTaskId())) {
            task.setTaskId(input.getTaskId());
        }
        task.setTaskTag(input.getTaskTag());
        task.setAllowRetry(input.getAllowRetry());
        task.setBizTag(input.getBizTag());
        task.setBizValue(input.getBizValue());
        task.setStatus(TaskStatusEnum.PENDING.getValue());
        task.setMessage(message);
        task.setTaskObject(JSON.toJSONString(input));

        task.setStartedTime(new Date());
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());

        omsTaskService.createTask(task);
        if (StringUtils.isBlank(input.getTaskId())) {
            input.setTaskId(task.getTaskId());
        }
        return task;
    }

    @Transactional(rollbackFor = Exception.class)
    public OmsTask createTask(T taskMsg) {
        OmsTask task = buildOmsTask(taskMsg, "Task created.");
        log.info("Created task successfully: {}", JSON.toJSONString(task));
        super.push(taskMsg);
        return task;
    }

    public OmsTask createTask(T taskMsg, String message) {
        OmsTask task = buildOmsTask(taskMsg, message);
        super.push(taskMsg);
        return task;
    }

}

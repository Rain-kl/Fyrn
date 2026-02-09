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

package com.arctel.oms.infrastructure.task.base;

import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.Resource;

public abstract class BaseTaskRspCollector<T extends BaseTaskMessage> {

    @Resource
    public RedisTemplate<String, Object> redisTemplate;

    protected abstract T getTaskMessage();

    /**
     * 收集任务响应结果，存储到Redis中，过期时间7天，同时发送通知
     */
    public void collectResponse(Object response) {
        T taskMessage = getTaskMessage();
        TaskRspCollectorUtil.collectResponse(redisTemplate, taskMessage, response);
    }

    /**
     * 阻塞式获取响应结果
     */
    public Object getResponse() {
        T taskMessage = getTaskMessage();
        return TaskRspCollectorUtil.getResponse(redisTemplate, taskMessage.getTaskId());
    }

    /**
     * 阻塞式获取响应结果（带类型转换）
     */
    public <R> R getResponse(Class<R> clazz) {
        T taskMessage = getTaskMessage();
        return TaskRspCollectorUtil.getResponse(redisTemplate, taskMessage.getTaskId(), clazz);
    }

}

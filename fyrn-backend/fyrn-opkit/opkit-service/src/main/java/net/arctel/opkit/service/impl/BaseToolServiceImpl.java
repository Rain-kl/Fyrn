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

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import net.arctel.framework.core.task.utils.TaskRspCollectorUtil;

import jakarta.annotation.Resource;
import net.arctel.opkit.service.BaseToolService;

@Service
public abstract class BaseToolServiceImpl implements BaseToolService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 阻塞式查询任务结果, 直到结果返回或超时
     *
     * @param taskId 任务 ID
     */
    @Override
    public <R> R queryResponse(String taskId, Class<R> clazz) {
        return TaskRspCollectorUtil.getResponse(redisTemplate, taskId, clazz);
    }
}

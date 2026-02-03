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

import com.arctel.oms.domain.task.BaseTaskMessage;
import com.arctel.oms.domain.task.RegistrationInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;

public abstract class BaseTaskQueue<T extends BaseTaskMessage> implements InitializingBean {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private Class<T> taskMsgClass;

    public static final String TASK_PREFIX = "oms:task:";

    public static final String TASK_QUEUE = TASK_PREFIX + "queue:";

    public static final String TASK_METRICS = TASK_PREFIX + "metrics:";

    private RegistrationInfo<T> registrationInfo;

    /**
     * 获取注册信息, 将任务队列注册到系统中
     */
    public abstract RegistrationInfo<T> getRegistrationInfo();

    @Override
    public void afterPropertiesSet() throws Exception {
        initTaskQueue();
    }

    public void initTaskQueue() {
        register();
    }

    public void register() {
        this.registrationInfo = getRegistrationInfo();
        this.taskMsgClass = registrationInfo.getClazz();
        redisTemplate.opsForHash().put(TASK_METRICS + registrationInfo.getQueueName(), "description", registrationInfo.getDescription());
    }

    /**
     * 添加任务信息
     *
     * @param taskMsg 任务信息
     */
    public Boolean put(T taskMsg) {
        redisTemplate.opsForList().rightPush(TASK_QUEUE + registrationInfo.getQueueName(), taskMsg);
        return null;
    }

    /**
     * 获取任务信息
     */
    public T pop() {
        Object o = redisTemplate.opsForList().rightPop(TASK_QUEUE + registrationInfo.getQueueName());
        return taskMsgClass.cast(o);
    }

    /**
     * 自动消费任务
     */
    void autoConsumption() {

    }

}

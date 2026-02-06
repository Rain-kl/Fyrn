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

import static com.arctel.oms.common.constants.RedisPrefixConstant.TASK_METRICS;
import static com.arctel.oms.common.constants.RedisPrefixConstant.TASK_QUEUE;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务队列基类
 * 设计: 一个模块一个任务队列，在队列注册监听器
 *
 * @param <T>
 */
@Slf4j
public abstract class BaseTaskQueue<T extends BaseTaskMessage> implements InitializingBean {

    private static final Long KEEP_ALIVE_TOLERANCE_FACTOR = 5L;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private RegistrationInfo<T> registrationInfo;

    private TaskQueueConfig queueConfig;

    private BaseThreadPoolListener<T> listener;

    private ScheduledExecutorService scheduledExecutor;


    /**
     * 获取注册信息, 将任务队列注册到系统中
     */
    public abstract RegistrationInfo<T> getRegistrationInfo();

    /**
     * 获取任务队列配置, 默认配置
     */
    public TaskQueueConfig getTaskQueueConfig() {
        return new TaskQueueConfig();
    }

    /**
     * 获取任务池监听器
     */
    public abstract BaseThreadPoolListener<T> getPoolListener();


    @Override
    public void afterPropertiesSet() {
        initTaskQueue();
    }

    public void initTaskQueue() {
        // 获取任务队列配置
        this.queueConfig = getTaskQueueConfig();
        // 任务队列注册
        register();
        // 初始化定时任务线程池
        this.scheduledExecutor = new ScheduledThreadPoolExecutor(1,
                new ThreadFactoryBuilder().setNameFormat(registrationInfo.getQueueName() + "-%d").build());
        // 启动心跳保持
        keepAlive();

        // 启动自动消费任务
        autoConsumption();
    }

    public void register() {
        this.registrationInfo = getRegistrationInfo();
        this.listener = getPoolListener();
    }

    /**
     * 任务队列心跳保持, 定时更新任务队列的存活信息到 Redis
     */
    public void keepAlive() {
        scheduledExecutor.scheduleWithFixedDelay(() -> {
            try {
                String queue_key = TASK_METRICS + registrationInfo.getQueueName();
                redisTemplate.opsForValue().set(queue_key, registrationInfo);
                redisTemplate.expire(queue_key,
                        Duration.ofMinutes(queueConfig.getKeepAliveInterval() * KEEP_ALIVE_TOLERANCE_FACTOR));
            } catch (Throwable e) {
                log.error("Failed to update task queue keep-alive info for queue: {}",
                        registrationInfo.getQueueName(), e);
            }
        }, 0, queueConfig.getKeepAliveInterval(), TimeUnit.SECONDS);
    }

    /**
     * 自动消费任务
     */
    private void autoConsumption() {
        scheduledExecutor.schedule(() -> {
            try {
                T taskMsg = pop();
                if (taskMsg != null) {
                    submit2Listener(taskMsg);
                }
            } catch (Throwable e) {
                log.error("Failed to consume task from queue: {}", registrationInfo.getQueueName(), e);
            } finally {
                autoConsumption();
            }

        }, queueConfig.getWorkingInterval(), TimeUnit.SECONDS);
    }

    private void submit2Listener(T taskMsg) {
        listener.submit2Pool(taskMsg);
    }

    /**
     * 获取队列长度
     *
     */
    public int getQueueSize() {
        Long size = redisTemplate.opsForList().size(TASK_QUEUE + registrationInfo.getQueueName());
        return size != null ? size.intValue() : 0;
    }

    /**
     * 添加任务信息
     *
     * @param taskMsg 任务信息
     */
    public Boolean push(T taskMsg) {
        if (getQueueSize() >= queueConfig.getMaxQueueSize()) {
            log.warn("Task queue {} is full, cannot add new task {}", registrationInfo.getQueueName(), taskMsg.getTaskId());
            return false;
        }
        checkTaskMessage(taskMsg);
        redisTemplate.opsForList().rightPush(TASK_QUEUE + registrationInfo.getQueueName(), taskMsg);
        return true;
    }

    private void checkTaskMessage(T taskMsg) {
        if (StringUtils.isBlank(taskMsg.getTaskId())) {
            throw new IllegalArgumentException("Task message taskId cannot be null or empty");
        }
        if (StringUtils.isBlank(taskMsg.getTaskTag())) {
            throw new IllegalArgumentException("Task message handlerId cannot be null or empty");
        }
    }

    /**
     * 获取任务信息
     */
    public T pop() {
        Object o = redisTemplate.opsForList().rightPop(TASK_QUEUE + registrationInfo.getQueueName());
        return registrationInfo.getTaskMessageClazz().cast(o);
    }


    @Data
    @AllArgsConstructor
    public static class RegistrationInfo<T> {
        private String queueName;
        private String description;
        private Class<T> taskMessageClazz;
    }

    @Data
    @AllArgsConstructor
    public static class TaskQueueConfig {
        /**
         * 任务队列容量
         */
        private Long maxQueueSize;
        /**
         * 心跳保持间隔时间，单位秒
         */
        private Long keepAliveInterval;
        /**
         * 默认工作间隔时间，单位秒
         */
        private Long workingInterval;

        public TaskQueueConfig() {
            this.maxQueueSize = 100L;
            this.keepAliveInterval = 5L;
            this.workingInterval = 1L;
        }
    }


}

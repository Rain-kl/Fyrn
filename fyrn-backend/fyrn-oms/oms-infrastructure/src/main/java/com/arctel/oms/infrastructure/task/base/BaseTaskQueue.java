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

import static com.arctel.oms.common.constants.RedisPrefixConstant.TASK_METRICS_PREFIX;
import static com.arctel.oms.common.constants.RedisPrefixConstant.TASK_STREAM_PREFIX;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import com.alibaba.fastjson2.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务队列基类
 * 设计: 一个模块一个任务队列，在队列注册监听器
 * 使用 Redis Stream 实现阻塞式消费，避免轮询
 *
 * @param <T>
 */
@Slf4j
public abstract class BaseTaskQueue<T extends BaseTaskMessage> implements InitializingBean, DisposableBean {

    private static final Long KEEP_ALIVE_TOLERANCE_FACTOR = 5L;
    private static final String TASK_DATA_FIELD = "data";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private RegistrationInfo<T> registrationInfo;

    private TaskQueueConfig queueConfig;

    private BaseThreadPoolListener<T> listener;

    private ScheduledExecutorService scheduledExecutor;

    /** Stream 消费线程 */
    private Thread consumerThread;

    /** 控制消费线程是否继续运行 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** 当前 Stream 读取位置，初始为 $ (只消费新消息) */
    private volatile String lastReadId = "$";

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
     *
     * @return 如果需要消费任务, 则必须注册监听器, 仅发布任务返回 null
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

        // 如果注册了监听器，则启动心跳保持和自动消费任务
        if (listener != null) {
            // 初始化定时任务线程池 (仅用于心跳)
            this.scheduledExecutor = new ScheduledThreadPoolExecutor(1,
                    new ThreadFactoryBuilder().setNameFormat(registrationInfo.getQueueName() + "-heartbeat-%d")
                            .build());

            // 启动心跳保持
            keepAlive();

            // 启动 Stream 阻塞消费
            startStreamConsumer();
        }
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
                String queue_key = TASK_METRICS_PREFIX + registrationInfo.getQueueName();
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
     * 启动 Stream 阻塞消费线程
     */
    private void startStreamConsumer() {
        running.set(true);
        String streamKey = getStreamKey();

        consumerThread = new Thread(() -> {
            StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
            // 阻塞读取超时时间
            Duration blockTimeout = Duration.ofSeconds(queueConfig.getBlockTimeoutSeconds());

            log.info("Stream consumer started for queue: {}, stream key: {}", registrationInfo.getQueueName(),
                    streamKey);

            while (running.get()) {
                try {
                    // 使用 XREAD BLOCK 阻塞读取
                    StreamReadOptions options = StreamReadOptions.empty()
                            .count(queueConfig.getBatchSize())
                            .block(blockTimeout);

                    @SuppressWarnings("unchecked")
                    List<MapRecord<String, String, String>> records = streamOps.read(
                            options,
                            StreamOffset.create(streamKey, ReadOffset.from(lastReadId)));

                    if (records != null && !records.isEmpty()) {
                        for (MapRecord<String, String, String> record : records) {
                            try {
                                // 更新最后读取的 ID
                                lastReadId = record.getId().getValue();

                                // 解析任务消息
                                String taskData = record.getValue().get(TASK_DATA_FIELD);
                                if (taskData != null) {
                                    T taskMsg = JSON.parseObject(taskData, registrationInfo.getTaskMessageClazz());
                                    if (taskMsg != null) {
                                        submit2Listener(taskMsg);
                                    }
                                }

                                // 删除已处理的消息，控制 Stream 内存占用
                                streamOps.delete(streamKey, record.getId());
                            } catch (Exception e) {
                                log.error("Failed to process stream record {} from queue: {}",
                                        record.getId(), registrationInfo.getQueueName(), e);
                            }
                        }
                    }
                    // 超时返回 null 是正常的，继续循环
                } catch (Exception e) {
                    if (running.get()) {
                        log.error("Stream consumer error for queue: {}, will retry...",
                                registrationInfo.getQueueName(), e);
                        // 发生错误时短暂休眠避免频繁重试
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            log.info("Stream consumer stopped for queue: {}", registrationInfo.getQueueName());
        }, registrationInfo.getQueueName() + "-consumer");

        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    private void submit2Listener(T taskMsg) {
        listener.submit2Pool(taskMsg);
    }

    /**
     * 获取队列长度 (Stream 长度)
     */
    public int getQueueSize() {
        Long size = redisTemplate.opsForStream().size(getStreamKey());
        return size != null ? size.intValue() : 0;
    }

    /**
     * 添加任务信息到 Stream
     *
     * @param taskMsg 任务信息
     */
    public Boolean push(T taskMsg) {
        if (getQueueSize() >= queueConfig.getMaxQueueSize()) {
            log.warn("Task queue {} is full, cannot add new task {}", registrationInfo.getQueueName(),
                    taskMsg.getTaskId());
            return false;
        }
        checkTaskMessage(taskMsg);

        // 使用 XADD 添加到 Stream
        String taskData = JSON.toJSONString(taskMsg);
        redisTemplate.opsForStream().add(
                MapRecord.create(getStreamKey(), Collections.singletonMap(TASK_DATA_FIELD, taskData)));

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
     * 获取 Stream key
     */
    private String getStreamKey() {
        return TASK_STREAM_PREFIX + registrationInfo.getQueueName();
    }

    /**
     * 优雅关闭资源
     */
    @Override
    public void destroy() {
        log.info("Shutting down task queue: {}",
                registrationInfo != null ? registrationInfo.getQueueName() : "unknown");

        // 停止消费线程
        running.set(false);
        if (consumerThread != null) {
            consumerThread.interrupt();
            try {
                consumerThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 关闭定时任务线程池
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
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
         * Stream 阻塞读取超时时间，单位秒
         */
        private Long blockTimeoutSeconds;
        /**
         * 每次读取的批量大小
         */
        private Long batchSize;

        public TaskQueueConfig() {
            this.maxQueueSize = 100L;
            this.keepAliveInterval = 5L;
            this.blockTimeoutSeconds = 5L;
            this.batchSize = 10L;
        }
    }

}

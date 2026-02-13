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

package net.arctel.framework.core.task;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.hutool.core.util.StrUtil;
import net.arctel.framework.core.task.model.BaseTaskMessage;
import net.arctel.framework.core.task.model.RegistrationInfo;
import net.arctel.framework.core.task.model.TaskQueueConfig;
import net.arctel.framework.core.task.utils.TaskRedisUtil;
import net.arctel.framework.dto.ApplicationInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.*;

import com.alibaba.fastjson2.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static net.arctel.framework.constants.RedisPrefixConstant.*;

/**
 * 任务队列基类
 * 设计: 一个模块一个任务队列，在队列注册监听器
 * 使用 Redis Stream 实现阻塞式消费，避免轮询
 *
 * @param <T>
 */
@Slf4j
public abstract class BaseTaskQueue<T extends BaseTaskMessage> implements InitializingBean, DisposableBean {

    private static final Long KEEP_ALIVE_TOLERANCE_FACTOR = 3L;
    private static final String TASK_DATA_FIELD = "data";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ApplicationInfo applicationInfo;

    private RegistrationInfo<T> registrationInfo;

    private TaskQueueConfig queueConfig;

    private BaseThreadPoolListener<T> listener;

    private ScheduledExecutorService scheduledExecutor;

    /**
     * Stream 消费线程
     */
    private Thread consumerThread;

    /**
     * 控制消费线程是否继续运行
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 消费组名称
     */
    private String consumerGroup;

    /**
     * 当前实例的消费者名称
     */
    private String consumerName;

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
            this.scheduledExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat(registrationInfo.getQueueName() + "-heartbeat-%d").build());

            // 初始化消费组与消费者名称
            this.consumerGroup = registrationInfo.getQueueName() + "-group";
            this.consumerName = registrationInfo.getQueueName() + "-" + UUID.randomUUID();
            log.info("Initialized task queue: {}, consumer group: {}, consumer name: {}", registrationInfo.getQueueName(), consumerGroup, consumerName);

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

        String queueKey = TASK_METRICS_QUEUE_PREFIX + registrationInfo.getQueueName();
        String handlerKey = TASK_METRICS_HANDLER_PREFIX + applicationInfo.getAppId();

        scheduledExecutor.scheduleWithFixedDelay(() -> {
            try {
                redisTemplate.opsForValue().set(queueKey, registrationInfo);
                redisTemplate.expire(queueKey, Duration.ofSeconds(queueConfig.getKeepAliveInterval() * KEEP_ALIVE_TOLERANCE_FACTOR));

                redisTemplate.opsForValue().set(handlerKey, listener.getHandlerMap().keySet());
                redisTemplate.expire(handlerKey, Duration.ofSeconds(queueConfig.getKeepAliveInterval() * KEEP_ALIVE_TOLERANCE_FACTOR));

            } catch (Throwable e) {
                log.error("Failed to update task queue keep-alive info for queue: {}", registrationInfo.getQueueName(), e);
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
            ensureConsumerGroup(streamOps, streamKey, consumerGroup);
            // 阻塞读取超时时间
            Duration blockTimeout = Duration.ofSeconds(queueConfig.getBlockTimeoutSeconds());

            log.info("Stream consumer started for queue: {}, stream key: {}", registrationInfo.getQueueName(), streamKey);

            while (running.get()) {
                try {
                    // 使用 XREAD BLOCK 阻塞读取
                    StreamReadOptions options = StreamReadOptions.empty().count(queueConfig.getBatchSize()).block(blockTimeout);

                    @SuppressWarnings("unchecked") List<MapRecord<String, String, String>> records = streamOps.read(Consumer.from(consumerGroup, consumerName), options, StreamOffset.create(streamKey, ReadOffset.lastConsumed()));

                    if (records != null && !records.isEmpty()) {
                        for (MapRecord<String, String, String> record : records) {
                            try {
                                // 解析任务消息
                                String taskData = record.getValue().get(TASK_DATA_FIELD);
                                if (taskData != null) {
                                    T taskMsg = JSON.parseObject(taskData, registrationInfo.getTaskMessageClazz());
                                    if (taskMsg != null) {
                                        submit2Listener(taskMsg);
                                    }
                                }

                                // 确认并删除已处理的消息，避免重复消费
                                streamOps.acknowledge(streamKey, consumerGroup, record.getId());
                                streamOps.delete(streamKey, record.getId());
                            } catch (Exception e) {
                                log.error("Failed to process stream record {} from queue: {}", record.getId(), registrationInfo.getQueueName(), e);
                            }
                        }
                    }
                    // 超时返回 null 是正常的，继续循环
                } catch (Exception e) {
                    if (running.get()) {
                        log.error("Stream consumer error for queue: {}, will retry...", registrationInfo.getQueueName(), e);
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
     * 确保消费组存在，如果不存在则创建
     *
     * @param streamOps Stream 操作对象
     * @param streamKey Stream key
     * @param group     消费组名称
     */
    private void ensureConsumerGroup(StreamOperations<String, String, String> streamOps, String streamKey, String group) {
        if (!queueConfig.isCreateGroupOnStart()) {
            return;
        }
        try {
            streamOps.createGroup(streamKey, resolveGroupReadOffset(), group);
        } catch (Exception e) {
            // 如果消费组已存在，继续执行；如果 Stream 不存在，则先添加一条消息再创建消费组
            if (StrUtil.containsIgnoreCase(e.getCause().toString(), "BUSYGROUP")) {
                return;
            }
            log.error("Failed to ensure stream consumer group for queue: {}", registrationInfo.getQueueName(), e);
        }
    }

    /**
     * 解析消费组的初始读取位置
     */
    private ReadOffset resolveGroupReadOffset() {
        String lastReadId = queueConfig.getLastReadId();
        // 默认从头开始消费，如果配置为 LAST_READ_NEW_MESSAGES 则只消费新消息
        if (TaskQueueConfig.LAST_READ_NEW_MESSAGES.equals(lastReadId)) {
            return ReadOffset.latest();
        }
        return ReadOffset.from(lastReadId);
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
            log.warn("Task queue {} is full, cannot add new task {}", registrationInfo.getQueueName(), taskMsg.getTaskId());
            return false;
        }
        checkTaskMessage(taskMsg);

        // 使用 XADD 添加到 Stream
        String taskData = JSON.toJSONString(taskMsg);
        redisTemplate.opsForStream().add(MapRecord.create(getStreamKey(), Collections.singletonMap(TASK_DATA_FIELD, taskData)));

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
        log.info("Shutting down task queue: {}", registrationInfo != null ? registrationInfo.getQueueName() : "unknown");

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

}

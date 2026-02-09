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

import static com.arctel.oms.common.constants.RedisPrefixConstant.TASK_RSP_NOTIFY_PREFIX;
import static com.arctel.oms.common.constants.RedisPrefixConstant.TASK_VALUE_PREFIX;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.alibaba.fastjson2.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 * 任务响应收集工具类
 * 使用 Redis Pub/Sub 实现阻塞式获取响应，避免轮询
 */
@Slf4j
public class TaskRspCollectorUtil {

    /** 默认等待超时时间（秒） */
    private static final long DEFAULT_TIMEOUT_SECONDS = 30L;

    /** 响应结果过期时间（天） */
    private static final long RESPONSE_EXPIRE_DAYS = 7L;

    /**
     * 收集任务响应结果，存储到Redis并发布通知
     */
    public static <T extends BaseTaskMessage> void collectResponse(
            RedisTemplate<String, Object> redisTemplate, T taskMessage, Object response) {
        String redisKey = TASK_VALUE_PREFIX + taskMessage.getTaskId();
        String notifyChannel = TASK_RSP_NOTIFY_PREFIX + taskMessage.getTaskId();

        // 存储响应结果并设置过期时间
        redisTemplate.opsForValue().set(redisKey, response, RESPONSE_EXPIRE_DAYS, TimeUnit.DAYS);

        // 发布通知
        redisTemplate.convertAndSend(notifyChannel, "1");
    }

    /**
     * 阻塞式获取响应结果，使用 Pub/Sub 等待通知
     * 获取后立即删除 Redis 中的响应数据
     *
     * @param redisTemplate     Redis 模板
     * @param listenerContainer Redis 消息监听容器（需要从 Spring 容器获取）
     * @param taskId            任务 ID
     * @return 响应结果，超时返回 null
     */
    public static Object getResponse(
            RedisTemplate<String, Object> redisTemplate,
            RedisMessageListenerContainer listenerContainer,
            String taskId) {
        return getResponse(redisTemplate, listenerContainer, taskId, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 阻塞式获取响应结果，使用 Pub/Sub 等待通知
     * 获取后立即删除 Redis 中的响应数据
     *
     * @param redisTemplate     Redis 模板
     * @param listenerContainer Redis 消息监听容器
     * @param taskId            任务 ID
     * @param timeoutSeconds    超时时间（秒）
     * @return 响应结果，超时返回 null
     */
    public static Object getResponse(
            RedisTemplate<String, Object> redisTemplate,
            RedisMessageListenerContainer listenerContainer,
            String taskId,
            long timeoutSeconds) {

        String redisKey = TASK_VALUE_PREFIX + taskId;

        // 1. 先检查结果是否已存在，获取并删除
        Object existingResult = getAndDelete(redisTemplate, redisKey);
        if (existingResult != null) {
            return existingResult;
        }

        // 2. 结果不存在，使用 Pub/Sub 阻塞等待
        String notifyChannel = TASK_RSP_NOTIFY_PREFIX + taskId;
        ChannelTopic topic = new ChannelTopic(notifyChannel);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Object> resultRef = new AtomicReference<>();

        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                // 收到通知后获取并删除结果
                Object result = getAndDelete(redisTemplate, redisKey);
                resultRef.set(result);
                latch.countDown();
            }
        };

        try {
            // 注册监听
            listenerContainer.addMessageListener(listener, topic);

            // 在注册后再次检查，避免竞态条件
            Object result = getAndDelete(redisTemplate, redisKey);
            if (result != null) {
                return result;
            }

            // 阻塞等待通知或超时
            boolean received = latch.await(timeoutSeconds, TimeUnit.SECONDS);
            if (received) {
                return resultRef.get();
            } else {
                // 超时，最后再检查一次
                return getAndDelete(redisTemplate, redisKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for task response: {}", taskId);
            return null;
        } finally {
            // 确保移除监听器，避免内存泄漏
            listenerContainer.removeMessageListener(listener, topic);
        }
    }

    /**
     * 获取响应结果（带类型转换）
     */
    public static <R> R getResponse(
            RedisTemplate<String, Object> redisTemplate,
            RedisMessageListenerContainer listenerContainer,
            String taskId,
            Class<R> clazz) {
        Object response = getResponse(redisTemplate, listenerContainer, taskId);
        if (response == null) {
            return null;
        }
        return JSON.parseObject(JSON.toJSONString(response), clazz);
    }

    /**
     * 获取并删除 Redis 中的值
     */
    private static Object getAndDelete(RedisTemplate<String, Object> redisTemplate, String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            redisTemplate.delete(key);
        }
        return value;
    }

    // ==================== 向后兼容的方法 ====================

    /**
     * @deprecated 使用
     *             {@link #getResponse(RedisTemplate, RedisMessageListenerContainer, String)}
     *             替代
     *             保留此方法以兼容旧代码，内部改为简单等待实现
     */
    @Deprecated
    public static Object getResponse(RedisTemplate<String, Object> redisTemplate, String taskId) {
        String redisKey = TASK_VALUE_PREFIX + taskId;

        // 使用简单的阻塞等待，因为没有 listenerContainer
        for (int i = 0; i < 30; i++) {
            Object result = getAndDelete(redisTemplate, redisKey);
            if (result != null) {
                return result;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }

    /**
     * @deprecated 使用
     *             {@link #getResponse(RedisTemplate, RedisMessageListenerContainer, String, Class)}
     *             替代
     */
    @Deprecated
    public static <R> R getResponse(RedisTemplate<String, Object> redisTemplate, String taskId, Class<R> clazz) {
        Object response = getResponse(redisTemplate, taskId);
        if (response == null) {
            return null;
        }
        return JSON.parseObject(JSON.toJSONString(response), clazz);
    }

}

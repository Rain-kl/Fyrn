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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.arctel.oms.common.constants.ErrorConstant;
import com.arctel.oms.common.exception.BizException;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.fastjson2.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 * 任务响应收集工具类
 * 使用 Redis BLPOP 实现阻塞式获取响应，避免轮询
 * 高并发安全，不使用 Pub/Sub 避免订阅状态冲突
 */
@Slf4j
public class TaskRspCollectorUtil {

    /** 默认等待超时时间（秒） */
    private static final long DEFAULT_TIMEOUT_SECONDS = 20L;

    /** 响应结果过期时间（天） */
    private static final long RESPONSE_EXPIRE_DAYS = 7L;

    /**
     * 收集任务响应结果，存储到Redis并推送通知
     * 使用 List RPUSH 发送通知，配合 BLPOP 实现阻塞等待
     */
    public static <T extends BaseTaskMessage> void collectResponse(
            RedisTemplate<String, Object> redisTemplate, T taskMessage, Object response) {
        String redisKey = TASK_VALUE_PREFIX + taskMessage.getTaskId();
        String notifyKey = TASK_RSP_NOTIFY_PREFIX + taskMessage.getTaskId();

        // 存储响应结果并设置过期时间
        redisTemplate.opsForValue().set(redisKey, response, RESPONSE_EXPIRE_DAYS, TimeUnit.DAYS);

        // 推送通知到 List（BLPOP 将收到此通知）
        redisTemplate.opsForList().rightPush(notifyKey, "1");
        // 设置通知 key 的过期时间，防止内存泄漏
        redisTemplate.expire(notifyKey, Duration.ofMinutes(5));
    }

    /**
     * 阻塞式获取响应结果，使用 BLPOP 等待通知
     * 获取后立即删除 Redis 中的响应数据
     * 线程安全，适用于高并发场景
     *
     * @param redisTemplate Redis 模板
     * @param taskId        任务 ID
     * @return 响应结果，超时返回 null
     */
    public static Object getResponse(RedisTemplate<String, Object> redisTemplate, String taskId) {
        return getResponse(redisTemplate, taskId, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 阻塞式获取响应结果，使用 BLPOP 等待通知
     * 获取后立即删除 Redis 中的响应数据
     *
     * @param redisTemplate  Redis 模板
     * @param taskId         任务 ID
     * @param timeoutSeconds 超时时间（秒）
     * @return 响应结果，超时返回 null
     */
    public static Object getResponse(
            RedisTemplate<String, Object> redisTemplate,
            String taskId,
            long timeoutSeconds) {

        String redisKey = TASK_VALUE_PREFIX + taskId;
        String notifyKey = TASK_RSP_NOTIFY_PREFIX + taskId;

        // 1. 先检查结果是否已存在
        Object existingResult = getAndDelete(redisTemplate, redisKey);
        if (existingResult != null) {
            // 清理可能已存在的通知
            redisTemplate.delete(notifyKey);
            return existingResult;
        }

        // 2. 使用 BLPOP 阻塞等待通知
        try {
            Object notification = redisTemplate.opsForList().leftPop(
                    notifyKey, Duration.ofSeconds(timeoutSeconds));

            if (notification != null) {
                // 收到通知，获取并删除结果
                return getAndDelete(redisTemplate, redisKey);
            } else {
                throw new BizException(ErrorConstant.TIMEOUT_ERROR,"响应超时，未收到任务结果");
            }
        } catch (Exception e) {
            log.warn("Error while waiting for task response: {}, error: {}", taskId, e.getMessage());
            // 发生错误时尝试获取已有结果
            return getAndDelete(redisTemplate, redisKey);
        }
    }

    /**
     * 获取响应结果（带类型转换）
     */
    public static <R> R getResponse(
            RedisTemplate<String, Object> redisTemplate,
            String taskId,
            Class<R> clazz) {
        Object response = getResponse(redisTemplate, taskId);
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

}

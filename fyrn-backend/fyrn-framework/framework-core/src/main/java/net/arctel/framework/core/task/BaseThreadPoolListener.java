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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import net.arctel.framework.core.task.model.BaseTaskMessage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 线程池监听器基类
 * 设计: 一个模块一个线程池监听器，监听器内可以注册多个处理器
 *
 * @param <T>
 */
@Slf4j
public abstract class BaseThreadPoolListener<T extends BaseTaskMessage> implements InitializingBean {

    @Autowired(required = false)
    private List<BaseThreadPoolHandler<T>> handlers;

    private ThreadPoolExecutor pool;

    private ThreadPoolConfig config;

    @Getter
    private final Map<String, BaseThreadPoolHandler<T>> handlerMap = new HashMap<>();


    @Override
    public void afterPropertiesSet() {
        initializePool();
        initHandlers();
    }

    public void initializePool() {
        this.config = getTaskQueueConfig();
        this.pool = new ThreadPoolExecutor(config.getCorePoolSize(), config.getMaxPoolSize(),
                1000, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(config.getPoolQueueSize()),
                new ThreadFactoryBuilder().setNameFormat(config.getTag() + "-%d").build());
        this.pool.allowCoreThreadTimeOut(true);
    }

    private void initHandlers() {
        // handler 的handlerId 不能重复
        Set<String> handlerIds = new java.util.HashSet<>();
        if (handlers != null) {
            handlers.forEach(handler -> {
                if (handler.getHandlerId() == null || handler.getHandlerId().isEmpty()) {
                    throw new IllegalArgumentException("handlerId cannot be null or empty");
                }
                if (!handlerIds.add(handler.getHandlerId())) {
                    throw new IllegalArgumentException("Duplicate handlerId: " + handler.getHandlerId());
                }
                this.handlerMap.put(handler.getHandlerId(), handler);
            });
        }

    }

    /**
     * 初始化任务队列
     */
    public ThreadPoolConfig getTaskQueueConfig() {
        return new ThreadPoolConfig();
    }

    /**
     * 提供外部接口，提交任务到线程池
     */
    public void submit2Pool(T taskMsg) {
        try {
            doExecute(taskMsg);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 执行具体的任务逻辑
     *
     * @param taskMsg 任务消息, 根据消息调度具体的业务处理
     */
    public void doExecute(T taskMsg) {

        Runnable wrapper = () -> {
            try {
                this.handlerMap.get(taskMsg.getTaskTag()).doProcess(taskMsg);
            } catch (Exception e) {
                log.error("Handler {} process task {} failed: {}", taskMsg.getTaskTag(), taskMsg.getTaskId(), e.getMessage(), e);
            }
        };

        Future<?> future = pool.submit(wrapper);

    }


    @Data
    @AllArgsConstructor
    public static class ThreadPoolConfig {
        private int corePoolSize;
        private int maxPoolSize;
        private int poolQueueSize;
        private String tag;

        public ThreadPoolConfig() {
            this("ThreadPool");
        }

        public ThreadPoolConfig(String tag) {
            this(10, 50, 1000, tag);
        }
    }

}

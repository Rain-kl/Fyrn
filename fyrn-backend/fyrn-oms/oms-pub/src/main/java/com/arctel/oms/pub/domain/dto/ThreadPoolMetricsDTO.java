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

package com.arctel.oms.pub.domain.dto;

import lombok.Data;

import java.util.concurrent.ThreadPoolExecutor;

@Data
public class ThreadPoolMetricsDTO {

    /**
     * 核心线程数
     */
    private int corePoolSize;

    /**
     * 最大线程数
     */
    private int maxPoolSize;

    /**
     * 活跃线程数
     */
    private int activeCount;

    /**
     * 当前线程池大小
     */
    private int poolSize;

    /**
     * 队列大小
     */
    private int queueSize;

    /**
     * 已完成任务数
     */
    private long completedTaskCount;

    /**
     * 历史最大线程数
     */
    private int largestPoolSize;

    /**
     * 采集时间戳
     */
    private long timestamp;

    /**
     * 采集线程池指标
     * @param executor 线程池执行器
     * @return 线程池指标DTO
     */
    public static ThreadPoolMetricsDTO collectMetrics(ThreadPoolExecutor executor) {
        ThreadPoolMetricsDTO dto = new ThreadPoolMetricsDTO();
        dto.setCorePoolSize(executor.getCorePoolSize());
        dto.setMaxPoolSize(executor.getMaximumPoolSize());
        dto.setActiveCount(executor.getActiveCount());
        dto.setPoolSize(executor.getPoolSize());
        dto.setQueueSize(executor.getQueue().size());
        dto.setCompletedTaskCount(executor.getCompletedTaskCount());
        dto.setLargestPoolSize(executor.getLargestPoolSize());
        dto.setTimestamp(System.currentTimeMillis());
        return dto;
    }
}
package com.arctel.oms.domain.dto;

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
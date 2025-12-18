package com.arctel.oms.support;

import com.arctel.oms.domain.dto.ThreadPoolMetricsDTO;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static com.arctel.oms.common.constants.RedisPrefixConstant.DEFAULT_THREAD_POOL_PREFIX;

@Component
public class LocalThreadPoolTaskSupport {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Getter
    @Resource
    @Qualifier("defaultThreadPool")
    public ThreadPoolTaskExecutor taskExecutor;

    private static final String THREAD_POOL_METRICS = DEFAULT_THREAD_POOL_PREFIX + "metrics:";

    /**
     * 采集当前线程池的运行时指标并写入 Redis（Hash 结构）
     */
    @Scheduled(fixedDelay = 1000)
    public void collectAndPublishMetrics() {
        ThreadPoolExecutor executor = taskExecutor.getThreadPoolExecutor();

        // 1. 采集指标到 DTO
        ThreadPoolMetricsDTO metricsDto = ThreadPoolMetricsDTO.collectMetrics(executor);

        // 2. 转为 Map<String, String>
        Map<String, String> metricsMap = new HashMap<>();
        metricsMap.put("corePoolSize", String.valueOf(metricsDto.getCorePoolSize()));
        metricsMap.put("maxPoolSize", String.valueOf(metricsDto.getMaxPoolSize()));
        metricsMap.put("activeCount", String.valueOf(metricsDto.getActiveCount()));
        metricsMap.put("poolSize", String.valueOf(metricsDto.getPoolSize()));
        metricsMap.put("queueSize", String.valueOf(metricsDto.getQueueSize()));
        metricsMap.put("completedTaskCount", String.valueOf(metricsDto.getCompletedTaskCount()));
        metricsMap.put("largestPoolSize", String.valueOf(metricsDto.getLargestPoolSize()));
        metricsMap.put("timestamp", String.valueOf(metricsDto.getTimestamp()));

        // 写入 Redis Hash，设置过期时间（例如 5 分钟）
        redisTemplate.opsForHash().putAll(THREAD_POOL_METRICS, metricsMap);
        redisTemplate.expire(THREAD_POOL_METRICS, Duration.ofMinutes(5));
    }

}

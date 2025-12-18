package com.arctel.oms.support;

import com.arctel.oms.common.base.BaseQueryPage;
import com.arctel.oms.common.constants.JobStatusEnum;
import com.arctel.oms.common.utils.Result;
import com.arctel.oms.domain.OmsJob;
import com.arctel.oms.domain.dto.JobRunnable;
import com.arctel.oms.domain.dto.ThreadPoolMetricsDTO;
import com.arctel.oms.domain.input.*;
import com.arctel.oms.service.OmsJobService;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static com.arctel.oms.common.constants.RedisPrefixConstant.DEFAULT_THREAD_POOL_PREFIX;

@Component
public class ThreadPoolJobSupport extends JobSupport {

    @Resource
    OmsJobService omsJobService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    @Qualifier("defaultThreadPool")
    private ThreadPoolTaskExecutor taskExecutor;

    // 存储 taskId -> Future 的映射，线程安全
    private final Map<String, Future<?>> taskFutures = new ConcurrentHashMap<>();


    /**
     * 创建新任务
     *
     * @param input
     * @return
     */
    public Result<OmsJob> createJob(CreateJobInput input, JobRunnable task) {
        OmsJob omsJob = omsJobService.createJob(input);
        Runnable wrapper = () -> {
            try {
                omsJobService.updateJob(
                        new UpdateJobInput(omsJob.getJobId(),
                                JobStatusEnum.RUNNING.getValue(),
                                "Task is running")
                );
                task.run();
                omsJobService.updateJob(
                        new UpdateJobInput(omsJob.getJobId(),
                                JobStatusEnum.SUCCESS.getValue(),
                                "Task completed successfully")
                );
            } finally {
                // 任务完成后移除记录
                taskFutures.remove(omsJob.getJobId());
            }
        };

        String jobId = omsJob.getJobId();
        if (taskFutures.containsKey(jobId)) {
            throw new IllegalArgumentException("Job ID already exists: " + jobId);
        }

        Future<?> future = taskExecutor.submit(wrapper);
        taskFutures.put(jobId, future);
        return Result.success(omsJob);

    }

    /**
     * 取消指定任务
     *
     * @param jobId                 任务ID
     * @param mayInterruptIfRunning 是否中断正在运行的任务
     * @return true 表示成功取消（或任务已完成/已取消）
     */
    public boolean cancelTask(String jobId, boolean mayInterruptIfRunning) {
        Future<?> future = taskFutures.get(jobId);
        if (future != null) {
            boolean result = future.cancel(mayInterruptIfRunning);
            // 如果取消成功或任务已完成，移除记录
            if (result || future.isDone()) {
                taskFutures.remove(jobId);
            }
            return result;
        }
        return false;
    }

    public boolean completeTask(String jobId) {
        Future<?> future = taskFutures.get(jobId);
        if (future != null) {
            if (future.isDone()) {
                taskFutures.remove(jobId);
                return true;
            }
        }
        return false;
    }

}

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

package com.arctel.oms.biz;

import com.arctel.oms.biz.job.JobRunnable;
import com.arctel.oms.biz.job.OmsJobService;
import com.arctel.oms.biz.job.ThreadPoolJobService;
import com.arctel.oms.pub.constants.JobStatusEnum;
import com.arctel.oms.pub.domain.OmsJob;
import com.arctel.oms.pub.domain.input.CreateJobInput;
import com.arctel.oms.pub.domain.input.UpdateJobInput;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Service
public class ThreadPoolJobServiceImpl extends OmsJobServiceImpl implements ThreadPoolJobService {
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
     * @param input 创建任务输入参数
     * @param task 任务逻辑
     * @return 创建的任务对象
     */
    public OmsJob createJob(CreateJobInput input, JobRunnable task) {
        OmsJob omsJob = omsJobService.createJob(input);
        Runnable wrapper = () -> {
            try {
                omsJobService.updateJob(
                        new UpdateJobInput(omsJob.getJobId(),
                                JobStatusEnum.RUNNING.getValue(),
                                "Task is running")
                );

                task.run(omsJob);
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
        return omsJob;
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

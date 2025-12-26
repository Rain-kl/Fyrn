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

package com.arctel.oms.support;

import com.arctel.oms.biz.job.JobRunnable;
import com.arctel.oms.biz.job.ThreadPoolJobService;

import com.arctel.oms.pub.domain.OmsJob;
import com.arctel.oms.pub.domain.input.CreateJobInput;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Component
public class ThreadPoolJobSupport {

    @Resource
    ThreadPoolJobService threadPoolJobService;


    // 存储 taskId -> Future 的映射，线程安全
    private final Map<String, Future<?>> taskFutures = new ConcurrentHashMap<>();


    /**
     * 创建新任务
     *
     * @param input
     * @return
     */
    public OmsJob createJob(CreateJobInput input, JobRunnable task) {
        return threadPoolJobService.createJob(input, task);
    }

    /**
     * 取消指定任务
     *
     * @param jobId                 任务ID
     * @param mayInterruptIfRunning 是否中断正在运行的任务
     * @return true 表示成功取消（或任务已完成/已取消）
     */
    public boolean cancelTask(String jobId, boolean mayInterruptIfRunning) {
        return threadPoolJobService.cancelTask(jobId, mayInterruptIfRunning);
    }

    public boolean completeTask(String jobId) {
        return threadPoolJobService.completeTask(jobId);
    }

}

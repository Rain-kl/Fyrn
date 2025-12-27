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

package com.arctel.oms.biz.job;


import com.arctel.oms.pub.domain.OmsJob;
import com.arctel.oms.pub.domain.dto.JobProgressDto;
import com.arctel.oms.pub.domain.input.UpdateJobProgressInput;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class JobRunnable {

    protected OmsJob omsJob;

    ThreadPoolJobService threadPoolJobService;

    private final AtomicInteger currentProgress = new AtomicInteger(0);

    public JobRunnable(ThreadPoolJobService threadPoolJobService) {
        this.threadPoolJobService = threadPoolJobService;
    }

    public void updateLog(String logMessage) {
        threadPoolJobService.updateLog(omsJob.getJobId(), logMessage);
    }

    public void updateProgress(int current, int total, String logMessage) {
        currentProgress.set(current);
        threadPoolJobService.updateJobProgress(new UpdateJobProgressInput(
                omsJob.getJobId(), logMessage, new JobProgressDto(currentProgress.incrementAndGet(), total)
        ));
    }

    public void updateProgress(int total, String logMessage) {
        threadPoolJobService.updateJobProgress(new UpdateJobProgressInput(
                omsJob.getJobId(), logMessage, new JobProgressDto(currentProgress.incrementAndGet(), total)
        ));
    }

    public void updateProgress(String logMessage) {
        threadPoolJobService.updateJobProgress(new UpdateJobProgressInput(
                omsJob.getJobId(), logMessage, new JobProgressDto(currentProgress.incrementAndGet(), 0)
        ));
    }

    protected abstract void taskRun();


    public void run(OmsJob omsJob) {
        this.omsJob = omsJob;
        taskRun();
    }
}
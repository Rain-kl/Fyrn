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

package com.arctel.biz.impl;

import com.arctel.oms.biz.job.JobRunnable;
import com.arctel.oms.biz.job.ThreadPoolJobService;
import com.arctel.oms.pub.constants.JobStatusEnum;
import com.arctel.oms.pub.domain.OmsJob;
import com.arctel.oms.pub.domain.dto.JobProgressDto;
import com.arctel.oms.pub.domain.input.CreateJobInput;
import com.arctel.oms.pub.domain.input.UpdateJobInput;
import com.arctel.oms.pub.domain.input.UpdateJobProgressInput;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ThreadPoolJobServiceImplTest {

    @Resource
    ThreadPoolJobService threadPoolJobSupport;

    OmsJob omsJob;

    String jobId = "2001618046585470977";

    @Test
    void createJob() {
        OmsJob job = threadPoolJobSupport.createJob(
                new CreateJobInput("test_message", "test_task")
        );
        this.omsJob = job;
        this.jobId = job.getJobId();
    }


    @Test
    void createJobAsync() {


        OmsJob job = threadPoolJobSupport.createJob(new CreateJobInput("test_message", "test_task"),
                new JobRunnable(threadPoolJobSupport) {
                    @Override
                    public void taskRun() {
                        System.out.println("Async job running for jobId: " + omsJob.getJobId());
                        // Simulate some work with sleep
                        try {
                            for (int i = 0; i < 100; i++) {
                                Thread.sleep(500);
                                System.out.println("JobId: " + omsJob.getJobId() + " - Progress: " + (i + 1) + "%");
                                this.updateProgress(i + 1, 100, "Progress updated to " + (i + 1) + "%");
                            }
                        } catch (InterruptedException e) {
                            System.out.println("Main thread interrupted: " + e.getMessage());
                        }
                        System.out.println("Async job completed for jobId: " + omsJob.getJobId());
                    }
                });
        this.omsJob = job;
        this.jobId = job.getJobId();

        //等待异步任务完成
        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted: " + e.getMessage());
        }
    }


    @Test
    void updateJob() {
        UpdateJobInput updateJobInput = new UpdateJobInput();
        updateJobInput.setJobId(jobId);
        updateJobInput.setStatus(JobStatusEnum.RUNNING.getValue());
        updateJobInput.setMessage("start running");
        threadPoolJobSupport.updateJob(updateJobInput);
    }

    @Test
    void updateTaskProgress() {
        JobProgressDto jobProgressDto = new JobProgressDto(5, 100);
        threadPoolJobSupport.updateJobProgress(new UpdateJobProgressInput(jobId, "", jobProgressDto));
    }

    @Test
    void completeTask() {
    }

    @Test
    void cancelTask() {
    }
}
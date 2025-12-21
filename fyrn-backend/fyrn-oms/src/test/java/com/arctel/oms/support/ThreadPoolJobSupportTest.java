package com.arctel.oms.support;

import com.arctel.oms.common.constants.JobStatusEnum;
import com.arctel.oms.common.utils.Result;
import com.arctel.oms.domain.OmsJob;
import com.arctel.oms.domain.dto.JobProgressDto;
import com.arctel.oms.domain.dto.JobRunnable;
import com.arctel.oms.domain.input.CreateJobInput;
import com.arctel.oms.domain.input.UpdateJobInput;
import com.arctel.oms.domain.input.UpdateJobProgressInput;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ThreadPoolJobSupportTest {

    @Resource
    ThreadPoolJobSupport threadPoolJobSupport;

    OmsJob omsJob;

    String jobId = "2001618046585470977";

    @Test
    void createJob() {
        CreateJobInput createJobInput = new CreateJobInput();
        createJobInput.setMessage("test message");
        createJobInput.setTask_type("test_task");
        Result<OmsJob> job = threadPoolJobSupport.createJob(createJobInput);
        this.omsJob = job.getData();
        this.jobId = job.getData().getJobId();
    }


    @Test
    void createJobAsync() {
        CreateJobInput createJobInput = new CreateJobInput();
        createJobInput.setMessage("test message");
        createJobInput.setTask_type("test_task");

        Result<OmsJob> job = threadPoolJobSupport.createJob(createJobInput, new JobRunnable(threadPoolJobSupport) {
            @Override
            public void taskRun() {
                System.out.println("Async job running for jobId: " + omsJob.getJobId());
                // Simulate some work with sleep
                try {
                    for (int i = 0; i < 100; i++) {
                        Thread.sleep(500);
                        System.out.println("JobId: " + omsJob.getJobId() + " - Progress: " + (i + 1) + "%");
                        JobProgressDto jobProgressDto = new JobProgressDto();
                        jobProgressDto.setCurrent(i+1);
                        jobProgressDto.setTotal(100);
                        threadPoolJobSupport.updateJobProgress(new UpdateJobProgressInput(
                                omsJob.getJobId(),
                                "Progress updated to " + (i + 1) + "%",
                                jobProgressDto
                        ));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Async job completed for jobId: " + omsJob.getJobId());
            }
        });
        this.omsJob = job.getData();
        this.jobId = job.getData().getJobId();

        //等待异步任务完成
        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {

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
        JobProgressDto jobProgressDto = new JobProgressDto();
        jobProgressDto.setTotal(100);
        jobProgressDto.setCurrent(5);
        threadPoolJobSupport.updateJobProgress(new UpdateJobProgressInput(jobId,"",jobProgressDto));
    }

    @Test
    void completeTask() {
    }

    @Test
    void cancelTask() {
    }
}
package com.arctel.oms.support;

import com.arctel.oms.common.constants.JobStatusEnum;
import com.arctel.oms.common.utils.Result;
import com.arctel.oms.domain.OmsJob;
import com.arctel.oms.domain.dto.JobProgressDto;
import com.arctel.oms.domain.input.CreateJobInput;
import com.arctel.oms.domain.input.UpdateJobInput;
import com.arctel.oms.domain.input.UpdateJobProgressInput;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

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
        UpdateJobProgressInput updateJobProgressInput = new UpdateJobProgressInput();
        updateJobProgressInput.setJobId(jobId);
        JobProgressDto jobProgressDto = new JobProgressDto();
        jobProgressDto.setTotal(100);
        jobProgressDto.setCurrent(5);
        updateJobProgressInput.setJobProgressDto(jobProgressDto);
        threadPoolJobSupport.updateJobProgress(updateJobProgressInput);
    }

    @Test
    void completeTask() {
    }

    @Test
    void cancelTask() {
    }
}
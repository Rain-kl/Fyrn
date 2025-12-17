package com.arctel.oms.biz.job.controller;

import com.arctel.oms.biz.job.domain.OmsJob;
import com.arctel.oms.biz.job.domain.dto.listJobsInput;
import com.arctel.oms.biz.job.service.OmsJobService;
import com.arctel.oms.common.base.BaseQueryPage;
import com.arctel.oms.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务调度控制器
 */
@RequestMapping("/oms/job")
@RestController
public class JobController {
    @Resource
    OmsJobService omsJobService;

    @GetMapping("/list")
    public Result<BaseQueryPage<OmsJob>> listJobs(listJobsInput input) {
        OmsJob omsJob = new OmsJob();
        omsJob.setJobId(input.getJobId());
        omsJob.setTaskType(input.getTaskType());
        omsJob.setStatus(input.getStatus());
        return Result.success(omsJobService.pageJob(omsJob, input.getPageNo(), input.getPageSize()));
    }
}

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

package com.arctel.oms.controller;

import com.arctel.oms.common.base.BaseQueryPage;
import com.arctel.oms.common.utils.Result;
import com.arctel.oms.domain.OmsJob;
import com.arctel.oms.domain.input.*;
import com.arctel.oms.service.OmsJobService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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


    /**
     * 创建新任务
     *
     * @param input
     * @return
     */
    @PostMapping("/create")
    public Result<OmsJob> createJob(CreateJobInput input) {
        return Result.success(omsJobService.createJob(input));
    }


    /**
     * 获取任务列表
     *
     * @param input
     * @return
     */
    @GetMapping("/list")
    public Result<BaseQueryPage<OmsJob>> listJobs(ListJobsInput input) {
        OmsJob omsJob = new OmsJob();
        omsJob.setJobId(input.getJobId());
        omsJob.setTaskType(input.getTaskType());
        omsJob.setStatus(input.getStatus());
        return Result.success(omsJobService.pageJob(omsJob, input.getPageNo(), input.getPageSize()));
    }

    /**
     * 获取任务详情
     *
     * @param input
     * @return
     */
    @GetMapping("/detail")
    public Result<OmsJob> getJobDetail(GetJobDetailInput input) {
        return Result.success(omsJobService.getJobDetail(input));
    }

    /**
     * 更新任务进度
     *
     * @param input
     * @return
     */
    @PostMapping("/updateProgress")
    public Result<Boolean> updateJobProgress(UpdateJobProgressInput input) {
        return Result.success(omsJobService.updateJobProgress(input));
    }

    /**
     * 更新任务信息
     *
     * @param input
     * @return
     */
    @PostMapping("/updateJob")
    public Result<Boolean> updateJobInput(UpdateJobInput input) {
        return Result.success(omsJobService.updateJob(input));
    }

    /**
     * 获取任务日志
     *
     * @param logId
     * @return
     */
    @PostMapping("/log")
    public Result<String> getJobLog(String logId) {
        return Result.success(omsJobService.getLog(logId));
    }

}

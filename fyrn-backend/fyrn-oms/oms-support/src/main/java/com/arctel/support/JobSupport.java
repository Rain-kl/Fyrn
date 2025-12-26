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

package com.arctel.support;

import com.arctel.biz.job.OmsJobService;

import com.arctel.pub.base.BaseQueryPage;
import com.arctel.pub.domain.OmsJob;
import com.arctel.pub.domain.input.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class JobSupport {


    @Resource
    OmsJobService omsJobService;


    /**
     * 创建新任务
     *
     * @param input
     * @return
     */
    public OmsJob createJob(CreateJobInput input) {
        return omsJobService.createJob(input);
    }


    /**
     * 获取任务列表
     *
     * @param input
     * @return
     */
    public BaseQueryPage<OmsJob> listJobs(ListJobsInput input) {
        OmsJob omsJob = new OmsJob();
        omsJob.setJobId(input.getJobId());
        omsJob.setTaskType(input.getTaskType());
        omsJob.setStatus(input.getStatus());
        return omsJobService.pageJob(omsJob, input.getPageNo(), input.getPageSize());
    }

    /**
     * 获取任务详情
     *
     * @param input
     * @return
     */
    public OmsJob getJobDetail(GetJobDetailInput input) {
        return omsJobService.getJobDetail(input);
    }

    /**
     * 更新任务进度
     *
     * @param input
     * @return
     */
    public Boolean updateJobProgress(UpdateJobProgressInput input) {
        return omsJobService.updateJobProgress(input);
    }

    /**
     * 更新任务信息
     *
     * @param input
     * @return
     */
    public Boolean updateJob(UpdateJobInput input) {
        return omsJobService.updateJob(input);
    }
}

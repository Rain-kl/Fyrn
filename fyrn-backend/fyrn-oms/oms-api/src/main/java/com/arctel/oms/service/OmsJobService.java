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

package com.arctel.oms.service;

import com.arctel.oms.common.base.BaseQueryPage;

import com.arctel.oms.entity.OmsJob;
import com.arctel.oms.input.CreateJobInput;
import com.arctel.oms.input.GetJobDetailInput;
import com.arctel.oms.input.UpdateJobInput;
import com.arctel.oms.input.UpdateJobProgressInput;
import com.arctel.oms.output.JobDetailOutput;
import com.arctel.oms.output.JobMonitorOutput;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author hspcadmin
 * @description 针对表【oms_job(任务表)】的数据库操作Service
 * @createDate 2025-12-16 11:27:56
 */
public interface OmsJobService extends IService<OmsJob> {

    /**
     * 分页查询任务
     */
    BaseQueryPage<OmsJob> pageJob(OmsJob omsJob, Integer pageNo, Integer pageSize);

    /**
     * 获取任务详情
     */
    JobDetailOutput getJobDetail(GetJobDetailInput input);

    /**
     * 创建任务
     * @param input
     * @return
     */
    OmsJob createJob(CreateJobInput input);

    /**
     * 更新任务进度
     * @param input
     * @return
     */
    boolean updateJobProgress(UpdateJobProgressInput input);

    /**
     * 更新任务状态
     * @param input
     * @return
     */
    boolean updateJob(UpdateJobInput input);

    /**
     * 更新任务日志到 Redis
     * @param jobId
     * @param logMessage
     */
    void updateLog(String jobId, String logMessage);

    /**
     * 获取任务日志
     * @param jobId
     * @return
     */
    String getLog(String jobId);

    /**
     * 获取任务日志
     * @param jobId
     * @param limit
     * @return
     */
    String getLog(String jobId, int limit);

    /**
     * 监控任务执行情况
     * @return
     */
    JobMonitorOutput monitorJob();
}

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

package com.arctel.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.arctel.oms.common.base.BaseQueryPage;
import com.arctel.oms.common.constants.ErrorConstant;
import com.arctel.oms.common.constants.JobStatus;
import com.arctel.oms.common.exception.BizException;
import com.arctel.oms.domain.OmsJob;
import com.arctel.oms.domain.dto.JobProgressDto;
import com.arctel.oms.domain.input.GetJobDetailInput;
import com.arctel.oms.domain.input.UpdateJobInput;
import com.arctel.oms.domain.input.UpdateJobProgressInput;
import com.arctel.oms.domain.output.GetJobDetailOutput;
import com.arctel.oms.mapper.OmsJobMapper;
import com.arctel.oms.service.OmsJobService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author hspcadmin
 * @description 针对表【oms_job(任务表)】的数据库操作Service实现
 * @createDate 2025-12-16 11:27:55
 */
@Service
public class OmsJobServiceImpl extends ServiceImpl<OmsJobMapper, OmsJob>
        implements OmsJobService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String JOB_PROGRESS_KEY_PREFIX = "oms:job:progress:";

    private OmsJob getJobById(String jobId) {
        OmsJob omsJob = getBaseMapper().selectOne(
                new LambdaQueryWrapper<OmsJob>()
                        .eq(OmsJob::getJobId, jobId)
        );
        if (ObjectUtil.isNull(omsJob)) {
            throw new BizException(ErrorConstant.COMMON_ERROR, "任务不存在");
        }
        return omsJob;
    }

    @Override
    public BaseQueryPage<OmsJob> pageJob(OmsJob omsJob, Integer pageNo, Integer pageSize) {
        //        设置分页
        IPage<OmsJob> page = new Page<>(pageNo, pageSize);

        IPage<OmsJob> result = page(
                page,
                new LambdaQueryWrapper<OmsJob>()
                        .like(omsJob.getJobId() != null,
                                OmsJob::getJobId, omsJob.getJobId())
                        .like(omsJob.getTaskType() != null,
                                OmsJob::getTaskType, omsJob.getTaskType())
                        .like(omsJob.getStatus() != null,
                                OmsJob::getStatus, omsJob.getStatus())
                        .orderByDesc(OmsJob::getId)
        );
//
        List<OmsJob> ordersList = result.getRecords();


        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }

    @Override
    public GetJobDetailOutput getJobDetail(GetJobDetailInput input) {
        OmsJob omsJob = getJobById(input.getJobId());
        JobProgressDto jobProgressDto = (JobProgressDto) redisTemplate.opsForValue().get(JOB_PROGRESS_KEY_PREFIX + input.getJobId());
        GetJobDetailOutput output = new GetJobDetailOutput();
        BeanUtils.copyProperties(omsJob, output);
        output.setJobProgressDto(jobProgressDto);
        return output;
    }

    @Override
    public boolean updateJobProgress(UpdateJobProgressInput input) {
        String key = JOB_PROGRESS_KEY_PREFIX + input.getJobId();
        redisTemplate.opsForValue().set(key, input.getJobProgressDto());
        return true;
    }

    @Override
    public boolean updateJob(UpdateJobInput input) {
        OmsJob omsJob = getJobById(input.getJobId());
        omsJob.setStatus(input.getStatus());
        omsJob.setMessage(input.getMessage());
        // 任务完成
        if (input.getStatus().equals(JobStatus.SUCCESS.getValue())) {
            omsJob.setFinishedTime(new Date());
        }
        return updateById(omsJob);
    }
}





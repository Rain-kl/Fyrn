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

package com.arctel.oms.biz;

import cn.hutool.core.util.ObjectUtil;
import com.arctel.oms.biz.job.OmsJobService;
import com.arctel.oms.biz.mapper.OmsJobMapper;
import com.arctel.oms.biz.scheduled.ThreadPoolMetricsPublisher;
import com.arctel.oms.pub.base.BaseQueryPage;
import com.arctel.oms.pub.constants.ErrorConstant;
import com.arctel.oms.pub.constants.JobStatusEnum;
import com.arctel.oms.pub.domain.OmsJob;
import com.arctel.oms.pub.domain.dto.JobOverviewDto;
import com.arctel.oms.pub.domain.dto.JobProgressDto;
import com.arctel.oms.pub.domain.dto.ThreadPoolMetricsDTO;
import com.arctel.oms.pub.domain.input.CreateJobInput;
import com.arctel.oms.pub.domain.input.GetJobDetailInput;
import com.arctel.oms.pub.domain.input.UpdateJobInput;
import com.arctel.oms.pub.domain.input.UpdateJobProgressInput;
import com.arctel.oms.pub.domain.output.GetJobDetailOutput;
import com.arctel.oms.pub.domain.output.JobMonitorOutput;
import com.arctel.oms.pub.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import static com.arctel.oms.pub.constants.RedisPrefixConstant.JOB_LOG_KEY_PREFIX;
import static com.arctel.oms.pub.constants.RedisPrefixConstant.JOB_PROGRESS_KEY_PREFIX;

/**
 * @author hspcadmin
 * @description 针对表【oms_job(任务表)】的数据库操作Service实现
 * @createDate 2025-12-16 11:27:55
 */
@Service("omsJobService")
public class OmsJobServiceImpl extends ServiceImpl<OmsJobMapper, OmsJob>
        implements OmsJobService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ThreadPoolMetricsPublisher threadPoolMetricsPublisher;

    public static final Integer MESSAGE_MAX_LENGTH = 4096;

    private OmsJob getJobById(String jobId) {
        OmsJob omsJob = getBaseMapper().selectOne(
                new LambdaQueryWrapper<OmsJob>()
                        .eq(OmsJob::getJobId, jobId));
        if (ObjectUtil.isNull(omsJob)) {
            throw new BizException(ErrorConstant.COMMON_ERROR, "任务不存在");
        }
        return omsJob;
    }

    @Override
    public BaseQueryPage<OmsJob> pageJob(OmsJob omsJob, Integer pageNo, Integer pageSize) {
        // 设置分页
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
                        .orderByDesc(OmsJob::getJobId));
        //
        List<OmsJob> ordersList = result.getRecords();

        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }

    @Override
    public GetJobDetailOutput getJobDetail(GetJobDetailInput input) {
        OmsJob omsJob = getJobById(input.getJobId());
        JobProgressDto jobProgressDto = (JobProgressDto) redisTemplate.opsForValue()
                .get(JOB_PROGRESS_KEY_PREFIX + input.getJobId());
        String jobLog = getLog(input.getJobId());
        GetJobDetailOutput output = new GetJobDetailOutput();
        BeanUtils.copyProperties(omsJob, output);
        output.setJobProgressDto(jobProgressDto);
        output.setJobLog(jobLog);
        return output;
    }

    @Override
    public OmsJob createJob(CreateJobInput input) {
        String taskType = input.getTask_type();
        String message = input.getMessage();
        OmsJob omsJob = new OmsJob();
        omsJob.setStatus(JobStatusEnum.PENDING.getValue());
        omsJob.setTaskType(taskType);
        omsJob.setMessage(message);
        save(omsJob);
        return omsJob;
    }

    @Override
    public synchronized boolean updateJobProgress(UpdateJobProgressInput input) {
        String bizLog = input.getLog();
        if (StringUtils.isNotBlank(bizLog)) {
            updateLog(input.getJobId(), bizLog);
        }
        String key = JOB_PROGRESS_KEY_PREFIX + input.getJobId();
        redisTemplate.opsForValue().set(key, input.getJobProgressDto(), Duration.ofMinutes(10));
        return true;
    }

    // TODO: 后续考虑使用分布式锁
    @Override
    public synchronized boolean updateJob(UpdateJobInput input) {
        OmsJob omsJob = getJobById(input.getJobId());
        Integer newStatus = input.getStatus();
        if (newStatus.equals(JobStatusEnum.SUCCESS.getValue())) {
            if (omsJob.getStatus().equals(JobStatusEnum.RUNNING.getValue())) {
                omsJob.setFinishedTime(new Date());
            } else {
                throw new BizException(ErrorConstant.COMMON_ERROR, "只有运行中的任务才能设置为成功");
            }
        }
        if (newStatus.equals(JobStatusEnum.RUNNING.getValue()) &&
                omsJob.getStatus().equals(JobStatusEnum.PENDING.getValue())) {
            omsJob.setStartedTime(new Date());
        }
        omsJob.setStatus(newStatus);
        String newMessage = input.getMessage();
        String currentMessage = omsJob.getMessage();
        if (currentMessage == null) {
            currentMessage = "";
        }
        String newUpdateMessage = currentMessage + "\n" + "[" + new Date() + "] " + newMessage;
        if (newUpdateMessage.length() > MESSAGE_MAX_LENGTH) {
            newUpdateMessage = newUpdateMessage.substring(newUpdateMessage.length() - MESSAGE_MAX_LENGTH);
        }
        omsJob.setMessage(newUpdateMessage);
        return updateById(omsJob);
    }


    @Override
    public void updateLog(String jobId, String bizLog) {
        String key = JOB_LOG_KEY_PREFIX + jobId;
        // 追加到头部（最新在前）
        String formatLog = "[" + new Date() + "] " + bizLog;
        redisTemplate.opsForList().leftPush(key, formatLog);
        // 只保留最近1万条
        redisTemplate.opsForList().trim(key, 0, 9999);
        // 保留7天
        redisTemplate.expire(key, java.time.Duration.ofDays(7));
    }

    @Override
    public String getLog(String jobId) {
        String key = JOB_LOG_KEY_PREFIX + jobId;
        List<Object> bizLog = redisTemplate.opsForList().range(key, 0, -1);
        if (bizLog == null || bizLog.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = bizLog.size() - 1; i >= 0; i--) {
            sb.append(bizLog.get(i)).append("\n");
        }
        return sb.toString();
    }

    @Override
    public JobMonitorOutput monitorJob() {
        JobMonitorOutput jobMonitorOutput = new JobMonitorOutput();
        ThreadPoolMetricsDTO currentMetrics = threadPoolMetricsPublisher.getCurrentMetrics();

        jobMonitorOutput.setThreadPoolMetricsDTO(currentMetrics);

        JobOverviewDto jobOverviewDto = new JobOverviewDto();

        LambdaQueryWrapper<OmsJob> qw = new LambdaQueryWrapper<>();
        qw.select(OmsJob::getJobId, OmsJob::getStatus, OmsJob::getStartedTime, OmsJob::getFinishedTime);

        List<OmsJob> list = baseMapper.selectList(qw);

        jobOverviewDto.setTotalJobCount(list.size());
        int runningCount = 0;
        int successCount = 0;
        int failedCount = 0;
        for (OmsJob job : list) {
            if (JobStatusEnum.RUNNING.getValue().equals(job.getStatus())) {
                runningCount++;
            } else if (JobStatusEnum.SUCCESS.getValue().equals(job.getStatus())) {
                successCount++;
            } else if (JobStatusEnum.FAILED.getValue().equals(job.getStatus())) {
                failedCount++;
            }
        }
        jobOverviewDto.setRunningJobCount(runningCount);
        jobOverviewDto.setSuccessJobCount(successCount);
        jobOverviewDto.setFailedJobCount(failedCount);

        jobMonitorOutput.setJobOverviewDto(jobOverviewDto);

        return jobMonitorOutput;
    }

}

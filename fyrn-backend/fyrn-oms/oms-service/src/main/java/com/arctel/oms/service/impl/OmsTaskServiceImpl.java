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
import net.arctel.framework.constants.LogConstant;
import net.arctel.framework.exception.BizException;
import com.arctel.oms.dto.TaskOverviewDTO;
import com.arctel.oms.dto.TaskProgressDTO;
import com.arctel.oms.entity.OmsTask;
import com.arctel.oms.common.enums.TaskStatusEnum;
import com.arctel.oms.mapper.OmsTaskMapper;
import com.arctel.oms.dto.ThreadPoolMetricsDTO;
import com.arctel.oms.input.TaskDetailGetInput;
import com.arctel.oms.input.TaskProgressUpdateInput;
import com.arctel.oms.input.TaskUpdateInput;
import com.arctel.oms.output.TaskDetailOutput;
import com.arctel.oms.output.TaskMonitorOutput;
import com.arctel.oms.scheduled.ThreadPoolMetricsPublisher;
import com.arctel.oms.service.OmsTaskService;
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

import static com.arctel.oms.common.constants.RedisPrefixConstant.TASK_LOG_KEY_PREFIX;
import static com.arctel.oms.common.constants.RedisPrefixConstant.TASK_PROGRESS_KEY_PREFIX;

/**
 * @author hspcadmin
 * @description 针对表【oms_task(任务表)】的数据库操作Service实现
 * @createDate 2025-12-16 11:27:55
 */
@Service("omsTaskService")
public class OmsTaskServiceImpl extends ServiceImpl<OmsTaskMapper, OmsTask>
        implements OmsTaskService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ThreadPoolMetricsPublisher threadPoolMetricsPublisher;

    public static final Integer MESSAGE_MAX_LENGTH = 4096;

    @Override
    public OmsTask getTaskById(String taskId) {
        OmsTask omsTask = getBaseMapper().selectOne(
                new LambdaQueryWrapper<OmsTask>()
                        .eq(OmsTask::getTaskId, taskId));
        if (ObjectUtil.isNull(omsTask)) {
            throw new BizException(ErrorConstant.COMMON_ERROR, "任务不存在");
        }
        return omsTask;
    }

    @Override
    public BaseQueryPage<OmsTask> pageTask(OmsTask omsTask, Integer pageNo, Integer pageSize) {
        // 设置分页
        IPage<OmsTask> page = new Page<>(pageNo, pageSize);

        IPage<OmsTask> result = page(
                page,
                new LambdaQueryWrapper<OmsTask>()
                        .like(omsTask.getTaskId() != null,
                                OmsTask::getTaskId, omsTask.getTaskId())
                        .like(omsTask.getBizTag() != null,
                                OmsTask::getBizTag, omsTask.getBizTag())
                        .like(omsTask.getStatus() != null,
                                OmsTask::getStatus, omsTask.getStatus())
                        .orderByDesc(OmsTask::getTaskId));
        //
        List<OmsTask> ordersList = result.getRecords();

        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }

    @Override
    public TaskDetailOutput getTaskDetail(TaskDetailGetInput input) {
        OmsTask omsTask = getTaskById(input.getTaskId());
        TaskProgressDTO taskProgressDTO = (TaskProgressDTO) redisTemplate.opsForValue()
                .get(TASK_PROGRESS_KEY_PREFIX + input.getTaskId());
        String taskLog = getLog(input.getTaskId());
        TaskDetailOutput output = new TaskDetailOutput();
        BeanUtils.copyProperties(omsTask, output);
        output.setTaskProgressDTO(taskProgressDTO);
        output.setTaskLog(taskLog);
        return output;
    }

    @Override
    public OmsTask createTask(OmsTask omsTask) {
        String formattedMsg = "[" + new Date() + "] " + omsTask.getMessage();
        omsTask.setMessage(formattedMsg);
        save(omsTask);
        return omsTask;
    }

    @Override
    public synchronized boolean updateTaskProgress(TaskProgressUpdateInput input) {
        String bizLog = input.getLog();
        // 更新日志
        if (StringUtils.isNotBlank(bizLog)) {
            writeLog(LogConstant.INFO, input.getTaskId(), bizLog);
        }
        // 更新进度到 Redis，设置10分钟过期
        String key = TASK_PROGRESS_KEY_PREFIX + input.getTaskId();
        redisTemplate.opsForValue().set(key, input.getTaskProgressDTO(), Duration.ofMinutes(10));
        return true;
    }

    // TODO: 后续考虑使用分布式锁
    @Override
    public synchronized boolean updateTask(TaskUpdateInput input) {
        OmsTask omsTask = getTaskById(input.getTaskId());
        Integer newStatus = input.getStatus();
        // 状态变更校验
        if (newStatus.equals(TaskStatusEnum.SUCCESS.getValue())) {
            if (omsTask.getStatus().equals(TaskStatusEnum.RUNNING.getValue())) {
                omsTask.setFinishedTime(new Date());
            } else {
                throw new BizException(ErrorConstant.COMMON_ERROR, "只有运行中的任务才能设置为成功");
            }
        }
        if (newStatus.equals(TaskStatusEnum.RUNNING.getValue()) &&
                omsTask.getStatus().equals(TaskStatusEnum.PENDING.getValue())) {
            omsTask.setStartedTime(new Date());
        }
        omsTask.setStatus(newStatus);
        String newMessage = input.getMessage();
        String currentMessage = omsTask.getMessage();
        if (currentMessage == null) {
            currentMessage = "";
        }
        String newUpdateMessage = currentMessage + "\n" + "[" + new Date() + "] " + newMessage;
        if (newUpdateMessage.length() > MESSAGE_MAX_LENGTH) {
            newUpdateMessage = newUpdateMessage.substring(newUpdateMessage.length() - MESSAGE_MAX_LENGTH);
        }
        omsTask.setMessage(newUpdateMessage);
        return updateById(omsTask);
    }


    @Override
    public void writeLog(String taskId, String bizLog) {
        this.writeLog(LogConstant.INFO, taskId, bizLog);
    }

    @Override
    public void writeLog(String logLevel, String taskId, String bizLog) {
        String key = TASK_LOG_KEY_PREFIX + taskId;
        // 追加到头部（最新在前）
        String formatLog = "[" + logLevel + "] [" + new Date() + "] " + bizLog;
        redisTemplate.opsForList().leftPush(key, formatLog);
        // 只保留最近1万条
        redisTemplate.opsForList().trim(key, 0, 9999);
        // 保留7天
        redisTemplate.expire(key, Duration.ofDays(7));
    }


    @Override
    public String getLog(String taskId) {
        return getLog(taskId, 200);
    }

    @Override
    public String getLog(String taskId, int limit) {
        String key = TASK_LOG_KEY_PREFIX + taskId;
        List<Object> bizLog = redisTemplate.opsForList().range(key, 0, limit);
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
    public TaskMonitorOutput monitorTask() {
        TaskMonitorOutput taskMonitorOutput = new TaskMonitorOutput();
        ThreadPoolMetricsDTO currentMetrics = threadPoolMetricsPublisher.getCurrentMetrics();

        taskMonitorOutput.setThreadPoolMetricsDTO(currentMetrics);

        TaskOverviewDTO taskOverviewDto = new TaskOverviewDTO();

        LambdaQueryWrapper<OmsTask> qw = new LambdaQueryWrapper<>();
        qw.select(OmsTask::getTaskId, OmsTask::getStatus, OmsTask::getStartedTime, OmsTask::getFinishedTime);

        List<OmsTask> list = baseMapper.selectList(qw);

        taskOverviewDto.setTotalTaskCount(list.size());
        int runningCount = 0;
        int successCount = 0;
        int failedCount = 0;
        for (OmsTask task : list) {
            if (TaskStatusEnum.RUNNING.getValue().equals(task.getStatus())) {
                runningCount++;
            } else if (TaskStatusEnum.SUCCESS.getValue().equals(task.getStatus())) {
                successCount++;
            } else if (TaskStatusEnum.FAILED.getValue().equals(task.getStatus())) {
                failedCount++;
            }
        }
        taskOverviewDto.setRunningTaskCount(runningCount);
        taskOverviewDto.setSuccessTaskCount(successCount);
        taskOverviewDto.setFailedTaskCount(failedCount);

        taskMonitorOutput.setTaskOverviewDTO(taskOverviewDto);

        return taskMonitorOutput;
    }

}

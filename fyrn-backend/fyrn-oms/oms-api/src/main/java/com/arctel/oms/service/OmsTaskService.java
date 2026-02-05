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
import com.arctel.oms.domain.entity.OmsTask;
import com.arctel.oms.input.*;
import com.arctel.oms.output.TaskDetailOutput;
import com.arctel.oms.output.TaskMonitorOutput;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author hspcadmin
 * @description 针对表【oms_task(任务表)】的数据库操作Service
 * @createDate 2025-12-16 11:27:56
 */
public interface OmsTaskService extends IService<OmsTask> {


    OmsTask getTaskById(String taskId);

    /**
     * 分页查询任务
     */
    BaseQueryPage<OmsTask> pageTask(OmsTask omsTask, Integer pageNo, Integer pageSize);

    /**
     * 获取任务详情
     */
    TaskDetailOutput getTaskDetail(TaskDetailGetInput input);

    /**
     * 创建任务
     *
     * @param omsTask
     * @return
     */
    OmsTask createTask(OmsTask omsTask);

    /**
     * 更新任务进度
     *
     * @param input
     * @return
     */
    boolean updateTaskProgress(TaskProgressUpdateInput input);

    /**
     * 更新任务状态
     *
     * @param input
     * @return
     */
    boolean updateTask(TaskUpdateInput input);

    /**
     * INFO级别
     *
     * @param taskId
     * @param
     */
    void writeLog(String taskId, String bizLog);

    /**
     * 更新任务日志到 Redis
     *
     * @param taskId
     * @param
     */
    void writeLog(String logLevel, String taskId, String bizLog);


    /**
     * 获取任务日志
     *
     * @param taskId
     * @return
     */
    String getLog(String taskId);

    /**
     * 获取任务日志
     *
     * @param taskId
     * @param limit
     * @return
     */
    String getLog(String taskId, int limit);

    /**
     * 监控任务执行情况
     *
     * @return
     */
    TaskMonitorOutput monitorTask();
}

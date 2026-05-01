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

package net.arctel.oms.controller;


import net.arctel.platform.framework.utils.Result;
import net.arctel.oms.input.TaskDetailGetInput;
import net.arctel.oms.input.TaskPageQueryInput;
import net.arctel.oms.input.TaskProgressUpdateInput;
import net.arctel.oms.input.TaskUpdateInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.arctel.oms.common.base.BaseQueryPage;
import net.arctel.oms.entity.OmsTask;
import net.arctel.oms.output.TaskDetailOutput;
import net.arctel.oms.output.TaskMonitorOutput;
import net.arctel.oms.service.OmsTaskService;

import jakarta.annotation.Resource;

@RequestMapping("/oms/task")
@RestController
public class OmsTaskController {

    @Resource
    OmsTaskService omsTaskService;


    /**
     * 获取任务列表
     *
     * @param input
     * @return
     */
    @GetMapping("/list")
    public Result<BaseQueryPage<OmsTask>> listTasks(TaskPageQueryInput input) {
        OmsTask omsTask = new OmsTask();
        omsTask.setTaskId(input.getTaskId());
        omsTask.setTaskTag(input.getTaskTag());
        omsTask.setStatus(input.getStatus());
        return Result.success(omsTaskService.pageTask(omsTask, input.getPageNo(), input.getPageSize()));
    }

    /**
     * 获取任务详情
     *
     * @param input
     * @return
     */
    @GetMapping("/detail")
    public Result<TaskDetailOutput> getTaskDetail(TaskDetailGetInput input) {
        return Result.success(omsTaskService.getTaskDetail(input));
    }

    /**
     * 更新任务进度
     *
     * @param input
     * @return
     */
    @PostMapping("/updateProgress")
    public Result<Boolean> updateTaskProgress(TaskProgressUpdateInput input) {
        return Result.success(omsTaskService.updateTaskProgress(input));
    }

    /**
     * 更新任务信息
     *
     * @param input
     * @return
     */
    @PostMapping("/updateTask")
    public Result<Boolean> updateTaskInput(TaskUpdateInput input) {
        return Result.success(omsTaskService.updateTask(input));
    }

    /**
     * 获取任务日志
     *
     * @param logId
     * @return
     */
    @PostMapping("/log")
    public Result<String> getTaskLog(String logId) {
        return Result.success(omsTaskService.getLog(logId));
    }


    /**
     * 任务监控
     */
    @GetMapping("/monitor")
    public Result<TaskMonitorOutput> monitorTask() {
        return Result.success(omsTaskService.monitorTask());
    }
}

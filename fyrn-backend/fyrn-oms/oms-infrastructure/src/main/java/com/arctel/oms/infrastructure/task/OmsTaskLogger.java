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

package com.arctel.oms.infrastructure.task;

import com.arctel.oms.domain.dto.TaskProgressDTO;
import com.arctel.oms.domain.entity.OmsTask;
import com.arctel.oms.infrastructure.task.base.BaseTaskMessage;
import com.arctel.oms.input.TaskProgressUpdateInput;
import com.arctel.oms.service.OmsTaskService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class OmsTaskLogger<T extends BaseTaskMessage> {


    public abstract OmsTask getOmsTask();

    public abstract OmsTaskService getOmsTaskService();

    private final AtomicLong currentProgress = new AtomicLong(0);

    @Setter
    private Long totalProgress;

    public void writeLog(String logMessage) {
        getOmsTaskService().writeLog(getOmsTask().getTaskId(), logMessage);
    }

    public void updateProgress(Long current, Long total, String logMessage) {
        this.totalProgress = total;
        currentProgress.set(current);
        getOmsTaskService().updateTaskProgress(new TaskProgressUpdateInput(
                getOmsTask().getTaskId(), logMessage, new TaskProgressDTO(currentProgress.incrementAndGet(), total)
        ));
    }

    public void updateProgress(Long total, String logMessage) {
        this.totalProgress = total;
        getOmsTaskService().updateTaskProgress(new TaskProgressUpdateInput(
                getOmsTask().getTaskId(), logMessage, new TaskProgressDTO(currentProgress.incrementAndGet(), total)
        ));
    }

    public void updateProgress(String logMessage) {
        // 如果 totalProgress 不为 null 且大于 0，则使用它来更新进度
        if (this.totalProgress != null && this.totalProgress > 0) {
            updateProgress(this.totalProgress, logMessage);
        } else {
            getOmsTaskService().updateTaskProgress(new TaskProgressUpdateInput(
                    getOmsTask().getTaskId(), logMessage, new TaskProgressDTO(currentProgress.incrementAndGet(), 0L)
            ));
        }
    }

    public void updateProgress(Integer current, Integer total, String logMessage) {
        updateProgress(Long.valueOf(current), Long.valueOf(total), logMessage);
    }

    public void updateProgress(Integer total, String logMessage) {
        updateProgress(Long.valueOf(total), logMessage);
    }


}

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

package net.arctel.oms.infra.task;

import net.arctel.framework.constants.LogConstant;
import net.arctel.oms.dto.TaskProgressDTO;
import net.arctel.oms.entity.OmsTask;
import net.arctel.framework.core.task.BaseTaskMessage;
import net.arctel.framework.core.task.BaseTaskRspCollector;
import net.arctel.framework.core.task.TaskLogger;
import net.arctel.oms.input.TaskProgressUpdateInput;
import net.arctel.oms.service.OmsTaskService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class OmsTaskLogger<T extends BaseTaskMessage> extends BaseTaskRspCollector<T> implements TaskLogger {


    public abstract OmsTask getOmsTask();

    public abstract OmsTaskService getOmsTaskService();

    private final AtomicLong currentProgress = new AtomicLong(0);

    @Setter
    private Long totalProgress;

    public void logInfo(String logMessage) {
        getOmsTaskService().writeLog(LogConstant.INFO, getOmsTask().getTaskId(), logMessage);
    }

    public void logError(String logMessage, Exception e) {
        log.error(logMessage, e);
        getOmsTaskService().writeLog(LogConstant.ERROR, getOmsTask().getTaskId(), logMessage);
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

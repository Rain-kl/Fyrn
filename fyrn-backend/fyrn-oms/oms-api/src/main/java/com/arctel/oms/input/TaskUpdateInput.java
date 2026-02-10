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

package com.arctel.oms.input;

import com.arctel.oms.common.enums.TaskStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateInput {
    String taskId;
    Integer status;
    String message;

    public TaskUpdateInput(String taskId, Integer status) {
        this.taskId = taskId;
        this.status = status;
        if (TaskStatusEnum.RUNNING.getValue().equals(status)) {
            this.message = "任务开始执行";
        }
        if (TaskStatusEnum.FAILED.getValue().equals(status)) {
            this.message = "任务执行失败";
        }
        if (TaskStatusEnum.SUCCESS.getValue().equals(status)) {
            this.message = "任务执行完成";
        }
    }
}

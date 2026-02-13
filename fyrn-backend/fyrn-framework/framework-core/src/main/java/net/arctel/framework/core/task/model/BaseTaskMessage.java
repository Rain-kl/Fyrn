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

package net.arctel.framework.core.task.model;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import lombok.Data;

@Data
public class BaseTaskMessage {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务标签, 用于分配到不同的处理器
     */
    private String taskTag;

    /**
     * 该任务是否允许重试，0表示不允许重试，1表示允许重试
     */
    private int allowRetry;

    /**
     * 业务标签
     */
    private String bizTag;

    /**
     * 业务值
     */
    private String bizValue;

    public BaseTaskMessage build() {
        this.taskId = UUID.randomUUID().toString();
        return this;
    }

    public BaseTaskMessage buildTaskMessage(String taskTag, int allowRetry, String bizTag, String bizValue) {
        if (this.taskId == null) {
            throw new IllegalArgumentException("taskId cannot be null");
        }
        this.taskTag = taskTag;
        this.allowRetry = allowRetry;
        this.bizTag = bizTag;
        this.bizValue = bizValue;
        return this;
    }

    public BaseTaskMessage buildTaskMessage(String taskTag, String bizTag, String bizValue) {
        if (this.taskId == null) {
            throw new IllegalArgumentException("taskId cannot be null");
        }
        this.taskTag = taskTag;
        this.allowRetry = 0;
        this.bizTag = bizTag;
        this.bizValue = bizValue;
        return this;
    }

    public BaseTaskMessage buildTaskMessage(String taskTag, String bizTag, Object bizValue) {
        if (this.taskId == null) {
            throw new IllegalArgumentException("taskId cannot be null");
        }
        this.taskTag = taskTag;
        this.allowRetry = 0;
        this.bizTag = bizTag;
        this.bizValue = JSON.toJSONString(bizValue);
        return this;
    }
}

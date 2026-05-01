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

package net.arctel.workbench.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 工作台任务表
 *
 * @TableName wb_task
 */
@TableName(value = "wb_task")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WbTask {
    /**
     * 任务ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务类型 0-任务 1-提醒
     */
    private Integer type;

    /**
     * 任务分类, 未分类/日常/工作/<自定义>
     */
    private String tag;

    /**
     * 任务状态 0-未开始 1-进行中 2-已完成 3-已取消
     */
    private Integer status;

    /**
     * 任务进度 0-100
     */
    private Integer progress;

    /**
     * 优先级 1-低 2-中 3-高 4-紧急
     */
    private Integer priority;

    /**
     * 父任务ID
     */
    private String parentId;

    /**
     * 任务开始时间
     */
    private Date startTime;

    /**
     * 任务截止时间
     */
    private Date deadline;

    /**
     * 任务完成时间
     */
    private Date completeTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    public static WbTask buildPageInput(WbTask input) {
        return WbTask.builder()
                .name(input.getName())
                .description(input.getDescription())
                .type(input.getType())
                .tag(input.getTag())
                .status(input.getStatus())
                .priority(input.getPriority())
                .parentId(input.getParentId())
                .startTime(input.getStartTime())
                .deadline(input.getDeadline())
                .completeTime(input.getCompleteTime())
                .build();
    }
}

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

package com.arctel.oms.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

/**
 * 任务表
 *
 * @TableName oms_task
 */
@TableName(value = "oms_task")
@Data
public class OmsTask {
    /**
     * 业务任务ID
     */
    @TableId(type = IdType.ASSIGN_ID)
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

    /**
     * 0=queued,1=running,2=success,3=failed,4=canceled
     */
    private Integer status;

    /**
     * 状态说明/失败摘要
     */
    private String message;

    /**
     * 任务对象(JSON格式), 重试时使用
     */
    @JsonIgnore
    private String taskObject;

    /**
     * 触发人
     */
    private String createdUser;

    /**
     * 任务开始时间
     */
    private Date startedTime;

    /**
     * 任务结束时间
     */
    private Date finishedTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;


}
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

package net.arctel.fyrn.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 小说元数据表
 * @TableName mms_meta
 */
@TableName(value ="mms_meta")
@Data
public class MmsMeta {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * Platform Book Number
     */
    private String pbn;

    /**
     * 小说名称
     */
    private String title;

    /**
     * 小说作者
     */
    private String author;

    /**
     * 小说标签
     */
    private String tag;

    /**
     * 小说简介
     */
    private String summary;

    /**
     * 小说人气值
     */
    private Long popularity;

    /**
     * 小说字数, 单位：字
     */
    private Long wordCount;

    /**
     * 1: 连载中, 2：完结
     */
    private Integer status;

    /**
     * 小说来源
     */
    private String source;

    /**
     * 小说来源链接
     */
    private String sourceUrl;

    /**
     * 小说发布时间
     */
    private Date postTime;

    /**
     * 小说最后编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 创建人
     */
    private String createdUser;

    /**
     * 修改人
     */
    private String updatedUser;
}

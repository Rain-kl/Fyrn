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

package com.arctel.pub.base;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 封装分页查询结果
 */
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseQueryPageInput extends BaseInput {

    /**
     * 当前页码
     */
    private Integer pageNo = 1;

    /**
     * 每页记录数
     */
    private Integer pageSize = 20;

    /**
     * 获取当前页码，如果为空则返回默认值1
     */
    public Integer getPageNo() {
        return pageNo == null ? 1 : pageNo;
    }

    /**
     * 获取每页记录数，如果为空则返回默认值20
     */
    public Integer getPageSize() {
        return pageSize == null ? 20 : pageSize;
    }

}

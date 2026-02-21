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

package net.arctel.oms.common.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 封装分页查询结果
 */
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseQueryPageInput {

    public static final String ASC = "asc";
    public static final String DESC = "desc";

    /**
     * 当前页码
     */
    private Integer pageNo = 1;

    /**
     * 每页记录数
     */
    private Integer pageSize = 20;

    /**
     * 排序字段，默认为id
     * -- GETTER --
     * 获取排序字段，如果为空则返回默认值id
     */
    @Getter
    private String orderBy = null;

    /**
     * 排序方向，默认为升序（asc），可选值为asc或desc
     */
    private String orderDirection = ASC;

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

    /**
     * 获取排序方向，如果为空则返回默认值asc
     */
    public String getOrderDirection() {
        return orderDirection == null ? ASC : orderDirection;
    }

}

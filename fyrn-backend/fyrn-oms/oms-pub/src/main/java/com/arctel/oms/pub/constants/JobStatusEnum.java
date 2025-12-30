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

package com.arctel.oms.pub.constants;

import lombok.Getter;

@Getter
public enum JobStatusEnum {
    PENDING("QUEUED",0, "列队中"),
    RUNNING("RUNNING", 1,"运行中"),
    SUCCESS("SUCCESS",2, "成功"),
    FAILED("FAILED",3, "失败"),
    CANCELLED("CANCELLED", 4,"已取消");

    private final String code;
    private final String description;
    private final int value;


    JobStatusEnum(String code, int value, String description) {
        this.code = code;
        this.value = value;
        this.description = description;
    }

    /**
     * 判断当前状态是否为终态（即任务不会再发生变化）
     */
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }

    /**
     * 根据 code 获取对应的 JobStatus 枚举
     *
     * @param code 状态码
     * @return 对应的枚举，若未找到则返回 null
     */
    public static JobStatusEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (JobStatusEnum status : JobStatusEnum.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断给定状态是否有效（即是否属于已定义的枚举值）
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
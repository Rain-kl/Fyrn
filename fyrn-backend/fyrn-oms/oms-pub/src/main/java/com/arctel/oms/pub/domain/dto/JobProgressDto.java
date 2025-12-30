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

package com.arctel.oms.pub.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class JobProgressDto {

    Long current;

    Long total;

    public JobProgressDto(Integer current, Integer total) {
        this.current = Long.valueOf(current);
        this.total = Long.valueOf(total);
    }

    public void setCurrent(Integer current) {
        this.current = Long.valueOf(current);
    }

    public void setTotal(Integer total) {
        this.total = Long.valueOf(total);
    }

    public double getPercent() {
        if (total == null || total == 0) {
            return 0.0;
        }
        if (current == null) {
            return 0.0;
        }
        return (double) current / total;
    }
}

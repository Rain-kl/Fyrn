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

package net.arctel.opkit.output;

import lombok.Data;


@Data
public class OpkitListOutput {

    /**
     * 工具名称
     */
    String toolName;

    /**
     * 工具描述
     */
    String description;

    /**
     * 工具处理器标签
     */
    String handlerTag;

    /**
     * 是否可用
     */
    Boolean available;

    /**
     * 工具版本
     */
    String version;


}

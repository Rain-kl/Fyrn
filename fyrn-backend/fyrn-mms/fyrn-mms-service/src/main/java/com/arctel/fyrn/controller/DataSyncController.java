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

package com.arctel.fyrn.controller;



import java.io.IOException;

import net.arctel.framework.utils.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arctel.fyrn.input.SyncMaterialInput;
import com.arctel.fyrn.service.DataSyncService;
import com.arctel.oms.entity.OmsTask;

import jakarta.annotation.Resource;

@RequestMapping("/sync")
@RestController
@Validated
public class DataSyncController {

    @Resource
    DataSyncService dataSyncService;

    /**
     * 同步小说任务
     */
    @PostMapping("/ossToMms")
    public Result<OmsTask> ossToMms() {
        return Result.success(dataSyncService.ossToMms());
    }

    /**
     * 同步本地文件到OSS
     *
     * @param input 最大同步数量, -1表示不限制
     */
    @PostMapping("/fileToOss")
    public Result<String> fileToOss(SyncMaterialInput input) throws IOException {
        return Result.success(dataSyncService.fileToOss(input));
    }

}

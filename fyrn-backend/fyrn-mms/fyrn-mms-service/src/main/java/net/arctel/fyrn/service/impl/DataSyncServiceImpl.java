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

package net.arctel.fyrn.service.impl;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.arctel.fyrn.constants.MmsHandlerTagConstant;
import net.arctel.fyrn.input.SyncMaterialInput;
import net.arctel.fyrn.service.DataSyncService;
import net.arctel.fyrn.task.MmsSvrQueue;
import net.arctel.platform.oms.entity.OmsTask;
import net.arctel.platform.oms.task.DefaultTaskMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author ryan
 * @description
 * @createDate
 */
@Service
@Slf4j
public class DataSyncServiceImpl implements DataSyncService {


    @Resource
    MmsSvrQueue mmsSvrQueue;


    @Override
    public String fileToOss(SyncMaterialInput input) throws IOException {
        DefaultTaskMessage defaultTaskMessage = new DefaultTaskMessage();
        defaultTaskMessage.setTaskTag(MmsHandlerTagConstant.FileToOssHandler);
        defaultTaskMessage.setBizTag("同步本地文件到OSS");
        defaultTaskMessage.setBizValue(String.valueOf(input.getSize()));
        OmsTask task = mmsSvrQueue.createTask(defaultTaskMessage);
        return task.getTaskId();
    }

    @Override
    public OmsTask ossToMms() {
        DefaultTaskMessage defaultTaskMessage = new DefaultTaskMessage();
        defaultTaskMessage.setTaskTag(MmsHandlerTagConstant.OssToMms);
        defaultTaskMessage.setBizTag("同步OSS文件到MMS");
        return mmsSvrQueue.createTask(defaultTaskMessage);
    }


}

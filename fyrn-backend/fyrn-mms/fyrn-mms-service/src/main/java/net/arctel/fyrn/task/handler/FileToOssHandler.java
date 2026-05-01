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

package net.arctel.fyrn.task.handler;

import net.arctel.common.MockFunction;
import net.arctel.fyrn.constants.MmsHandlerTagConstant;
import net.arctel.fyrn.dto.LocalFileSimpleDTO;
import net.arctel.fyrn.input.UMmsPageInput;
import net.arctel.fyrn.service.MmsNovelFileService;
import net.arctel.platform.oms.common.base.BaseQueryPage;
import net.arctel.platform.oms.task.DefaultTaskMessage;
import net.arctel.platform.oms.task.OmsTaskHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class FileToOssHandler extends OmsTaskHandler<DefaultTaskMessage> {

    @Resource
    MmsNovelFileService mmsNovelFileService;

    @Override
    public String getHandlerId() {
        return MmsHandlerTagConstant.FileToOssHandler;
    }

    @Override
    public void handleTask(DefaultTaskMessage taskMsg) throws IOException {
        int size = Integer.parseInt(taskMsg.getBizValue());
        if (size <= 0) {
            size = Integer.MAX_VALUE;
        }
        UMmsPageInput uMmsPageInput = new UMmsPageInput();
        uMmsPageInput.setPageSize(size);
        uMmsPageInput.setPageNo(1);
        // 获取未处理的本地文件列表
        BaseQueryPage<LocalFileSimpleDTO> unprocessedLocalFile = mmsNovelFileService.getUnprocessedLocalFile(uMmsPageInput);
        List<LocalFileSimpleDTO> rows = unprocessedLocalFile.getRows();

        rows.forEach(f -> {
            try {
                mmsNovelFileService.syncLocalFile(f, MockFunction.getOperator());
                updateProgress(rows.size(), "Processed: " + f.getFileName());
            } catch (Exception e) {
                // 记录失败日志或更新 job 状态，但不中断其他文件处理
                updateProgress(rows.size(), "Failed to process file: " + f.getFileName() + ", error: " + e.getMessage());
            }
        });
    }

}

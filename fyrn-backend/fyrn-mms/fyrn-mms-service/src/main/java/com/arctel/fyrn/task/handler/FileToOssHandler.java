package com.arctel.fyrn.task.handler;

import com.arctel.common.MockFunction;
import com.arctel.fyrn.constants.MmsHandlerTagConstant;
import com.arctel.fyrn.dto.LocalFileSimpleDTO;
import com.arctel.fyrn.input.UMmsPageInput;
import com.arctel.fyrn.service.MmsNovelFileService;
import com.arctel.oms.common.base.BaseQueryPage;
import com.arctel.oms.common.utils.Result;
import com.arctel.oms.infrastructure.task.OmsTaskHandler;
import com.arctel.oms.infrastructure.task.DefaultTaskMessage;
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
        Result<BaseQueryPage<LocalFileSimpleDTO>> unprocessedLocalFile = mmsNovelFileService.getUnprocessedLocalFile(uMmsPageInput);
        List<LocalFileSimpleDTO> rows = unprocessedLocalFile.getData().getRows();

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
package com.arctel.mms.controller;


import com.arctel.domain.dto.input.SyncMaterialInput;
import com.arctel.mms.service.DataSyncService;
import com.arctel.oms.common.domain.OmsJob;
import com.arctel.oms.common.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 数据同步控制器
 *
 * @author ryan
 * @description
 * @createDate
 */
@RequestMapping("/sync")
@RestController
@Validated
public class DataSyncController {

    @Resource
    DataSyncService dataSyncService;

    /**
     * 同步小说任务
     *
     * @return
     */
    @PostMapping("/ossToMms")
    public Result<OmsJob> ossToMms() {
        return Result.success(dataSyncService.ossToMms());
    }

    @PostMapping("/fileToOss")
    public Result<String> fileToOss(SyncMaterialInput input) throws IOException {
        return Result.success(dataSyncService.fileToOss(input));
    }

}

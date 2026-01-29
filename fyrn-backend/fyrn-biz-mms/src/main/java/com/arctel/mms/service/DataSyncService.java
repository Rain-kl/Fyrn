package com.arctel.mms.service;

import com.arctel.domain.dto.input.SyncMaterialInput;
import com.arctel.oms.common.domain.OmsJob;
import com.arctel.oms.common.utils.Result;

import java.io.IOException;

/**
 * @author ryan
 * @description
 * @createDate
 */
public interface DataSyncService {


    /**
     * 同步本地文件到OSS
     */
    String fileToOss(SyncMaterialInput input) throws IOException;

    /**
     * 同步OSS文件到MMS
     */
    OmsJob ossToMms();

}

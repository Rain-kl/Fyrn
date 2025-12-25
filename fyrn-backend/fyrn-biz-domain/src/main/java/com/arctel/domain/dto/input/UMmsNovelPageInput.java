package com.arctel.domain.dto.input;

import com.arctel.oms.common.base.BaseQueryPageInput;
import lombok.Data;

@Data
public class UMmsNovelPageInput extends BaseQueryPageInput {
    /**
     * 小说ID, 关联 mms_novel.id
     */
    private Long novelId;

    /**
     * 文件名称
     */
    private String fileName;
}

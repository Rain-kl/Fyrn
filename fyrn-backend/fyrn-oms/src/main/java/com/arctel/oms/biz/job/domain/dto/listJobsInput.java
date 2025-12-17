package com.arctel.oms.biz.job.domain.dto;

import com.arctel.oms.common.base.BaseQueryPageInput;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class listJobsInput extends BaseQueryPageInput {

    /**
     * 业务任务ID(对外返回)
     */
    private String jobId = null;

    /**
     * 任务类型，如 file_batch
     */
    private String taskType = null;

    /**
     * 0=queued,1=running,2=success,3=failed,4=canceled
     */
    private Integer status = null;

}

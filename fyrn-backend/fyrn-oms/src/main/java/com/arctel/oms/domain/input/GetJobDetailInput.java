package com.arctel.oms.domain.input;

import com.arctel.oms.common.base.BaseInput;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetJobDetailInput extends BaseInput {

    String jobId;

    public String getJobId() {
        if(StringUtils.isBlank(jobId)){
            throw new IllegalArgumentException("jobId 不能为空");
        }
        return jobId;
    }
}

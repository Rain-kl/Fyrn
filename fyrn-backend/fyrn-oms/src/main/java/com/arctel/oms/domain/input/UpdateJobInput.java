package com.arctel.oms.domain.input;

import com.arctel.oms.common.base.BaseInput;
import com.arctel.oms.common.constants.JobStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobInput extends BaseInput {
    String jobId;
    Integer status;
    String message;
}
